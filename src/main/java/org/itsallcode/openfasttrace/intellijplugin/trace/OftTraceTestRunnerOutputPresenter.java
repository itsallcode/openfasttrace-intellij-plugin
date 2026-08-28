package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestTree.OftTraceItemNode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestTree.OftTraceLinkNode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestTree.OftTraceSuiteNode;

import java.util.function.Function;

public final class OftTraceTestRunnerOutputPresenter implements OftTraceOutputPresenter {
    private static final String SPECIFICATION_ITEM_PROGRESS_CATEGORY = "OpenFastTrace specification items";

    private final Function<Project, SMTRunnerConsoleView> consoleFactory;
    private final boolean showTransitiveDefects;

    public OftTraceTestRunnerOutputPresenter(final Function<Project, SMTRunnerConsoleView> consoleFactory) {
        this(consoleFactory, true);
    }

    public OftTraceTestRunnerOutputPresenter(
            final Function<Project, SMTRunnerConsoleView> consoleFactory,
            final boolean showTransitiveDefects
    ) {
        this.consoleFactory = consoleFactory;
        this.showTransitiveDefects = showTransitiveDefects;
    }

    // [impl->dsn~trace-test-runner-presentation~2]
    // [impl->dsn~count-only-specification-items-in-test-runner-results~1]
    @Override
    public void show(final Project project, final String contentTitle, final OftTraceResult result) {
        final SMTRunnerConsoleView console = consoleFactory.apply(project);
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();
        final SMTestProxy.SMRootTestProxy root = resultsViewer.getTestsRootNode();
        root.setPresentation(contentTitle);
        resultsViewer.onTestingStarted(root);
        final PresentationOutcome outcome = result.trace()
                .map(trace -> showTrace(project, resultsViewer, root, OftTraceTestTreeMapper.map(
                        trace,
                        project.getBasePath(),
                        showTransitiveDefects
                )))
                .orElseGet(() -> showResultWithoutTrace(console, resultsViewer, root, result));
        if (outcome.failed()) {
            markFailed(root, outcome.details());
        }
        root.setFinished();
        resultsViewer.onTestingFinished(root);
    }

    private static PresentationOutcome showTrace(
            final Project project,
            final SMTestRunnerResultsForm resultsViewer,
            final SMTestProxy.SMRootTestProxy root,
            final OftTraceTestTree tree
    ) {
        resultsViewer.onCustomProgressTestsCategory(SPECIFICATION_ITEM_PROGRESS_CATEGORY, tree.testCount());
        for (final OftTraceSuiteNode suite : tree.suites()) {
            showSuite(project, resultsViewer, root, suite);
        }
        return new PresentationOutcome(
                tree.failed(),
                tree.failed() ? OftTraceTestNodeDetails.topLevelFailure() : OftTraceTestNodeDetails.none()
        );
    }

    private static void showSuite(
            final Project project,
            final SMTestRunnerResultsForm resultsViewer,
            final SMTestProxy parent,
            final OftTraceSuiteNode suite
    ) {
        final SMTestProxy suiteProxy = OftTraceTestProxy.sourceFileSuite(project, suite.name(), suite.sourcePath());
        parent.addChild(suiteProxy);
        suiteProxy.setSuiteStarted();
        resultsViewer.onSuiteStarted(suiteProxy);
        for (final OftTraceItemNode item : suite.items()) {
            showItem(project, resultsViewer, suiteProxy, item);
        }
        if (suite.failed()) {
            markFailed(suiteProxy, suite.failureDetails());
        }
        suiteProxy.setFinished();
        resultsViewer.onSuiteFinished(suiteProxy);
    }

