package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.DeepCoverageStatus;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.LinkStatus;
import org.itsallcode.openfasttrace.api.core.TracedLink;

import java.util.EnumMap;
import java.util.List;

record OftTraceTestNodeDetails(String failureMessage, String detailText) {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final OftTraceTestNodeDetails NONE = new OftTraceTestNodeDetails("", "");
    private static final OftTraceTestNodeDetails TOP_LEVEL_FAILURE = new OftTraceTestNodeDetails(
            "OpenFastTrace trace contains defects.",
            "At least one source-file suite contains a failed specification item or trace link."
    );
    private static final EnumMap<LinkStatus, LinkDetailTemplate> LINK_DETAIL_TEMPLATES =
            createLinkDetailTemplates();

    static OftTraceTestNodeDetails none() {
        return NONE;
    }

    static OftTraceTestNodeDetails topLevelFailure() {
        return TOP_LEVEL_FAILURE;
    }

    static OftTraceTestNodeDetails sourceSuiteFailure(final String sourceName) {
        return new OftTraceTestNodeDetails(
                "OpenFastTrace defects in " + sourceName + ".",
                "At least one specification item or trace link from this source file is failed."
        );
    }

    // [impl->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    static OftTraceTestNodeDetails specificationItemFailure(
            final LinkedSpecificationItem item,
            final String visibleStatus
    ) {
        return new OftTraceTestNodeDetails(
                itemFailureMessage(item, visibleStatus),
                "Specification item: " + item.getId()
                        + LINE_SEPARATOR
                        + "Trace status: " + visibleStatus
                        + LINE_SEPARATOR
                        + itemFailureExplanation(item, visibleStatus)
        );
    }

    // [impl->dsn~show-trace-link-defect-details-in-test-runner-ui~1]
    static OftTraceTestNodeDetails traceLinkFailure(
            final String owningItemId,
            final String linkedItemId,
            final String directionLabel,
            final LinkStatus linkStatus,
            final String visibleStatus
    ) {
        return LINK_DETAIL_TEMPLATES.get(linkStatus)
                .create(owningItemId, linkedItemId, directionLabel, visibleStatus);
    }

    static OftTraceTestNodeDetails resultWithoutTrace(final OftTraceResult result) {
        return new OftTraceTestNodeDetails(
                result.statusMessage(),
                result.output()
        );
    }

    private static String itemFailureMessage(final LinkedSpecificationItem item, final String visibleStatus) {
        return switch (visibleStatus) {
            case "duplicate" -> "Duplicate OpenFastTrace specification item.";
            case "cycle" -> "OpenFastTrace coverage cycle.";
            case "uncovered" -> "Uncovered OpenFastTrace specification item.";
            default -> "Defective OpenFastTrace specification item " + item.getId() + ".";
        };
    }

    private static String itemFailureExplanation(final LinkedSpecificationItem item, final String visibleStatus) {
        return switch (visibleStatus) {
            case "duplicate" -> "The trace contains more than one specification item with this ID.";
            case "cycle" -> "Deep coverage cannot be proven because the coverage graph contains a cycle.";
            case "uncovered" -> uncoveredExplanation(item);
            default -> defectiveLinksExplanation(item);
        };
    }

    private static String defectiveLinksExplanation(final LinkedSpecificationItem item) {
        final List<String> defectiveLinks = item.getTracedLinks().stream()
                .filter(link -> link.getStatus().isBad())
                .map(OftTraceTestNodeDetails::defectiveLinkDescription)
                .toList();
        if (defectiveLinks.isEmpty()) {
            return "OpenFastTrace reported one or more trace defects on this item.";
        }
        return "OpenFastTrace reported defective trace link(s): "
                + String.join("; ", defectiveLinks)
                + ".";
    }

    private static String defectiveLinkDescription(final TracedLink link) {
        return link.getStatus() + " link to " + link.getOtherLinkEnd().getId();
    }

    private static String uncoveredExplanation(final LinkedSpecificationItem item) {
        final List<String> uncoveredArtifactTypes = item.getUncoveredArtifactTypes();
        if (uncoveredArtifactTypes.isEmpty() || item.getDeepCoverageStatus() != DeepCoverageStatus.UNCOVERED) {
            return "Required coverage is missing for this specification item.";
        }
        return "Required coverage is missing for artifact type(s): "
                + String.join(", ", uncoveredArtifactTypes)
                + ".";
    }

    private static EnumMap<LinkStatus, LinkDetailTemplate> createLinkDetailTemplates() {
        final EnumMap<LinkStatus, LinkDetailTemplate> templates = new EnumMap<>(LinkStatus.class);
        templates.put(LinkStatus.COVERS, new LinkDetailTemplate(
                "Valid outgoing trace link.",
                "The owning specification item covers the linked item."
        ));
        templates.put(LinkStatus.COVERED_SHALLOW, new LinkDetailTemplate(
                "Valid incoming trace link.",
                "The linked item covers the owning specification item."
        ));
        templates.put(LinkStatus.PREDATED, new LinkDetailTemplate(
                "Predated outgoing trace link.",
                "The owning item covers a newer revision than the linked item in the trace."
        ));
        templates.put(LinkStatus.OUTDATED, new LinkDetailTemplate(
                "Outdated outgoing trace link.",
                "The owning item covers an older revision than the linked item in the trace."
        ));
        templates.put(LinkStatus.AMBIGUOUS, new LinkDetailTemplate(
                "Ambiguous outgoing trace link.",
                "The target item ID is ambiguous because the trace contains more than one matching item."
        ));
        templates.put(LinkStatus.UNWANTED, new LinkDetailTemplate(
                "Unwanted outgoing trace link.",
                "The owning item covers an item that does not require coverage."
        ));
        templates.put(LinkStatus.ORPHANED, new LinkDetailTemplate(
                "Orphaned outgoing trace link.",
                "The owning item covers an item that OpenFastTrace could not find."
        ));
        templates.put(LinkStatus.COVERED_UNWANTED, new LinkDetailTemplate(
                "Unwanted incoming trace link.",
                "The owning item is covered although it does not require coverage."
        ));
        templates.put(LinkStatus.COVERED_PREDATED, new LinkDetailTemplate(
                "Predated incoming trace link.",
                "The linked item covers a newer revision than the owning item in the trace."
        ));
        templates.put(LinkStatus.COVERED_OUTDATED, new LinkDetailTemplate(
                "Outdated incoming trace link.",
                "The linked item covers an older revision than the owning item in the trace."
        ));
        templates.put(LinkStatus.DUPLICATE, new LinkDetailTemplate(
                "Duplicate trace link.",
                "The trace contains duplicate specification items with the same ID."
        ));
        return templates;
    }

    private record LinkDetailTemplate(String failureMessage, String explanation) {
        private OftTraceTestNodeDetails create(
                final String owningItemId,
                final String linkedItemId,
                final String directionLabel,
                final String visibleStatus
        ) {
            return new OftTraceTestNodeDetails(
                    failureMessage,
                    "Owning item: " + owningItemId
                            + LINE_SEPARATOR
                            + "Linked item: " + linkedItemId
                            + LINE_SEPARATOR
                            + "Direction: " + directionLabel
                            + LINE_SEPARATOR
                            + "Trace-link status: " + visibleStatus
                            + LINE_SEPARATOR
                            + explanation
            );
        }
    }
}
