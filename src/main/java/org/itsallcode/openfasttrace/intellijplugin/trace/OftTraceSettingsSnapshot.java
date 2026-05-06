package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.util.List;
import java.util.regex.Pattern;

record OftTraceSettingsSnapshot(
        OftTraceScopeMode scopeMode,
        boolean includeSourceRoots,
        boolean includeTestRoots,
        String additionalPathsText
) {
    private static final Pattern LINE_SEPARATOR = Pattern.compile("\\R");

    List<String> additionalPaths() {
        return LINE_SEPARATOR.splitAsStream(additionalPathsText)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }
}
