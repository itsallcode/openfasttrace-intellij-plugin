package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.EdtTestUtil;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfiguration;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfigurationFactory;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfigurationType;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

public class OftTraceTestRunnerRunContentOutputPresenterTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~show-trace-project-in-test-runner-ui-by-default~1]
    public void testGivenTraceResultWhenPresentedThenItShowsTestRunnerRunContent() {
        final AtomicReference<SMTRunnerConsoleView> consoleRef = new AtomicReference<>();
        final AtomicReference<RunContentDescriptor> descriptorRef = new AtomicReference<>();
        final OftTraceTestRunnerRunContentOutputPresenter presenter =
                new OftTraceTestRunnerRunContentOutputPresenter(
                        project -> {
                            final SMTRunnerConsoleView console = createConsole();
                            consoleRef.set(console);
                            return console;
                        },
                        (project, descriptor) -> descriptorRef.set(descriptor)
                );

        try {
            EdtTestUtil.runInEdtAndWait(() -> presenter.show(
                    getProject(),
                    "OpenFastTrace Trace: default",
                    OftTraceResult.invalidInput("invalid configuration")
            ));

            assertThat(consoleRef.get(), notNullValue());
            assertThat(descriptorRef.get(), notNullValue());
            assertThat(descriptorRef.get().getExecutionId(), Matchers.greaterThan(0L));
            assertThat(
                    consoleRef.get().getResultsViewer().getTestsRootNode().getChildren().getFirst().getName(),
                    containsString("could not start")
            );
        } finally {
            if (descriptorRef.get() != null) {
                Disposer.dispose(descriptorRef.get());
            } else if (consoleRef.get() != null) {
                Disposer.dispose(consoleRef.get());
            }
        }
    }

    private SMTRunnerConsoleView createConsole() {
        final SMTRunnerConsoleProperties properties = new SMTRunnerConsoleProperties(
                createConfiguration(),
                "OpenFastTrace",
                DefaultRunExecutor.getRunExecutorInstance()
        );
        final SMTRunnerConsoleView console = new SMTRunnerConsoleView(properties);
        console.initUI();
        return console;
    }

    private OftRunConfiguration createConfiguration() {
        return new OftRunConfiguration(
                getProject(),
                new OftRunConfigurationFactory(new OftRunConfigurationType()),
                "OpenFastTrace"
        );
    }
}
