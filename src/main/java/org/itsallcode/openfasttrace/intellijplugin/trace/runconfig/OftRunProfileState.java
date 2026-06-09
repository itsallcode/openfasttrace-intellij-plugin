package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceBackgroundRunner;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceInputResolution;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceInputResolver;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceOutputPresenter;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResult;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResultView;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceRunContentOutputPresenter;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceRunner;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceService;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestRunnerOutputPresenter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public final class OftRunProfileState implements RunProfileState {
    private final ExecutionEnvironment environment;
    private final OftTraceSettingsSnapshot settings;

    public OftRunProfileState(final ExecutionEnvironment environment, final OftTraceSettingsSnapshot settings) {
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public @NonNull ExecutionResult execute(final Executor executor, @NotNull final ProgramRunner<?> runner) {
        final Project project = environment.getProject();
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(project, settings);

        final String contentTitle = "OpenFastTrace Trace: " + environment.getRunProfile().getName();
        final ExecutionPresentation executionPresentation = createExecutionPresentation(project, executor);

        if (!resolution.isValid()) {
            executionPresentation.outputPresenter()
                    .show(project, contentTitle, OftTraceResult.invalidInput(resolution.errorMessage()));
            return new DefaultExecutionResult(executionPresentation.console(), new NopProcessHandler());
        }

        final ProcessHandler processHandler = new NopProcessHandler();
        final OftTraceRunner traceRunner = new OftTraceBackgroundRunner(
                new OftTraceService(),
                executionPresentation.outputPresenter(),
                processHandler
        );

        traceRunner.run(project, resolution.inputs(), contentTitle);
        return new DefaultExecutionResult(executionPresentation.console(), processHandler);
    }

    private ExecutionPresentation createExecutionPresentation(final Project project, final Executor executor) {
        if (settings.resultView() == OftTraceResultView.TEST_RUNNER) {
            return createTestRunnerExecutionPresentation(project, executor);
        }
        return createPlainTextExecutionPresentation(project);
    }

    // [impl->dsn~select-plain-text-trace-result-view~1]
    private static ExecutionPresentation createPlainTextExecutionPresentation(final Project project) {
        final ConsoleView console = OftTraceRunContentOutputPresenter.createTraceConsole(project);
        final OftTraceOutputPresenter outputPresenter = new OftTraceRunContentOutputPresenter(
                p -> console,
                (p, descriptor) -> {
                    // The descriptor is handled by the execution framework.
                }
        );
        return new ExecutionPresentation(console, outputPresenter);
    }

    // [impl->dsn~test-runner-as-default-run-configuration-result-view~1]
    // [impl->dsn~select-test-runner-trace-result-view~1]
    private ExecutionPresentation createTestRunnerExecutionPresentation(
            final Project project,
            final Executor executor
    ) {
        final SMTRunnerConsoleProperties properties = new SMTRunnerConsoleProperties(
                project,
                environment.getRunProfile(),
                "OpenFastTrace",
                executor
        );
        final SMTRunnerConsoleView console = new SMTRunnerConsoleView(properties);
        console.initUI();
        final OftTraceOutputPresenter outputPresenter = new OftTraceTestRunnerOutputPresenter(p -> console);
        return new ExecutionPresentation(console, outputPresenter);
    }

    private record ExecutionPresentation(ExecutionConsole console, OftTraceOutputPresenter outputPresenter) {
    }
}
