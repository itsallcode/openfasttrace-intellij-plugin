package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import java.util.function.BiConsumer;
import java.util.function.Function;

final class OftTraceRunContentOutputPresenter implements OftTraceOutputPresenter {
    public static final String IDEA_CYCLE_BUFFER_SIZE = "idea.cycle.buffer.size";
    private final Function<Project, ConsoleView> consoleFactory;
    private final BiConsumer<Project, RunContentDescriptor> runContentShower;
    private final OftAnsiConsoleOutput ansiConsoleOutput = new OftAnsiConsoleOutput();

    OftTraceRunContentOutputPresenter() {
        this(
                OftTraceRunContentOutputPresenter::createTraceConsole,
                (project, descriptor) -> RunContentManager.getInstance(project)
                        .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)
        );
    }

    OftTraceRunContentOutputPresenter(
            final Function<Project, ConsoleView> consoleFactory,
            final BiConsumer<Project, RunContentDescriptor> runContentShower
    ) {
        this.consoleFactory = consoleFactory;
        this.runContentShower = runContentShower;
    }

    // [impl->dsn~trace-output-presentation~1]
    @Override
    public void show(final Project project, final String contentTitle, final OftTraceResult result) {
        final ConsoleView console = consoleFactory.apply(project);
        print(console, result.statusMessage() + System.lineSeparator(), result.requiresAttention());
        print(console, System.lineSeparator(), false);
        ansiConsoleOutput.print(console, result.output());
        if (!result.output().endsWith(System.lineSeparator())) {
            print(console, System.lineSeparator(), false);
        }
        final RunContentDescriptor descriptor =
                new RunContentDescriptor(console, null, console.getComponent(), contentTitle);
        Disposer.register(descriptor, console);
        runContentShower.accept(project, descriptor);
    }

    private void print(final ConsoleView console, final String text, final boolean isError) {
        console.print(
                text,
                isError ? ConsoleViewContentType.ERROR_OUTPUT : ConsoleViewContentType.NORMAL_OUTPUT
        );
    }

    static ConsoleView createTraceConsole(final Project project) {
        final String previousBufferSize = System.getProperty(IDEA_CYCLE_BUFFER_SIZE);
        try {
            System.setProperty(IDEA_CYCLE_BUFFER_SIZE, "disabled");
            final ConsoleViewImpl console = new ConsoleViewImpl(project, true);
            console.getComponent();
            console.getEditor().getDocument().setCyclicBufferSize(0);
            return console;
        } finally {
            restoreCycleBufferSize(previousBufferSize);
        }
    }

    private static void restoreCycleBufferSize(final String previousBufferSize) {
        if (previousBufferSize == null) {
            System.clearProperty(IDEA_CYCLE_BUFFER_SIZE);
        } else {
            System.setProperty(IDEA_CYCLE_BUFFER_SIZE, previousBufferSize);
        }
    }
}
