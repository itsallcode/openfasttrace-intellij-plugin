package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.nio.file.Path;

final class OftTraceInputResolution {
    private final Path inputPath;
    private final String errorMessage;

    private OftTraceInputResolution(final Path inputPath, final String errorMessage) {
        this.inputPath = inputPath;
        this.errorMessage = errorMessage;
    }

    static OftTraceInputResolution valid(final Path inputPath) {
        return new OftTraceInputResolution(inputPath, null);
    }

    static OftTraceInputResolution invalid(final String errorMessage) {
        return new OftTraceInputResolution(null, errorMessage);
    }

    boolean isValid() {
        return inputPath != null;
    }

    Path inputPath() {
        return inputPath;
    }

    String errorMessage() {
        return errorMessage;
    }
}
