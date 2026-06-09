package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.DeepCoverageStatus;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.LinkStatus;
import org.itsallcode.openfasttrace.api.core.Location;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.api.core.TracedLink;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class OftTraceTestTreeMapper {
    private static final String UNKNOWN_SOURCE = "Unknown source";
    private static final Comparator<LinkedSpecificationItem> ITEM_ORDER = Comparator
            .comparing(LinkedSpecificationItem::getArtifactType)
            .thenComparing(LinkedSpecificationItem::getName)
            .thenComparingInt(LinkedSpecificationItem::getRevision);

    // [impl->dsn~trace-test-runner-presentation~1]
    // [impl->dsn~show-trace-source-files-as-test-runner-suites~1]
    // [impl->dsn~show-trace-specification-items-as-test-runner-tests~1]
    // [impl->dsn~show-specification-item-title-in-test-runner-ui~1]
    // [impl->dsn~sort-specification-items-in-test-runner-ui~1]
    // [impl->dsn~show-trace-links-as-test-runner-sub-tests~1]
    // [impl->dsn~show-specification-item-status-in-test-runner-ui~1]
    // [impl->dsn~show-trace-link-status-in-test-runner-ui~1]
    // [impl->dsn~show-trace-link-direction-in-test-runner-ui~1]
    // [impl->dsn~show-unicode-trace-link-direction-in-test-runner-ui~1]
    // [impl->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [impl->dsn~map-trace-link-status-to-test-runner-status~1]
    // [impl->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    // [impl->dsn~show-trace-link-defect-details-in-test-runner-ui~1]
    // [impl->dsn~navigate-from-test-runner-specification-items~1]
    // [impl->dsn~navigate-from-test-runner-trace-links~1]
    OftTraceTestTree map(final Trace trace) {
        return map(trace, null);
    }

    OftTraceTestTree map(final Trace trace, final String projectBasePath) {
        final Map<String, List<LinkedSpecificationItem>> itemsBySource = new LinkedHashMap<>();
        for (final LinkedSpecificationItem item : trace.getItems()) {
            itemsBySource.computeIfAbsent(sourceName(item, projectBasePath), ignored -> new ArrayList<>())
                    .add(item);
        }
        final List<OftTraceSuiteNode> suites = itemsBySource.entrySet().stream()
                .map(entry -> new OftTraceSuiteNode(entry.getKey(), mapItems(entry.getValue())))
                .toList();
        return new OftTraceTestTree(suites);
    }

    private static List<OftTraceItemNode> mapItems(final List<LinkedSpecificationItem> items) {
        return items.stream()
                .sorted(ITEM_ORDER)
                .map(OftTraceTestTreeMapper::mapItem)
                .toList();
    }

    private static OftTraceItemNode mapItem(final LinkedSpecificationItem item) {
        final String itemStatus = itemStatus(item);
        final String itemId = item.getId().toString();
        return new OftTraceItemNode(
                itemLabel(item) + " (" + itemStatus + ")",
                itemId,
                item.isDefect(),
                item.isDefect()
                        ? OftTraceTestNodeDetails.specificationItemFailure(item, itemStatus)
                        : OftTraceTestNodeDetails.none(),
                item.getTracedLinks().stream()
                        .map(link -> mapLink(item, link))
                        .toList()
        );
    }

    private static OftTraceLinkNode mapLink(final LinkedSpecificationItem owner, final TracedLink link) {
        final LinkedSpecificationItem otherItem = link.getOtherLinkEnd();
        final String otherItemId = otherItem.getId().toString();
        final LinkDirection direction = direction(link);
        final String linkStatus = linkStatus(link.getStatus());
        return new OftTraceLinkNode(
                direction.marker() + " " + itemLabel(otherItem) + " (" + linkStatus + ")",
                otherItemId,
                link.getStatus().isBad(),
                link.getStatus().isBad()
                        ? OftTraceTestNodeDetails.traceLinkFailure(
                                owner.getId().toString(),
                                otherItemId,
                                direction.label(),
                                link.getStatus(),
                                linkStatus
                        )
                        : OftTraceTestNodeDetails.none()
        );
    }

    private static String itemLabel(final LinkedSpecificationItem item) {
        final String itemId = item.getId().toString();
        final String title = item.getTitle();
        return title == null || title.isBlank() ? itemId : title + " \u2014 " + itemId;
    }

    private static String sourceName(final LinkedSpecificationItem item, final String projectBasePath) {
        final Location location = item.getLocation();
        if (location == null || location.getPath() == null || location.getPath().isBlank()) {
            return UNKNOWN_SOURCE;
        }
        return sourcePathLabel(location.getPath(), projectBasePath);
    }

    private static String sourcePathLabel(final String sourcePath, final String projectBasePath) {
        final Path source = pathOrNull(sourcePath);
        if (source == null) {
            return displayPath(sourcePath);
        }
        final Path labelPath = projectLocalPath(source, projectBasePath);
        return displayPath(labelPath.toString());
    }

    private static Path projectLocalPath(final Path source, final String projectBasePath) {
        final Path normalizedSource = source.normalize();
        if (!normalizedSource.isAbsolute() || projectBasePath == null || projectBasePath.isBlank()) {
            return normalizedSource;
        }
        final Path projectBase = pathOrNull(projectBasePath);
        if (projectBase == null) {
            return normalizedSource;
        }
        final Path normalizedProjectBase = projectBase.normalize();
        if (normalizedProjectBase.isAbsolute() && normalizedSource.startsWith(normalizedProjectBase)) {
            return normalizedProjectBase.relativize(normalizedSource);
        }
        return normalizedSource;
    }

    private static Path pathOrNull(final String path) {
        try {
            return Path.of(path);
        } catch (final InvalidPathException exception) {
            return null;
        }
    }

    private static String displayPath(final String path) {
        return path.replace('\\', '/');
    }

    private static String itemStatus(final LinkedSpecificationItem item) {
        if (!item.isDefect()) {
            return "covered";
        }
        if (item.hasDuplicates()) {
            return "duplicate";
        }
        final DeepCoverageStatus deepCoverageStatus = item.getDeepCoverageStatus();
        if (deepCoverageStatus == DeepCoverageStatus.CYCLE) {
            return "cycle";
        }
        if (deepCoverageStatus == DeepCoverageStatus.UNCOVERED) {
            return "uncovered";
        }
        return "defective";
    }

    private static String linkStatus(final LinkStatus status) {
        if (status == LinkStatus.COVERED_SHALLOW) {
            return "covered";
        }
        return status.toString().toLowerCase(Locale.ROOT);
    }

    private static LinkDirection direction(final TracedLink link) {
        if (link.isIncoming()) {
            return new LinkDirection("\u2190", "incoming");
        }
        if (link.isOutgoing()) {
            return new LinkDirection("\u2192", "outgoing");
        }
        return new LinkDirection("\u2194", "non-directional");
    }

    private record LinkDirection(String marker, String label) {
    }
}
