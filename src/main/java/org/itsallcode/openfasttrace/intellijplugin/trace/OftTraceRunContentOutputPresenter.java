package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

final class OftTraceRunContentOutputPresenter implements OftTraceOutputPresenter {
    // [impl->dsn~trace-output-presentation~1]
    @Override
    public void show(final Project project, final String contentTitle, final OftTraceResult result) {
        final ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        print(console, result.statusMessage() + System.lineSeparator(), result.requiresAttention());
        print(console, System.lineSeparator(), false);
        print(console, result.output(), result.requiresAttention());
        if (!result.output().endsWith(System.lineSeparator())) {
            print(console, System.lineSeparator(), result.requiresAttention());
        }
        final RunContentDescriptor descriptor =
                new RunContentDescriptor(console, null, console.getComponent(), contentTitle);
        Disposer.register(descriptor, console);
        RunContentManager.getInstance(project).showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor);
    }

    private void print(final ConsoleView console, final String text, final boolean isError) {
        console.print(
                text,
                isError ? ConsoleViewContentType.ERROR_OUTPUT : ConsoleViewContentType.NORMAL_OUTPUT
        );
    }
}
