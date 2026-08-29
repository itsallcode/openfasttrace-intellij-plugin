package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.itsallcode.openfasttrace.api.core.ItemStatus;

public final class OftTraceInputs {
    private final boolean wholeProject;
    private final List<Path> inputPaths;
    private final List<String> artifactTypes;
    private final List<String> tags;
    private final boolean includeUntagged;
    private final Set<ItemStatus> selectedStatuses;

    private OftTraceInputs(
            final boolean wholeProject,
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged,
            final Set<ItemStatus> selectedStatuses
    ) {
        this.wholeProject = wholeProject;
        this.inputPaths = List.copyOf(inputPaths);
        this.artifactTypes = List.copyOf(artifactTypes);
        this.tags = List.copyOf(tags);
        this.includeUntagged = includeUntagged;
        Objects.requireNonNull(selectedStatuses, "selectedStatuses");
        this.selectedStatuses = Set.copyOf(selectedStatuses);
        if (this.selectedStatuses.isEmpty()) {
            throw new IllegalArgumentException("At least one specification item status must be selected.");
        }
    }

    public static OftTraceInputs wholeProject(
            final Path projectRoot,
            final List<String> artifactTypes,
            final List<String> tags
    ) {
        return wholeProject(projectRoot, artifactTypes, tags, false, OftTraceSettingsSnapshot.DEFAULT_STATUSES);
    }

    public static OftTraceInputs wholeProject(
            final Path projectRoot,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged
    ) {
        return wholeProject(projectRoot, artifactTypes, tags, includeUntagged, OftTraceSettingsSnapshot.DEFAULT_STATUSES);
    }

    public static OftTraceInputs wholeProject(
            final Path projectRoot,
            final List<String> artifactTypes,
            final List<String> tags,
            final Set<ItemStatus> selectedStatuses
    ) {
        return wholeProject(projectRoot, artifactTypes, tags, false, selectedStatuses);
    }

    public static OftTraceInputs wholeProject(
            final Path projectRoot,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged,
            final Set<ItemStatus> selectedStatuses
    ) {
        return new OftTraceInputs(true, List.of(projectRoot), artifactTypes, tags, includeUntagged, selectedStatuses);
    }

    public static OftTraceInputs selectedResources(
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags
    ) {
        return selectedResources(inputPaths, artifactTypes, tags, false, OftTraceSettingsSnapshot.DEFAULT_STATUSES);
    }

    public static OftTraceInputs selectedResources(
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged
    ) {
        return selectedResources(inputPaths, artifactTypes, tags, includeUntagged, OftTraceSettingsSnapshot.DEFAULT_STATUSES);
    }

    public static OftTraceInputs selectedResources(
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags,
            final Set<ItemStatus> selectedStatuses
    ) {
        return selectedResources(inputPaths, artifactTypes, tags, false, selectedStatuses);
    }

    public static OftTraceInputs selectedResources(
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged,
            final Set<ItemStatus> selectedStatuses
    ) {
        return new OftTraceInputs(false, inputPaths, artifactTypes, tags, includeUntagged, selectedStatuses);
    }

    public boolean isWholeProject() {
        return wholeProject;
    }

    public List<Path> inputPaths() {
        return inputPaths;
    }

    public List<String> artifactTypes() {
        return artifactTypes;
    }

    public List<String> tags() {
        return tags;
    }

    public boolean includeUntagged() {
        return includeUntagged;
    }

    public Set<ItemStatus> selectedStatuses() {
        return selectedStatuses;
    }

    String progressText() {
        if (wholeProject) {
            return inputPaths.getFirst().toString();
        }
        return inputPaths.size() + " configured trace input(s)";
    }
}
