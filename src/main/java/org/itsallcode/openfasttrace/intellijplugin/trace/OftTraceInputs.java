package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.nio.file.Path;
import java.util.List;

final class OftTraceInputs {
    private final boolean wholeProject;
    private final List<Path> inputPaths;

    private OftTraceInputs(final boolean wholeProject, final List<Path> inputPaths) {
        this.wholeProject = wholeProject;
        this.inputPaths = List.copyOf(inputPaths);
    }

    static OftTraceInputs wholeProject(final Path projectRoot) {
        return new OftTraceInputs(true, List.of(projectRoot));
    }

    static OftTraceInputs selectedResources(final List<Path> inputPaths) {
        return new OftTraceInputs(false, inputPaths);
    }

    boolean isWholeProject() {
        return wholeProject;
    }

    List<Path> inputPaths() {
        return inputPaths;
    }

    String progressText() {
        if (wholeProject) {
            return inputPaths.getFirst().toString();
        }
        return inputPaths.size() + " configured trace input(s)";
    }
}
