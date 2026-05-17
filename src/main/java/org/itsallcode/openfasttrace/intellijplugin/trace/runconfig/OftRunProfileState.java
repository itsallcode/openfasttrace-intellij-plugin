package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceBackgroundRunner;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceInputResolution;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceInputResolver;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceOutputPresenter;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResult;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceRunContentOutputPresenter;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceRunner;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceService;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OftRunProfileState implements RunProfileState {
    private final ExecutionEnvironment environment;
    private final OftTraceSettingsSnapshot settings;

    public OftRunProfileState(final ExecutionEnvironment environment, final OftTraceSettingsSnapshot settings) {
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public @Nullable ExecutionResult execute(final Executor executor, @NotNull final ProgramRunner<?> runner) {
        final Project project = environment.getProject();
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(project, settings);

        final String contentTitle = "OpenFastTrace Trace: " + environment.getRunProfile().getName();
        final ConsoleView console = OftTraceRunContentOutputPresenter.createTraceConsole(project);
        final OftTraceOutputPresenter outputPresenter = new OftTraceRunContentOutputPresenter(
                p -> console,
                (p, descriptor) -> {
                    // The descriptor is handled by the execution framework.
                }
        );

        if (!resolution.isValid()) {
            outputPresenter.show(project, contentTitle, OftTraceResult.invalidInput(resolution.errorMessage()));
            return new DefaultExecutionResult(console, new NopProcessHandler());
        }

        final ProcessHandler processHandler = new NopProcessHandler();
        final OftTraceRunner traceRunner = new OftTraceBackgroundRunner(
                new OftTraceService(),
                outputPresenter,
                processHandler
        );

        traceRunner.run(project, resolution.inputs(), contentTitle);
        return new DefaultExecutionResult(console, processHandler);
    }
}
