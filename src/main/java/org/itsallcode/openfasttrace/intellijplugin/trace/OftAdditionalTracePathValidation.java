package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class OftAdditionalTracePathValidation {
    private final String resolvedRelativeToText;
    private final List<String> messages;

    private OftAdditionalTracePathValidation(final String resolvedRelativeToText, final List<String> messages) {
        this.resolvedRelativeToText = resolvedRelativeToText;
        this.messages = List.copyOf(messages);
    }

    static OftAdditionalTracePathValidation validate(final Path projectRoot, final String additionalPathsText) {
        final List<String> messages = new ArrayList<>();
        final String[] lines = additionalPathsText.split("\\R", -1);
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            final String line = lines[lineIndex].trim();
            if (line.isEmpty()) {
                continue;
            }
            validateLine(projectRoot, line, lineIndex + 1, messages);
        }
        return new OftAdditionalTracePathValidation(
                "Resolved relative to: " + projectRoot.toAbsolutePath().normalize(),
                messages
        );
    }

    static OftAdditionalTracePathValidation unavailable() {
        return new OftAdditionalTracePathValidation("", List.of());
    }

    String resolvedRelativeToText() {
        return resolvedRelativeToText;
    }

    List<String> messages() {
        return messages;
    }

    private static void validateLine(
            final Path projectRoot,
            final String line,
            final int lineNumber,
            final List<String> messages
    ) {
        final Path relativePath;
        try {
            relativePath = Path.of(line);
        } catch (final InvalidPathException exception) {
            messages.add("Line " + lineNumber + ": '" + line + "' is not a valid path");
            return;
        }
        if (relativePath.isAbsolute()) {
            messages.add("Line " + lineNumber + ": '" + line + "' must be project-relative");
            return;
        }
        final Path resolvedPath = projectRoot.resolve(relativePath).normalize();
        if (!Files.exists(resolvedPath)) {
            messages.add("Line " + lineNumber + ": '" + line + "' not found");
            return;
        }
        if (!Files.isRegularFile(resolvedPath) && !Files.isDirectory(resolvedPath)) {
            messages.add("Line " + lineNumber + ": '" + line + "' is neither a file nor a directory");
        }
    }
}
