package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.diagnostic.Logger;
import org.itsallcode.openfasttrace.api.core.DeepCoverageStatus;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.LinkStatus;
import org.itsallcode.openfasttrace.api.core.Location;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.api.core.TracedLink;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestTree.OftTraceItemNode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestTree.OftTraceLinkNode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceTestTree.OftTraceSuiteNode;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class OftTraceTestTreeMapper {
    private static final Logger LOG = Logger.getInstance(OftTraceTestTreeMapper.class);
    private static final String UNKNOWN_SOURCE = "Unknown source";
    private static final Comparator<LinkedSpecificationItem> ITEM_ORDER = Comparator
            .comparing(LinkedSpecificationItem::getArtifactType)
            .thenComparing(LinkedSpecificationItem::getName)
            .thenComparingInt(LinkedSpecificationItem::getRevision);
    private static final EnumMap<LinkStatus, LinkStatus> REVERSE_LINK_STATUSES = createReverseLinkStatuses();

    private OftTraceTestTreeMapper() {
    }

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
    static OftTraceTestTree map(final Trace trace) {
        return map(trace, null);
    }

    static OftTraceTestTree map(final Trace trace, final String projectBasePath) {
        final List<TraceItemLinks> visibleLinksByItem = visibleLinksByItem(trace.getItems());
        final Map<String, SourceFileItems> itemsBySource = new LinkedHashMap<>();
        for (final TraceItemLinks itemLinks : visibleLinksByItem) {
            final SourceFileSuite sourceFileSuite = sourceFileSuite(itemLinks.item(), projectBasePath);
            itemsBySource.computeIfAbsent(
                    sourceFileSuite.name(),
                    ignored -> new SourceFileItems(sourceFileSuite.name(), sourceFileSuite.sourcePath())
            ).add(itemLinks);
        }
        final List<OftTraceSuiteNode> suites = itemsBySource.values().stream()
                .map(source -> new OftTraceSuiteNode(
                        source.name(),
                        source.sourcePath(),
                        mapItems(source.items())
                ))
                .toList();
        return new OftTraceTestTree(suites);
    }

    private static List<OftTraceItemNode> mapItems(final List<TraceItemLinks> items) {
        return items.stream()
                .sorted(Comparator.comparing(TraceItemLinks::item, ITEM_ORDER))
                .map(itemLinks -> mapItem(itemLinks.item(), itemLinks.links()))
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

    private static List<TraceItemLinks> visibleLinksByItem(
            final List<LinkedSpecificationItem> items
    ) {
        final List<TraceItemLinks> linksByItem = items.stream()
                .map(TraceItemLinks::new)
                .toList();
        for (final TraceItemLinks ownerLinks : linksByItem) {
            final LinkedSpecificationItem owner = ownerLinks.item();
            for (final TracedLink link : owner.getTracedLinks()) {
                ownerLinks.add(link.getOtherLinkEnd(), link.getStatus());
                findTraceItemLinks(linksByItem, link.getOtherLinkEnd())
                        .ifPresent(otherLinks -> reverseStatus(link.getStatus())
                                .ifPresent(status -> otherLinks.add(owner, status)));
            }
        }
        return linksByItem;
    }

    private static Optional<TraceItemLinks> findTraceItemLinks(
            final List<TraceItemLinks> traceItemLinks,
            final LinkedSpecificationItem item
    ) {
        return traceItemLinks.stream()
                .filter(candidate -> candidate.item() == item)
                .findFirst();
    }

    private static Optional<LinkStatus> reverseStatus(final LinkStatus status) {
        return Optional.ofNullable(REVERSE_LINK_STATUSES.get(status));
    }

    private static EnumMap<LinkStatus, LinkStatus> createReverseLinkStatuses() {
        final EnumMap<LinkStatus, LinkStatus> statuses = new EnumMap<>(LinkStatus.class);
        statuses.put(LinkStatus.COVERS, LinkStatus.COVERED_SHALLOW);
        statuses.put(LinkStatus.COVERED_SHALLOW, LinkStatus.COVERS);
        statuses.put(LinkStatus.PREDATED, LinkStatus.COVERED_PREDATED);
        statuses.put(LinkStatus.COVERED_PREDATED, LinkStatus.PREDATED);
        statuses.put(LinkStatus.OUTDATED, LinkStatus.COVERED_OUTDATED);
        statuses.put(LinkStatus.COVERED_OUTDATED, LinkStatus.OUTDATED);
        statuses.put(LinkStatus.UNWANTED, LinkStatus.COVERED_UNWANTED);
        statuses.put(LinkStatus.COVERED_UNWANTED, LinkStatus.UNWANTED);
        statuses.put(LinkStatus.DUPLICATE, LinkStatus.DUPLICATE);
        return statuses;
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
        return (title == null || title.isBlank()) ? item.getId().toString() : title;
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
            LOG.debug("Ignoring invalid OFT trace source path: " + path, exception);
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
            return new LinkDirection("⊙←", "incoming");
        }
        if (status.isOutgoing()) {
            return new LinkDirection("⊙→", "outgoing");
        }
        return new LinkDirection("↔", "non-directional");
    }

    private record VisibleTraceLink(LinkedSpecificationItem otherItem, LinkStatus status) {
    }

    private record SourceFileSuite(String name, @Nullable String sourcePath) {
    }

    private static final class TraceItemLinks {
        private final LinkedSpecificationItem item;
        private final List<VisibleTraceLink> links = new ArrayList<>();

        private TraceItemLinks(final LinkedSpecificationItem item) {
            this.item = item;
        }

        private LinkedSpecificationItem item() {
            return item;
        }

        private void add(final LinkedSpecificationItem otherItem, final LinkStatus status) {
            final VisibleTraceLink visibleTraceLink = new VisibleTraceLink(otherItem, status);
            if (!links.contains(visibleTraceLink)) {
                links.add(visibleTraceLink);
            }
        }

        private List<VisibleTraceLink> links() {
            return List.copyOf(links);
        }
    }

    private static final class SourceFileItems {
        private final String name;
        private final String sourcePath;
        private final List<TraceItemLinks> items = new ArrayList<>();

        private SourceFileItems(final String name, final String sourcePath) {
            this.name = name;
            this.sourcePath = sourcePath;
        }

        private void add(final TraceItemLinks item) {
            items.add(item);
        }

        private String name() {
            return name;
        }

        private String sourcePath() {
            return sourcePath;
        }

        private List<TraceItemLinks> items() {
            return items;
        }
    }

    private record LinkDirection(String marker, String label) {
    }
}
