package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.testframework.Printable;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.ui.ConsoleViewContentType;
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
import org.jspecify.annotations.NonNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

// [itest->dsn~trace-test-runner-presentation~2]
public class OftTraceTestRunnerOutputPresenterTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~show-trace-source-files-as-test-runner-suites~2]
    // [itest->dsn~show-trace-specification-items-as-test-runner-tests~2]
    // [itest->dsn~show-specification-item-title-in-test-runner-ui~2]
    // [itest->dsn~show-specification-item-id-in-test-runner-details~1]
    // [itest->dsn~show-specification-item-status-in-test-runner-ui~2]
    // [itest->dsn~navigate-from-test-runner-source-files~1]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~2]
    // [itest->dsn~roll-up-source-file-suite-trace-status~1]
    // [itest->dsn~roll-up-top-level-trace-status~1]
    public void testGivenSuccessfulTraceResultWhenPresentedThenItCreatesPassedTestRunnerNodes() {
        myFixture.addFileToProject("doc/requirements.md", """
                req~clean_requirement~1
                """);
        final LinkedSpecificationItem requirement = titledItem(
                "req~clean_requirement~1",
                projectLocalPath("doc/requirements.md"),
                "Clean requirement"
        );
        final SMTRunnerConsoleView console = present(OftTraceResult.success("ok", trace(requirement)));
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();

        final SMTestProxy suite = childNamed(resultsViewer.getTestsRootNode(), "doc/requirements.md");
        final SMTestProxy item = childNamed(suite, "Clean requirement");

        assertThat(suite.isSuite(), is(true));
        assertThat(suite.canNavigate(), is(true));
        assertThat(item.isSuite(), is(false));
        assertThat(item.getPresentableName(), is("Clean requirement"));
        assertThat(item.isPassed(), is(true));
        assertThat(ownOutput(item), containsString("Specification item ID: req~clean_requirement~1"));
        assertThat(ownOutput(item), containsString("Trace status: covered"));
        assertThat(suite.isDefect(), is(false));
        assertThat(resultsViewer.getTestsRootNode().isDefect(), is(false));
        assertThat(totalTestCount(resultsViewer), is(1));
        assertThat(failedTestCount(resultsViewer), is(0));
    }

    // [itest->dsn~show-trace-source-files-as-test-runner-suites~2]
    // [itest->dsn~show-trace-specification-items-as-test-runner-tests~2]
    // [itest->dsn~show-trace-links-as-test-runner-sub-tests~2]
    // [itest->dsn~show-specification-item-status-in-test-runner-ui~2]
    // [itest->dsn~show-trace-link-status-in-test-runner-ui~2]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~2]
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
                "Missing requirement implementation (defective)"
        );
        final SMTestProxy link = childNamed(
                item,
                "⊙→ Missing requirement (orphaned)"
        );

        assertThat(resultsViewer.getTestsRootNode().isDefect(), is(true));
        assertThat(suite.isDefect(), is(true));
        assertThat(item.isSuite(), is(true));
        assertThat(item.isDefect(), is(true));
        assertThat(link.isDefect(), is(true));
        assertThat(suite.getErrorMessage(), is("OpenFastTrace defects in src/Main.java."));
        assertThat(resultsViewer.getTestsRootNode().getErrorMessage(), is("OpenFastTrace trace contains defects."));
        assertThat(item.getErrorMessage(), is("Defective OpenFastTrace specification item impl~missing_requirement~1."));
        assertThat(item.getStacktrace(), containsString("Specification item ID: impl~missing_requirement~1"));
        assertThat(item.getStacktrace(), containsString("Trace status: defective"));
        assertThat(item.getStacktrace(), containsString("orphaned link to req~missing_requirement~1"));
        assertThat(link.getPresentableName(),
                is("⊙→ Missing requirement (orphaned)"));
        assertThat(link.getErrorMessage(), is("Orphaned outgoing trace link."));
        assertThat(link.getStacktrace(), containsString("Owning item ID: impl~missing_requirement~1"));
        assertThat(link.getStacktrace(), containsString("Linked item ID: req~missing_requirement~1"));
        assertThat(link.getStacktrace(), containsString("OpenFastTrace could not find"));
        assertThat(totalTestCount(resultsViewer), is(2));
        assertThat(failedTestCount(resultsViewer), is(2));
        assertThat(resultsViewer.getTotalTestCount(), is(1));
        assertThat(resultsViewer.getFinishedTestCount(), is(1));
        assertThat(resultsViewer.getFailedTestCount(), is(1));
    }

    // [itest->dsn~count-only-specification-items-in-test-runner-results~1]
    // [itest->dsn~show-trace-source-files-as-test-runner-suites~2]
    // [itest->dsn~show-trace-specification-items-as-test-runner-tests~2]
    // [itest->dsn~show-trace-links-as-test-runner-sub-tests~2]
    public void testGivenCleanTraceWithLinkedItemsInMultipleSuitesWhenPresentedThenItCountsOnlyItems() {
        final LinkedSpecificationItem requirement = titledItem(
                "req~covered_requirement~1",
                projectLocalPath("doc/requirements.md"),
                "Covered requirement"
        );
        final LinkedSpecificationItem implementation = titledItem(
                "impl~covered_requirement~1",
                projectLocalPath("src/CoveredRequirement.java"),
                "Covered requirement implementation"
        );
        implementation.addLinkToItemWithStatus(requirement, LinkStatus.COVERS);

        final SMTestRunnerResultsForm resultsViewer = present(OftTraceResult.success(
                "ok",
                trace(requirement, implementation)
        )).getResultsViewer();

        assertAll(
                () -> assertThat(resultsViewer.getTestsRootNode().getChildren(), hasSize(2)),
                () -> assertThat(resultsViewer.getTotalTestCount(), is(2)),
                () -> assertThat(resultsViewer.getFinishedTestCount(), is(2)),
                () -> assertThat(resultsViewer.getFailedTestCount(), is(0))
        );
    }

    // [itest->dsn~mark-transitive-defects-in-test-runner~1]
    // [itest->dsn~trace-test-runner-presentation~2]
    // [itest->dsn~show-specification-item-status-in-test-runner-ui~2]
    // [itest->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    // [itest->dsn~roll-up-source-file-suite-trace-status~1]
    // [itest->dsn~roll-up-top-level-trace-status~1]
    public void testGivenTransitiveDefectTraceResultWhenPresentedThenItPrefixesTheNodeNameAndExplainsTheError()
            throws IOException {
        writeUncleanTraceChainProject(Path.of(Objects.requireNonNull(getProject().getBasePath())));

        final OftTraceResult result = new OftTraceService().traceProject(
                OftTraceInputs.wholeProject(Path.of(Objects.requireNonNull(getProject().getBasePath())), List.of(), List.of()),
                OftTraceProgress.NONE
        );
        final SMTRunnerConsoleView console = present(result);
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();
        final SMTestProxy suite = resultsViewer.getTestsRootNode().getChildren().getFirst();
        final SMTestProxy transitiveFeature = suite.getChildren().stream()
                .filter(child -> child.getName().startsWith("↳ Feature"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(transitiveFeature.isSuite(), is(true)),
                () -> assertThat(transitiveFeature.isDefect(), is(true)),
                () -> assertThat(transitiveFeature.getErrorMessage(),
                        is("Transitive trace defect. The problem is not in this item but in one it depends on.")),
                () -> assertThat(transitiveFeature.getStacktrace(), containsString("Specification item ID: feat~chain_feature~1")),
                () -> assertThat(transitiveFeature.getStacktrace(), containsString("Trace status: uncovered")),
                () -> assertThat(transitiveFeature.getStacktrace(), containsString("Fix the specification items this one depends on.")),
                () -> assertThat(resultsViewer.getTestsRootNode().isDefect(), is(true)));
    }

    // [itest->dsn~hide-transitive-defects-in-test-runner-ui~1]
    // [itest->dsn~trace-test-runner-presentation~2]
    public void testGivenTransitiveDefectTraceResultWhenTransitiveDefectsAreHiddenThenItOmitsTransitiveItems()
            throws IOException {
        writeUncleanTraceChainProject(Path.of(Objects.requireNonNull(getProject().getBasePath())));

        final OftTraceResult result = new OftTraceService(false).traceProject(
                OftTraceInputs.wholeProject(Path.of(Objects.requireNonNull(getProject().getBasePath())), List.of(), List.of()),
                OftTraceProgress.NONE
        );
        final SMTRunnerConsoleView console = present(result, false);
        final SMTestRunnerResultsForm resultsViewer = console.getResultsViewer();
        final SMTestProxy suite = resultsViewer.getTestsRootNode().getChildren().getFirst();

        assertAll(
                () -> assertThat(suite.getChildren(), hasSize(1)),
                () -> assertThat(suite.getChildren().getFirst().getName(), is("Design (uncovered)")),
                () -> assertThat(suite.getChildren().getFirst().isDefect(), is(true)),
                () -> assertThat(resultsViewer.getTestsRootNode().isDefect(), is(true))
        );
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
        assertThat(totalTestCount(resultsViewer), is(1));
        assertThat(failedTestCount(resultsViewer), is(1));
    }

    private SMTRunnerConsoleView present(final OftTraceResult result) {
        return present(result, true);
    }

    private SMTRunnerConsoleView present(final OftTraceResult result, final boolean showTransitiveDefects) {
        final AtomicReference<SMTRunnerConsoleView> consoleRef = new AtomicReference<>();
        final OftTraceTestRunnerOutputPresenter presenter = new OftTraceTestRunnerOutputPresenter(
                project -> {
                    final SMTRunnerConsoleView console = createConsole();
                    consoleRef.set(console);
                    return console;
                },
                showTransitiveDefects
        );

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

    private static int totalTestCount(final SMTestRunnerResultsForm resultsViewer) {
        return nodesForTest(resultsViewer).size();
    }

    private static int failedTestCount(final SMTestRunnerResultsForm resultsViewer) {
        return Math.toIntExact(nodesForTest(resultsViewer).stream()
                .filter(SMTestProxy::isDefect)
                .count());
    }

    private static List<SMTestProxy> nodesForTest(final SMTestRunnerResultsForm resultsViewer) {
        final SMTestProxy root = resultsViewer.getTestsRootNode();
        return root.getAllTests().stream()
                .filter(test -> test != root)
                .filter(test -> test.getParent() != root || !test.isSuite())
                .toList();
    }

    private static String ownOutput(final SMTestProxy proxy) {
        final StringBuilder output = new StringBuilder();
        proxy.printOwnPrintablesOn(new Printer() {
            @Override
            public void print(final @NonNull String text, final @NonNull ConsoleViewContentType contentType) {
                output.append(text);
            }

            @Override
            public void onNewAvailable(final @NonNull Printable printable) {
                // Intentionally empty.
            }

            @Override
            public void printHyperlink(final @NonNull String text, final HyperlinkInfo info) {
                output.append(text);
            }

            @Override
            public void mark() {
                // Intentionally empty.
            }
        });
        return output.toString();
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

    private static LinkedSpecificationItem titledItem(
            final String id,
            final String locationPath,
            final String title,
            final String... needsArtifactTypes
    ) {
        final SpecificationItem.Builder builder = SpecificationItem.builder()
                .id(SpecificationItemId.parseId(id))
                .title(title)
                .status(ItemStatus.APPROVED)
                .location(locationPath, 1);
        Arrays.stream(needsArtifactTypes).forEach(builder::addNeedsArtifactType);
        return new LinkedSpecificationItem(builder.build());
    }

    private void writeUncleanTraceChainProject(final Path projectRoot) throws IOException {
        final Path docDirectory = Files.createDirectories(projectRoot.resolve("doc"));
        Files.writeString(
                docDirectory.resolve("trace.md"),
                """
                ### Feature
                `feat~chain_feature~1`

                Needs: req

                ### Requirement
                `req~chain_requirement~1`

                Covers:
                - `feat~chain_feature~1`

                Needs: dsn

                ### Design
                `dsn~chain_design~1`

                Covers:
                - `req~chain_requirement~1`

                Needs: impl
                """
        );
    }
}
