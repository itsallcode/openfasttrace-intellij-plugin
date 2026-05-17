package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.util.List;
import java.util.regex.Pattern;

public record OftTraceSettingsSnapshot(
        OftTraceScopeMode scopeMode,
        boolean includeSourceRoots,
        boolean includeTestRoots,
        String additionalPathsText,
        String artifactTypesText,
        String tagsText
) {
    public static final OftTraceSettingsSnapshot DEFAULT = new OftTraceSettingsSnapshot(
            OftTraceScopeMode.WHOLE_PROJECT,
            true,
            true,
            "doc/",
            "",
            ""
    );

    private static final Pattern LINE_SEPARATOR = Pattern.compile("\\R");
    public static final Pattern COMMA = Pattern.compile(",");

    public List<String> additionalPaths() {
        return LINE_SEPARATOR.splitAsStream(additionalPathsText)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    public List<String> artifactTypes() {
        return splitCommaSeparated(artifactTypesText);
    }

    public List<String> tags() {
        return splitCommaSeparated(tagsText);
    }

    private static List<String> splitCommaSeparated(final String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return COMMA.splitAsStream(text)
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
