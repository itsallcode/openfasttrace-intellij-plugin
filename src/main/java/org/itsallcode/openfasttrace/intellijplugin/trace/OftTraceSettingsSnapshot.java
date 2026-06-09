package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.util.List;
import java.util.regex.Pattern;

public record OftTraceSettingsSnapshot(
        OftTraceScopeMode scopeMode,
        boolean includeSourceRoots,
        boolean includeTestRoots,
        String additionalPathsText,
        String artifactTypesText,
        String tagsText,
        OftTraceResultView resultView
) {
    public static final OftTraceSettingsSnapshot DEFAULT = new OftTraceSettingsSnapshot(
            OftTraceScopeMode.WHOLE_PROJECT,
            true,
            true,
            "doc/",
            "",
            "",
            OftTraceResultView.PLAIN_TEXT
    );

    private static final Pattern LINE_SEPARATOR = Pattern.compile("\\R");
    public static final Pattern COMMA = Pattern.compile(",");

    public OftTraceSettingsSnapshot(
            final OftTraceScopeMode scopeMode,
            final boolean includeSourceRoots,
            final boolean includeTestRoots,
            final String additionalPathsText,
            final String artifactTypesText,
            final String tagsText
    ) {
        this(
                scopeMode,
                includeSourceRoots,
                includeTestRoots,
                additionalPathsText,
                artifactTypesText,
                tagsText,
                OftTraceResultView.PLAIN_TEXT
        );
    }

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