    private static void showItem(
            final Project project,
            final SMTestRunnerResultsForm resultsViewer,
            final SMTestProxy parent,
            final OftTraceItemNode item
    ) {
        resultsViewer.onCustomProgressTestStarted();
        final boolean expandable = itemHasLinks(item);
        final SMTestProxy itemProxy = new OftTraceTestProxy(project, item.name(), expandable, item.navigationId());
        parent.addChild(itemProxy);
        if (expandable) {
            itemProxy.setSuiteStarted();
        } else {
            itemProxy.setStarted();
        }
        resultsViewer.onSuiteStarted(itemProxy);
        if (item.failed()) {
            markFailed(itemProxy, item.failureDetails());
            resultsViewer.onCustomProgressTestFailed();
        } else {
            addPassedDetails(itemProxy, item.details());
        }
        for (final OftTraceLinkNode link : item.links()) {
            showLink(project, resultsViewer, itemProxy, link);
        }
        itemProxy.setFinished();
        resultsViewer.onSuiteFinished(itemProxy);
        resultsViewer.onCustomProgressTestFinished();
    }

    private static boolean itemHasLinks(final OftTraceItemNode item) {
        return !item.links().isEmpty();
    }

    private static void showLink(
            final Project project,
            final SMTestRunnerResultsForm resultsViewer,
            final SMTestProxy parent,
            final OftTraceLinkNode link
    ) {
        final SMTestProxy linkProxy = new OftTraceTestProxy(project, link.name(), false, link.navigationId());
        parent.addChild(linkProxy);
        linkProxy.setStarted();
        resultsViewer.onSuiteStarted(linkProxy);
        if (link.failed()) {
            markFailed(linkProxy, link.details());
        } else {
            addPassedDetails(linkProxy, link.details());
        }
        linkProxy.setFinished();
        resultsViewer.onSuiteFinished(linkProxy);
    }

    private static void showTestNode(
            final SMTestRunnerResultsForm resultsViewer,
            final SMTestProxy testProxy,
            final boolean failed,
            final OftTraceTestNodeDetails details
    ) {
        testProxy.setStarted();
        resultsViewer.onTestStarted(testProxy);
        if (failed) {
            markFailed(testProxy, details);
            resultsViewer.onTestFailed(testProxy);
        } else {
            addPassedDetails(testProxy, details);
        }
        testProxy.setFinished();
        resultsViewer.onTestFinished(testProxy);
    }

    private static PresentationOutcome showResultWithoutTrace(
            final SMTRunnerConsoleView console,
            final SMTestRunnerResultsForm resultsViewer,
            final SMTestProxy.SMRootTestProxy root,
            final OftTraceResult result
    ) {
        console.print(result.statusMessage() + System.lineSeparator(), ConsoleViewContentType.ERROR_OUTPUT);
        console.print(System.lineSeparator(), ConsoleViewContentType.NORMAL_OUTPUT);
        console.print(result.output(), ConsoleViewContentType.ERROR_OUTPUT);
        final SMTestProxy failureProxy =
                new SMTestProxy(result.statusMessage(), false, null, true);
        root.addChild(failureProxy);
        resultsViewer.onTestsCountInSuite(1);
        showTestNode(
                resultsViewer,
                failureProxy,
                result.requiresAttention(),
                OftTraceTestNodeDetails.resultWithoutTrace(result)
        );
        return new PresentationOutcome(
                result.requiresAttention(),
                result.requiresAttention()
                        ? OftTraceTestNodeDetails.resultWithoutTrace(result)
                        : OftTraceTestNodeDetails.none()
        );
    }

    // [impl->dsn~roll-up-source-file-suite-trace-status~1]
    // [impl->dsn~roll-up-top-level-trace-status~1]
    // [impl->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    // [impl->dsn~show-specification-item-id-in-test-runner-details~1]
    // [impl->dsn~show-trace-link-defect-details-in-test-runner-ui~1]
    // [impl->dsn~show-trace-link-id-details-in-test-runner-ui~1]
    private static void markFailed(final SMTestProxy testProxy, final OftTraceTestNodeDetails details) {
        testProxy.setTestFailed(details.failureMessage(), details.detailText(), false);
    }

    private static void addPassedDetails(final SMTestProxy testProxy, final OftTraceTestNodeDetails details) {
        if (details.hasDetailText()) {
            testProxy.addStdOutput(details.detailText() + System.lineSeparator());
        }
    }

    private record PresentationOutcome(boolean failed, OftTraceTestNodeDetails details) {
    }
}
