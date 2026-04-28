package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.util.Arrays;
import java.util.List;

record OftTraceSettingsSnapshot(
        OftTraceScopeMode scopeMode,
        boolean includeSourceRoots,
        boolean includeTestRoots,
        String additionalPathsText
) {
    List<String> additionalPaths() {
        return Arrays.stream(additionalPathsText.split("\\R", -1))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }
}
