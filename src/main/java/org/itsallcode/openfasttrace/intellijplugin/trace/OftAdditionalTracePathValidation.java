package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class OftAdditionalTracePathValidation {
    private static final Pattern TRACE_INPUT_LIST_DIVIDER = Pattern.compile("\\R");
    private final String resolvedRelativeToText;
    private final List<String> messages;

    private OftAdditionalTracePathValidation(final String resolvedRelativeToText, final List<String> messages) {
        this.resolvedRelativeToText = resolvedRelativeToText;
        this.messages = List.copyOf(messages);
    }

    static OftAdditionalTracePathValidation validate(final Path projectRoot, final String additionalPathsText) {
        final List<String> messages = new ArrayList<>();
        final String[] lines = TRACE_INPUT_LIST_DIVIDER.split(additionalPathsText, -1);
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

    String resolvedRelativeToText() {
        return resolvedRelativeToText;
    }

    List<String> messages() {
        return messages;
    }

    @SuppressWarnings("java:S1166")
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
            // The UI intentionally shows only the validation result, not parser internals from InvalidPathException.
            addValidationErrorMessage(messages, line, lineNumber, "' is not a valid path");
            return;
        }
        if (relativePath.isAbsolute()) {
            addValidationErrorMessage(messages, line, lineNumber, "' must be project-relative");
            return;
        }
        final Path resolvedPath = projectRoot.resolve(relativePath).normalize();
        if (!Files.exists(resolvedPath)) {
            addValidationErrorMessage(messages, line, lineNumber, "' not found");
            return;
        }
        if (!Files.isRegularFile(resolvedPath) && !Files.isDirectory(resolvedPath)) {
            addValidationErrorMessage(messages, line, lineNumber, "' is neither a file nor a directory");
        }
    }

    private static void addValidationErrorMessage(final List<String> messages, final String line, final int lineNumber,
                                                  final String validationError) {
        messages.add("Line " + lineNumber + ": '" + line + validationError);
    }
}
