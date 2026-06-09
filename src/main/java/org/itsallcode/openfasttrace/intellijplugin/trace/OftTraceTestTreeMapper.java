package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.DeepCoverageStatus;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.LinkStatus;
import org.itsallcode.openfasttrace.api.core.Location;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.api.core.TracedLink;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class OftTraceTestTreeMapper {
    private static final String UNKNOWN_SOURCE = "Unknown source";
    private static final Comparator<LinkedSpecificationItem> ITEM_ORDER = Comparator
            .comparing(LinkedSpecificationItem::getArtifactType)
            .thenComparing(LinkedSpecificationItem::getName)
            .thenComparingInt(LinkedSpecificationItem::getRevision);

    // [impl->dsn~trace-test-runner-presentation~1]
    // [impl->dsn~show-trace-source-files-as-test-runner-suites~1]
    // [impl->dsn~show-trace-specification-items-as-test-runner-tests~1]
    // [impl->dsn~show-specification-item-title-in-test-runner-ui~2]
    // [impl->dsn~show-specification-item-id-in-test-runner-details~1]
    // [impl->dsn~sort-specification-items-in-test-runner-ui~1]
    // [impl->dsn~show-trace-links-as-test-runner-sub-tests~1]
    // [impl->dsn~show-specification-item-status-in-test-runner-ui~2]
    // [impl->dsn~show-trace-link-status-in-test-runner-ui~2]
    // [impl->dsn~show-trace-link-direction-in-test-runner-ui~1]
    // [impl->dsn~show-unicode-trace-link-direction-in-test-runner-ui~1]
    // [impl->dsn~map-specification-item-trace-status-to-test-runner-status~1]
    // [impl->dsn~map-trace-link-status-to-test-runner-status~1]
    // [impl->dsn~show-specification-item-defect-details-in-test-runner-ui~1]
    // [impl->dsn~show-trace-link-defect-details-in-test-runner-ui~1]
    // [impl->dsn~show-trace-link-id-details-in-test-runner-ui~1]
    // [impl->dsn~navigate-from-test-runner-source-files~1]
    // [impl->dsn~navigate-from-test-runner-specification-items~1]
    // [impl->dsn~navigate-from-test-runner-trace-links~1]
    OftTraceTestTree map(final Trace trace) {
        return map(trace, null);
    }

    OftTraceTestTree map(final Trace trace, final String projectBasePath) {
        final Map<LinkedSpecificationItem, List<VisibleTraceLink>> visibleLinksByItem =
                visibleLinksByItem(trace.getItems());
        final Map<String, SourceFileItems> itemsBySource = new LinkedHashMap<>();
        for (final LinkedSpecificationItem item : trace.getItems()) {
            final SourceFileSuite sourceFileSuite = sourceFileSuite(item, projectBasePath);
            itemsBySource.computeIfAbsent(
                    sourceFileSuite.name(),
                    ignored -> new SourceFileItems(sourceFileSuite.name(), sourceFileSuite.sourcePath())
            ).add(item);
        }
        final List<OftTraceSuiteNode> suites = itemsBySource.values().stream()
                .map(source -> new OftTraceSuiteNode(
                        source.name(),
                        source.sourcePath(),
                        mapItems(source.items(), visibleLinksByItem)
                ))
                .toList();
        return new OftTraceTestTree(suites);
    }

    private static List<OftTraceItemNode> mapItems(
            final List<LinkedSpecificationItem> items,
            final Map<LinkedSpecificationItem, List<VisibleTraceLink>> visibleLinksByItem
    ) {
        return items.stream()
                .sorted(ITEM_ORDER)
                .map(item -> mapItem(item, visibleLinksByItem.get(item)))
                .toList();
    }

    private static OftTraceItemNode mapItem(
            final LinkedSpecificationItem item,
            final List<VisibleTraceLink> visibleLinks
    ) {
        final String itemStatus = itemStatus(item);
        final String itemId = item.getId().toString();
        return new OftTraceItemNode(
                nodeName(itemName(item), itemStatus, !item.isDefect()),
                itemId,
                item.isDefect(),
                item.isDefect()
                        ? OftTraceTestNodeDetails.specificationItemFailure(item, itemStatus)
                        : OftTraceTestNodeDetails.specificationItem(item, itemStatus),
                visibleLinks.stream()
                        .map(link -> mapLink(item, link))
                        .toList()
        );
    }

    private static Map<LinkedSpecificationItem, List<VisibleTraceLink>> visibleLinksByItem(
            final List<LinkedSpecificationItem> items
    ) {
        final Set<LinkedSpecificationItem> traceItems = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<LinkedSpecificationItem, Set<VisibleTraceLink>> linkSetsByItem = new LinkedHashMap<>();
        for (final LinkedSpecificationItem item : items) {
            traceItems.add(item);
            linkSetsByItem.put(item, new LinkedHashSet<>());
        }
        for (final LinkedSpecificationItem owner : items) {
            for (final TracedLink link : owner.getTracedLinks()) {
                addVisibleLink(linkSetsByItem, owner, link.getOtherLinkEnd(), link.getStatus());
                if (traceItems.contains(link.getOtherLinkEnd())) {
                    reverseStatus(link.getStatus())
                            .ifPresent(status -> addVisibleLink(linkSetsByItem, link.getOtherLinkEnd(), owner, status));
                }
            }
        }
        final Map<LinkedSpecificationItem, List<VisibleTraceLink>> linksByItem = new LinkedHashMap<>();
        linkSetsByItem.forEach((item, links) -> linksByItem.put(item, List.copyOf(links)));
        return linksByItem;
    }

    private static void addVisibleLink(
            final Map<LinkedSpecificationItem, Set<VisibleTraceLink>> linkSetsByItem,
            final LinkedSpecificationItem owner,
            final LinkedSpecificationItem otherItem,
            final LinkStatus status
    ) {
        linkSetsByItem.get(owner).add(new VisibleTraceLink(otherItem, status));
    }

    private static Optional<LinkStatus> reverseStatus(final LinkStatus status) {
        return switch (status) {
            case COVERS -> Optional.of(LinkStatus.COVERED_SHALLOW);
            case COVERED_SHALLOW -> Optional.of(LinkStatus.COVERS);
            case PREDATED -> Optional.of(LinkStatus.COVERED_PREDATED);
            case COVERED_PREDATED -> Optional.of(LinkStatus.PREDATED);
            case OUTDATED -> Optional.of(LinkStatus.COVERED_OUTDATED);
            case COVERED_OUTDATED -> Optional.of(LinkStatus.OUTDATED);
            case UNWANTED -> Optional.of(LinkStatus.COVERED_UNWANTED);
            case COVERED_UNWANTED -> Optional.of(LinkStatus.UNWANTED);
            case DUPLICATE -> Optional.of(LinkStatus.DUPLICATE);
            case AMBIGUOUS, ORPHANED -> Optional.empty();
        };
    }

    private static OftTraceLinkNode mapLink(final LinkedSpecificationItem owner, final VisibleTraceLink link) {
        final LinkedSpecificationItem otherItem = link.otherItem();
        final String otherItemId = otherItem.getId().toString();
        final LinkDirection direction = direction(link.status());
        final String linkStatus = linkStatus(link.status());
        return new OftTraceLinkNode(
                nodeName(direction.marker() + " " + itemName(otherItem), linkStatus, !link.status().isBad()),
                otherItemId,
                link.status().isBad(),
                link.status().isBad()
                        ? OftTraceTestNodeDetails.traceLinkFailure(
                                owner.getId().toString(),
                                otherItemId,
                                direction.label(),
                                link.status(),
                                linkStatus
                        )
                        : OftTraceTestNodeDetails.traceLink(
                                owner.getId().toString(),
                                otherItemId,
                                direction.label(),
                                linkStatus
                        )
        );
    }

    private static String itemName(final LinkedSpecificationItem item) {
        final String title = item.getTitle();
        return title == null || title.isBlank() ? item.getId().toString() : title;
    }

    private static String nodeName(final String baseName, final String status, final boolean clean) {
        return clean ? baseName : baseName + " (" + status + ")";
    }

    private static SourceFileSuite sourceFileSuite(final LinkedSpecificationItem item, final String projectBasePath) {
        final Location location = item.getLocation();
        if (location == null || location.getPath() == null || location.getPath().isBlank()) {
            return new SourceFileSuite(UNKNOWN_SOURCE, null);
        }
        return sourcePathLabel(location.getPath(), projectBasePath);
    }

    private static SourceFileSuite sourcePathLabel(final String sourcePath, final String projectBasePath) {
        final Path source = pathOrNull(sourcePath);
        if (source == null) {
            return new SourceFileSuite(displayPath(sourcePath), null);
        }
        final Path labelPath = projectLocalPath(source, projectBasePath);
        return new SourceFileSuite(displayPath(labelPath.toString()), displayPath(source.normalize().toString()));
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

    private static LinkDirection direction(final LinkStatus status) {
        if (status.isIncoming()) {
            return new LinkDirection("\u2190", "incoming");
        }
        if (status.isOutgoing()) {
            return new LinkDirection("\u2192", "outgoing");
        }
        return new LinkDirection("\u2194", "non-directional");
    }

    private record VisibleTraceLink(LinkedSpecificationItem otherItem, LinkStatus status) {
    }

    private record SourceFileSuite(String name, @Nullable String sourcePath) {
    }

    private static final class SourceFileItems {
        private final String name;
        private final String sourcePath;
        private final List<LinkedSpecificationItem> items = new ArrayList<>();

        private SourceFileItems(final String name, final String sourcePath) {
            this.name = name;
            this.sourcePath = sourcePath;
        }

        private void add(final LinkedSpecificationItem item) {
            items.add(item);
        }

        private String name() {
            return name;
        }

        private String sourcePath() {
            return sourcePath;
        }

        private List<LinkedSpecificationItem> items() {
            return items;
        }
    }

    private record LinkDirection(String marker, String label) {
    }
}
