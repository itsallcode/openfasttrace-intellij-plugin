package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.ItemStatus;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.LinkStatus;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class OftTraceTestTreeMapperTest {
    private static final String PROJECT_BASE = "/workspace/openfasttrace-intellij-plugin";

    private final OftTraceTestTreeMapper mapper = new OftTraceTestTreeMapper();

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~show-trace-source-files-as-test-runner-suites~1]
    // [itest->dsn~roll-up-source-file-suite-trace-status~1]
    // [itest->dsn~roll-up-top-level-trace-status~1]
    @Test
    void testGivenTraceItemsFromDifferentSourceFilesWhenMappingThenItCreatesSuitesBySourceFile() {
        final LinkedSpecificationItem firstRequirement = item("req~first_requirement~1", "doc/requirements.md");
        final LinkedSpecificationItem secondRequirement = item("req~second_requirement~1", "doc/requirements.md");
        final LinkedSpecificationItem implementation = item("impl~first_requirement~1", "src/Main.java");

        final OftTraceTestTree tree = mapper.map(trace(firstRequirement, secondRequirement, implementation));

        Assertions.assertAll(
                () -> assertThat(tree.suites().stream().map(OftTraceSuiteNode::name).toList(),
                        contains("doc/requirements.md", "src/Main.java")),
                () -> assertThat(suiteNamed(tree, "doc/requirements.md").items(), hasSize(2)),
                () -> assertThat(suiteNamed(tree, "src/Main.java").items(), hasSize(1)),
                () -> assertThat(tree.testCount(), is(3)),
                () -> assertThat(suiteNamed(tree, "doc/requirements.md").failed(), is(false)),
                () -> assertThat(tree.failed(), is(false))
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~show-trace-source-files-as-test-runner-suites~1]
    // [itest->dsn~navigate-from-test-runner-source-files~1]
    @Test
    void testGivenAbsoluteTraceItemPathsBelowProjectWhenMappingThenItCreatesSuitesWithProjectLocalPaths() {
        final LinkedSpecificationItem requirement = item(
                "req~project_local_requirement~1",
                PROJECT_BASE + "/doc/system_requirements.md"
        );
        final LinkedSpecificationItem implementation = item(
                "impl~project_local_requirement~1",
                PROJECT_BASE + "/src/main/java/Main.java"
        );

        final OftTraceTestTree tree = mapper.map(trace(requirement, implementation), PROJECT_BASE);

        Assertions.assertAll(
                () -> assertThat(
                        tree.suites().stream()
                                .map(OftTraceSuiteNode::name)
                                .toList(),
                        contains("doc/system_requirements.md", "src/main/java/Main.java")
                ),
                () -> assertThat(
                        suiteNamed(tree, "doc/system_requirements.md").sourcePath(),
                        is(PROJECT_BASE + "/doc/system_requirements.md")
                )
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~show-trace-specification-items-as-test-runner-tests~1]
    // [itest->dsn~show-trace-links-as-test-runner-sub-tests~1]
    // [itest->dsn~show-specification-item-title-in-test-runner-ui~2]
    // [itest->dsn~show-specification-item-id-in-test-runner-details~1]
    // [itest->dsn~show-specification-item-status-in-test-runner-ui~2]
    // [itest->dsn~show-trace-link-status-in-test-runner-ui~2]
    // [itest->dsn~show-trace-link-direction-in-test-runner-ui~1]
    // [itest->dsn~show-unicode-trace-link-direction-in-test-runner-ui~1]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [itest->dsn~map-trace-link-status-to-test-runner-status~1]
    // [itest->dsn~show-trace-link-id-details-in-test-runner-ui~1]
    @Test
    void testGivenCoveredRequirementWithIncomingTraceLinkWhenMappingThenItCreatesPassedItemAndLinkNodes() {
        final LinkedSpecificationItem requirement = titledItem(
                "req~covered_requirement~1",
                "doc/requirements.md",
                "Covered requirement",
                "tst"
        );
        final LinkedSpecificationItem test = titledItem(
                "tst~covered_requirement~1",
                "src/CoveredRequirementTest.java",
                "Covered requirement test"
        );
        requirement.addLinkToItemWithStatus(test, LinkStatus.COVERED_SHALLOW);

        final OftTraceItemNode requirementNode = onlyItem(
                mapper.map(trace(requirement, test)),
                "doc/requirements.md"
        );

        Assertions.assertAll(
                () -> assertThat(requirementNode.name(),
                        is("Covered requirement")),
                () -> assertThat(requirementNode.navigationId(), is("req~covered_requirement~1")),
                () -> assertThat(requirementNode.failed(), is(false)),
                () -> assertThat(requirementNode.details().detailText(),
                        containsString("Specification item ID: req~covered_requirement~1")),
                () -> assertThat(requirementNode.details().detailText(), containsString("Trace status: covered")),
                () -> assertThat(requirementNode.testCount(), is(2)),
                () -> assertThat(requirementNode.links(), hasSize(1)),
                () -> assertThat(requirementNode.links().getFirst().name(),
                        is("⊙← Covered requirement test")),
                () -> assertThat(requirementNode.links().getFirst().navigationId(),
                        is("tst~covered_requirement~1")),
                () -> assertThat(requirementNode.links().getFirst().failed(), is(false)),
                () -> assertThat(requirementNode.links().getFirst().details().detailText(),
                        containsString("Owning item ID: req~covered_requirement~1")),
                () -> assertThat(requirementNode.links().getFirst().details().detailText(),
                        containsString("Linked item ID: tst~covered_requirement~1")),
                () -> assertThat(requirementNode.links().getFirst().details().detailText(),
                        containsString("Direction: incoming")),
                () -> assertThat(requirementNode.links().getFirst().details().detailText(),
                        containsString("Trace-link status: covered"))
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~show-trace-links-as-test-runner-sub-tests~1]
    // [itest->dsn~show-trace-link-direction-in-test-runner-ui~1]
    // [itest->dsn~show-unicode-trace-link-direction-in-test-runner-ui~1]
    @Test
    void testGivenOnlyOutgoingTraceLinkWhenMappingThenItCreatesIncomingLinkOnLinkedItem() {
        final LinkedSpecificationItem implementation = titledItem(
                "impl~covered_requirement~1",
                "src/CoveredRequirement.java",
                "Covered requirement implementation"
        );
        final LinkedSpecificationItem requirement = titledItem(
                "req~covered_requirement~1",
                "doc/requirements.md",
                "Covered requirement"
        );
        implementation.addLinkToItemWithStatus(requirement, LinkStatus.COVERS);

        final OftTraceItemNode requirementNode = onlyItem(
                mapper.map(trace(implementation, requirement)),
                "doc/requirements.md"
        );

        assertThat(
                requirementNode.links().stream()
                        .map(OftTraceLinkNode::name)
                        .toList(),
                contains("⊙← Covered requirement implementation")
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~sort-specification-items-in-test-runner-ui~1]
    @Test
    void testGivenUnsortedSpecificationItemsInOneSourceFileWhenMappingThenItSortsItemsByIdParts() {
        final OftTraceTestTree tree = mapper.map(trace(
                item("req~zeta_requirement~1", "doc/requirements.md"),
                item("impl~zeta_requirement~1", "doc/requirements.md"),
                item("req~alpha_requirement~2", "doc/requirements.md"),
                item("feat~trace_results~1", "doc/requirements.md"),
                item("req~alpha_requirement~1", "doc/requirements.md")
        ));

        assertThat(
                suiteNamed(tree, "doc/requirements.md").items().stream()
                        .map(OftTraceItemNode::name)
                        .toList(),
                contains(
                        "feat~trace_results~1",
                        "impl~zeta_requirement~1",
                        "req~alpha_requirement~1",
                        "req~alpha_requirement~2",
                        "req~zeta_requirement~1"
                )
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~show-trace-links-as-test-runner-sub-tests~1]
    // [itest->dsn~show-trace-link-status-in-test-runner-ui~2]
    // [itest->dsn~show-trace-link-direction-in-test-runner-ui~1]
    // [itest->dsn~show-unicode-trace-link-direction-in-test-runner-ui~1]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [itest->dsn~map-trace-link-status-to-test-runner-status~1]
    // [itest->dsn~roll-up-source-file-suite-trace-status~1]
    // [itest->dsn~roll-up-top-level-trace-status~1]
    // [itest->dsn~show-trace-link-defect-details-in-test-runner-ui~1]
    @Test
    void testGivenOutgoingOrphanedTraceLinkWhenMappingThenItCreatesFailedItemAndLinkNodes() {
        final LinkedSpecificationItem implementation = item("impl~missing_requirement~1", "src/Main.java");
        final LinkedSpecificationItem missingRequirement = item("req~missing_requirement~1", "doc/requirements.md");
        implementation.addLinkToItemWithStatus(missingRequirement, LinkStatus.ORPHANED);

        final OftTraceTestTree tree = mapper.map(trace(implementation));
        final OftTraceItemNode implementationNode = onlyItem(
                tree,
                "src/Main.java"
        );
        final OftTraceLinkNode link = implementationNode.links().getFirst();

        Assertions.assertAll(
                () -> assertThat(tree.failed(), is(true)),
                () -> assertThat(suiteNamed(tree, "src/Main.java").failed(), is(true)),
                () -> assertThat(implementationNode.failed(), is(true)),
                () -> assertThat(implementationNode.links(), hasSize(1)),
                () -> assertThat(link.name(), is("⊙→ req~missing_requirement~1 (orphaned)")),
                () -> assertThat(link.failed(), is(true)),
                () -> assertThat(link.details().failureMessage(), is("Orphaned outgoing trace link.")),
                () -> assertThat(link.details().detailText(), containsString("Owning item ID: impl~missing_requirement~1")),
                () -> assertThat(link.details().detailText(), containsString("Linked item ID: req~missing_requirement~1")),
                () -> assertThat(link.details().detailText(), containsString("Direction: outgoing")),
                () -> assertThat(link.details().detailText(), containsString("OpenFastTrace could not find"))
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    // [itest->dsn~show-specification-item-status-in-test-runner-ui~2]
    // [itest->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [itest->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    @Test
    void testGivenUncoveredRequirementWhenMappingThenItCreatesFailedItemNodeWithUncoveredStatus() {
        final LinkedSpecificationItem requirement = item("req~uncovered_requirement~1", "doc/requirements.md", "dsn");

        final OftTraceItemNode requirementNode = onlyItem(
                mapper.map(trace(requirement)),
                "doc/requirements.md"
        );

        Assertions.assertAll(
                () -> assertThat(requirementNode.name(), is("req~uncovered_requirement~1 (uncovered)")),
                () -> assertThat(requirementNode.failed(), is(true)),
                () -> assertThat(requirementNode.details().failureMessage(),
                        is("Uncovered OpenFastTrace specification item.")),
                () -> assertThat(requirementNode.details().detailText(),
                        containsString("Specification item ID: req~uncovered_requirement~1")),
                () -> assertThat(requirementNode.details().detailText(),
                        containsString("Trace status: uncovered")),
                () -> assertThat(requirementNode.details().detailText(),
                        containsString("Required coverage is missing"))
        );
    }

    private static OftTraceSuiteNode suiteNamed(final OftTraceTestTree tree, final String name) {
        return tree.suites().stream()
                .filter(suite -> suite.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static OftTraceItemNode onlyItem(final OftTraceTestTree tree, final String sourceName) {
        final List<OftTraceItemNode> items = suiteNamed(tree, sourceName).items();
        assertThat(items, hasSize(1));
        return items.getFirst();
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

    private static LinkedSpecificationItem item(
            final String id,
            final String locationPath,
            final String... needsArtifactTypes
    ) {
        return titledItem(id, locationPath, "", needsArtifactTypes);
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
}
