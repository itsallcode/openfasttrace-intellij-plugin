package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.nio.file.Path;
import java.util.List;

public final class OftTraceInputs {
    private final boolean wholeProject;
    private final List<Path> inputPaths;
    private final List<String> artifactTypes;
    private final List<String> tags;
    private final boolean includeUntagged;

    private OftTraceInputs(
            final boolean wholeProject,
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged
    ) {
        this.wholeProject = wholeProject;
        this.inputPaths = List.copyOf(inputPaths);
        this.artifactTypes = List.copyOf(artifactTypes);
        this.tags = List.copyOf(tags);
        this.includeUntagged = includeUntagged;
    }

    public static OftTraceInputs wholeProject(
            final Path projectRoot,
            final List<String> artifactTypes,
            final List<String> tags
    ) {
        return new OftTraceInputs(true, List.of(projectRoot), artifactTypes, tags, false);
    }

    public static OftTraceInputs wholeProject(
            final Path projectRoot,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged
    ) {
        return new OftTraceInputs(true, List.of(projectRoot), artifactTypes, tags, includeUntagged);
    }

    public static OftTraceInputs selectedResources(
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags
    ) {
        return new OftTraceInputs(false, inputPaths, artifactTypes, tags, false);
    }

    public static OftTraceInputs selectedResources(
            final List<Path> inputPaths,
            final List<String> artifactTypes,
            final List<String> tags,
            final boolean includeUntagged
    ) {
        return new OftTraceInputs(false, inputPaths, artifactTypes, tags, includeUntagged);
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

    String progressText() {
        if (wholeProject) {
            return inputPaths.getFirst().toString();
        }
        return inputPaths.size() + " configured trace input(s)";
    }
}
