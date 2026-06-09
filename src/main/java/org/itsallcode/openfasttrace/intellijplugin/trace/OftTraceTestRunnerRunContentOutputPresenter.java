package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfiguration;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfigurationFactory;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfigurationType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class OftTraceTestRunnerRunContentOutputPresenter implements OftTraceOutputPresenter {
    private static final AtomicInteger NEXT_DESCRIPTOR_ID = new AtomicInteger(1);

    private final Function<Project, SMTRunnerConsoleView> consoleFactory;
    private final BiConsumer<Project, RunContentDescriptor> runContentShower;

    public OftTraceTestRunnerRunContentOutputPresenter() {
        this(
                OftTraceTestRunnerRunContentOutputPresenter::createTraceConsole,
                (project, descriptor) -> RunContentManager.getInstance(project)
                        .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)
        );
    }

    OftTraceTestRunnerRunContentOutputPresenter(
            final Function<Project, SMTRunnerConsoleView> consoleFactory,
            final BiConsumer<Project, RunContentDescriptor> runContentShower
    ) {
        this.consoleFactory = consoleFactory;
        this.runContentShower = runContentShower;
    }

    // [impl->dsn~show-trace-project-in-test-runner-ui-by-default~1]
    @Override
    public void show(final Project project, final String contentTitle, final OftTraceResult result) {
        final AtomicReference<SMTRunnerConsoleView> consoleRef = new AtomicReference<>();
        final OftTraceTestRunnerOutputPresenter presenter =
                new OftTraceTestRunnerOutputPresenter(p -> createConsole(p, consoleRef));

        presenter.show(project, contentTitle, result);

        final SMTRunnerConsoleView console = consoleRef.get();
        final RunContentDescriptor descriptor =
                new RunContentDescriptor(console, null, console.getComponent(), contentTitle);
        descriptor.setExecutionId(NEXT_DESCRIPTOR_ID.getAndIncrement());
        Disposer.register(descriptor, console);
        runContentShower.accept(project, descriptor);
    }

    private SMTRunnerConsoleView createConsole(
            final Project project,
            final AtomicReference<SMTRunnerConsoleView> consoleRef
    ) {
        final SMTRunnerConsoleView console = consoleFactory.apply(project);
        consoleRef.set(console);
        return console;
    }

    private static SMTRunnerConsoleView createTraceConsole(final Project project) {
        final SMTRunnerConsoleProperties properties = new SMTRunnerConsoleProperties(
                project,
                createRunConfiguration(project),
                "OpenFastTrace",
                DefaultRunExecutor.getRunExecutorInstance()
        );
        final SMTRunnerConsoleView console = new SMTRunnerConsoleView(properties);
        console.initUI();
        return console;
    }

    private static OftRunConfiguration createRunConfiguration(final Project project) {
        return new OftRunConfiguration(
                project,
                new OftRunConfigurationFactory(new OftRunConfigurationType()),
                "Trace Project"
        );
    }
}
