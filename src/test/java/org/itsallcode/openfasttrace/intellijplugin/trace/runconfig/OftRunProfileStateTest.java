package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.openapi.util.Disposer;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResultView;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class OftRunProfileStateTest extends AbstractOftPlatformTestCase {
    public void testExecuteReturnsNotNull() throws Exception {
        final ExecutionEnvironment environment = createEnvironment();
        final OftRunProfileState state = new OftRunProfileState(
                environment,
                invalidInputsSettings(OftTraceResultView.PLAIN_TEXT)
        );

        final ExecutionResult result = state.execute(environment.getExecutor(), environment.getRunner());

        assertNotNull("ExecutionResult should not be null", result);
        if (result != null && result.getExecutionConsole() != null) {
            Disposer.register(getTestRootDisposable(), result.getExecutionConsole());
        }
    }

    // [itest->dsn~plain-text-as-default-run-configuration-result-view~1]
    public void testGivenPlainTextResultViewWhenExecutingThenItUsesPlainTextConsole() throws Exception {
        final ExecutionEnvironment environment = createEnvironment();
        final OftRunProfileState state = new OftRunProfileState(
                environment,
                invalidInputsSettings(OftTraceResultView.PLAIN_TEXT)
        );

        final ExecutionResult result = state.execute(environment.getExecutor(), environment.getRunner());

        Disposer.register(getTestRootDisposable(), result.getExecutionConsole());
        assertThat(result.getExecutionConsole(), is(not(instanceOf(SMTRunnerConsoleView.class))));
    }

    // [itest->dsn~select-test-runner-trace-result-view~1]
    public void testGivenTestRunnerResultViewWhenExecutingThenItUsesTestRunnerConsole() throws Exception {
        final ExecutionEnvironment environment = createEnvironment();
        final OftRunProfileState state = new OftRunProfileState(
                environment,
                invalidInputsSettings(OftTraceResultView.TEST_RUNNER)
        );

        final ExecutionResult result = state.execute(environment.getExecutor(), environment.getRunner());

        Disposer.register(getTestRootDisposable(), result.getExecutionConsole());
        assertThat(result.getExecutionConsole(), is(instanceOf(SMTRunnerConsoleView.class)));
    }

    private ExecutionEnvironment createEnvironment() {
        final OftRunConfiguration configuration = new OftRunConfiguration(
                getProject(),
                new OftRunConfigurationFactory(new OftRunConfigurationType()),
                "Test"
        );
        return ExecutionEnvironmentBuilder.create(
                DefaultRunExecutor.getRunExecutorInstance(),
                configuration
        ).build();
    }

    private static OftTraceSettingsSnapshot invalidInputsSettings(final OftTraceResultView resultView) {
        return new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                false,
                "",
                "",
                "",
                resultView
        );
    }
}
