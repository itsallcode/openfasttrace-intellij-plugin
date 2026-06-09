package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.EdtTestUtil;
import org.itsallcode.openfasttrace.api.core.ItemStatus;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.LinkStatus;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfiguration;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfigurationFactory;
import org.itsallcode.openfasttrace.intellijplugin.trace.runconfig.OftRunConfigurationType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// [itest->dsn~trace-test-runner-presentation~1]
public class OftTraceTestRunnerOutputPresenterTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~show-trace-source-files-as-test-runner-suites~1]
    // [itest->dsn~show-trace-specification-items-as-test-runner-tests~1]
    // [itest->dsn~show-specification-item-title-in-test-runner-ui~1]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [itest->dsn~roll-up-source-file-suite-trace-status~1]
    // [itest->dsn~roll-up-top-level-trace-status~1]
    public void testGivenSuccessfulTraceResultWhenPresentedThenItCreatesPassedTestRunnerNodes() {
        final LinkedSpecificationItem requirement = titledItem(
                "req~clean_requirement~1",
                projectLocalPath("doc/requirements.md"),
                "Clean requirement"
        );
        final SMTRunnerConsoleView console = present(OftTraceResult.success("ok", trace(requirement)));
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();

        final SMTestProxy suite = childNamed(resultsViewer.getTestsRootNode(), "doc/requirements.md");
        final SMTestProxy item = childNamed(suite, "Clean requirement \u2014 req~clean_requirement~1 (covered)");

        assertThat(suite.isSuite(), is(true));
        assertThat(item.isSuite(), is(false));
        assertThat(item.getPresentableName(), is("Clean requirement \u2014 req~clean_requirement~1 (covered)"));
        assertThat(item.isPassed(), is(true));
        assertThat(suite.isDefect(), is(false));
        assertThat(resultsViewer.getTestsRootNode().isDefect(), is(false));
        assertThat(resultsViewer.getTotalTestCount(), is(1));
        assertThat(resultsViewer.getFailedTestCount(), is(0));
    }

    // [itest->dsn~show-trace-source-files-as-test-runner-suites~1]
    // [itest->dsn~show-trace-specification-items-as-test-runner-tests~1]
    // [itest->dsn~show-trace-links-as-test-runner-sub-tests~1]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [itest->dsn~map-trace-link-status-to-test-runner-status~1]
    // [itest->dsn~roll-up-source-file-suite-trace-status~1]
    // [itest->dsn~roll-up-top-level-trace-status~1]
    // [itest->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    // [itest->dsn~show-trace-link-defect-details-in-test-runner-ui~1]
    public void testGivenDefectiveTraceResultWhenPresentedThenItCreatesFailedItemAndLinkNodes() {
        final LinkedSpecificationItem implementation = titledItem(
                "impl~missing_requirement~1",
                projectLocalPath("src/Main.java"),
                "Missing requirement implementation"
        );
        implementation.addLinkToItemWithStatus(
                titledItem("req~missing_requirement~1", projectLocalPath("doc/requirements.md"), "Missing requirement"),
                LinkStatus.ORPHANED
        );

        final SMTRunnerConsoleView console = present(OftTraceResult.failure("not ok", trace(implementation)));
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();
        final SMTestProxy suite = childNamed(resultsViewer.getTestsRootNode(), "src/Main.java");
        final SMTestProxy item = childNamed(
                suite,
                "Missing requirement implementation \u2014 impl~missing_requirement~1 (defective)"
        );
        final SMTestProxy link = childNamed(
                item,
                "\u2192 Missing requirement \u2014 req~missing_requirement~1 (orphaned)"
        );

        assertThat(resultsViewer.getTestsRootNode().isDefect(), is(true));
        assertThat(suite.isDefect(), is(true));
        assertThat(item.isSuite(), is(true));
        assertThat(item.isDefect(), is(true));
        assertThat(link.isDefect(), is(true));
        assertThat(suite.getErrorMessage(), is("OpenFastTrace defects in src/Main.java."));
        assertThat(resultsViewer.getTestsRootNode().getErrorMessage(), is("OpenFastTrace trace contains defects."));
        assertThat(item.getErrorMessage(), is("Defective OpenFastTrace specification item impl~missing_requirement~1."));
        assertThat(item.getStacktrace(), containsString("Trace status: defective"));
        assertThat(item.getStacktrace(), containsString("orphaned link to req~missing_requirement~1"));
        assertThat(link.getPresentableName(),
                is("\u2192 Missing requirement \u2014 req~missing_requirement~1 (orphaned)"));
        assertThat(link.getErrorMessage(), is("Orphaned outgoing trace link."));
        assertThat(link.getStacktrace(), containsString("Owning item: impl~missing_requirement~1"));
        assertThat(link.getStacktrace(), containsString("Linked item: req~missing_requirement~1"));
        assertThat(link.getStacktrace(), containsString("OpenFastTrace could not find"));
        assertThat(resultsViewer.getTotalTestCount(), is(2));
        assertThat(resultsViewer.getFailedTestCount(), is(2));
    }

    public void testGivenResultWithoutStructuredTraceWhenPresentedThenItCreatesFailedFallbackNode() {
        final SMTRunnerConsoleView console = present(OftTraceResult.invalidInput("invalid configuration"));
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();

        final SMTestProxy fallbackNode = childNamed(
                resultsViewer.getTestsRootNode(),
                "OpenFastTrace trace could not start."
        );

        assertThat(fallbackNode.isDefect(), is(true));
        assertThat(fallbackNode.getPresentableName(), is("OpenFastTrace trace could not start."));
        assertThat(resultsViewer.getTestsRootNode().isDefect(), is(true));
        assertThat(fallbackNode.getErrorMessage(), is("OpenFastTrace trace could not start."));
        assertThat(fallbackNode.getStacktrace(), is("invalid configuration"));
        assertThat(resultsViewer.getTestsRootNode().getErrorMessage(), is("OpenFastTrace trace could not start."));
        assertThat(resultsViewer.getTestsRootNode().getStacktrace(), is("invalid configuration"));
        assertThat(resultsViewer.getTotalTestCount(), is(1));
        assertThat(resultsViewer.getFailedTestCount(), is(1));
    }

    private SMTRunnerConsoleView present(final OftTraceResult result) {
        final AtomicReference<SMTRunnerConsoleView> consoleRef = new AtomicReference<>();
        final OftTraceTestRunnerOutputPresenter presenter = new OftTraceTestRunnerOutputPresenter(project -> {
            final SMTRunnerConsoleView console = createConsole();
            consoleRef.set(console);
            return console;
        });

        EdtTestUtil.runInEdtAndWait(() -> presenter.show(
                getProject(),
                "OpenFastTrace Trace: test-runner",
                result
        ));

        assertThat(consoleRef.get(), notNullValue());
        return consoleRef.get();
    }

    private SMTRunnerConsoleView createConsole() {
        final SMTRunnerConsoleProperties properties = new SMTRunnerConsoleProperties(
                createConfiguration(),
                "OpenFastTrace",
                DefaultRunExecutor.getRunExecutorInstance()
        );
        final SMTRunnerConsoleView console = new SMTRunnerConsoleView(properties);
        console.initUI();
        Disposer.register(getTestRootDisposable(), console);
        return console;
    }

    private OftRunConfiguration createConfiguration() {
        return new OftRunConfiguration(
                getProject(),
                new OftRunConfigurationFactory(new OftRunConfigurationType()),
                "OpenFastTrace"
        );
    }

    private String projectLocalPath(final String relativePath) {
        return Objects.requireNonNull(getProject().getBasePath()) + "/" + relativePath;
    }

    private static SMTestProxy childNamed(final SMTestProxy parent, final String name) {
        final List<? extends SMTestProxy> matchingChildren = parent.getChildren().stream()
                .filter(child -> child.getName().equals(name))
                .toList();
        assertThat(matchingChildren, hasSize(1));
        return matchingChildren.getFirst();
    }

    private static Trace trace(final LinkedSpecificationItem... items) {
        final List<LinkedSpecificationItem> traceItems = Arrays.asList(items);
        return Trace.builder()
                .items(traceItems)
                .defectItems(traceItems.stream()
                        .filter(LinkedSpecificationItem::isDefect)
                        .toList())
                .build();
    }

    private static LinkedSpecificationItem titledItem(final String id, final String locationPath, final String title) {
        return new LinkedSpecificationItem(SpecificationItem.builder()
                .id(SpecificationItemId.parseId(id))
                .title(title)
                .status(ItemStatus.APPROVED)
                .location(locationPath, 1)
                .build());
    }
}
