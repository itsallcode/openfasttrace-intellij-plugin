package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.util.Disposer;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;

public class OftRunProfileStateTest extends AbstractOftPlatformTestCase {
    public void testExecuteReturnsNotNull() throws Exception {
        OftRunConfiguration configuration = new OftRunConfiguration(
                getProject(),
                new OftRunConfigurationFactory(new OftRunConfigurationType()),
                "Test"
        );
        ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(
                DefaultRunExecutor.getRunExecutorInstance(),
                configuration
        ).build();
        OftRunProfileState state = new OftRunProfileState(environment, OftTraceSettingsSnapshot.DEFAULT);

        ExecutionResult result = state.execute(environment.getExecutor(), environment.getRunner());

        assertNotNull("ExecutionResult should not be null", result);
        if (result != null && result.getExecutionConsole() != null) {
            Disposer.register(getTestRootDisposable(), result.getExecutionConsole());
        }
    }
}
