package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.itsallcode.openfasttrace.api.core.ItemStatus;

public record OftTraceSettingsSnapshot(
        OftTraceScopeMode scopeMode,
        boolean includeSourceRoots,
        boolean includeTestRoots,
        String additionalPathsText,
        String artifactTypesText,
        String tagsText,
        boolean includeUntagged,
        boolean showTransitiveDefects,
        Set<ItemStatus> selectedStatuses,
        OftTraceResultView resultView
) {
    public static final Set<ItemStatus> DEFAULT_STATUSES = Set.of(ItemStatus.APPROVED);
    public static final OftTraceSettingsSnapshot DEFAULT = new OftTraceSettingsSnapshot(
            OftTraceScopeMode.WHOLE_PROJECT,
            true,
            true,
            "doc/",
            "",
            "",
            false,
            true,
            DEFAULT_STATUSES,
            OftTraceResultView.TEST_RUNNER
    );

    private static final Pattern LINE_SEPARATOR = Pattern.compile("\\R");
    public static final Pattern COMMA = Pattern.compile(",");

    public OftTraceSettingsSnapshot {
        Objects.requireNonNull(selectedStatuses, "selectedStatuses");
        selectedStatuses = Set.copyOf(selectedStatuses);
        if (selectedStatuses.isEmpty()) {
            throw new IllegalArgumentException("At least one specification item status must be selected.");
        }
    }

    public OftTraceSettingsSnapshot(
            final OftTraceScopeMode scopeMode,
            final boolean includeSourceRoots,
            final boolean includeTestRoots,
            final String additionalPathsText,
            final String artifactTypesText,
            final String tagsText,
            final Set<ItemStatus> selectedStatuses,
            final OftTraceResultView resultView
    ) {
        this(
                scopeMode,
                includeSourceRoots,
                includeTestRoots,
                additionalPathsText,
                artifactTypesText,
                tagsText,
                false,
                true,
                selectedStatuses,
                resultView
        );
    }

    public OftTraceSettingsSnapshot(
            final OftTraceScopeMode scopeMode,
            final boolean includeSourceRoots,
            final boolean includeTestRoots,
            final String additionalPathsText,
            final String artifactTypesText,
            final String tagsText,
            final OftTraceResultView resultView
    ) {
        this(
                scopeMode,
                includeSourceRoots,
                includeTestRoots,
                additionalPathsText,
                artifactTypesText,
                tagsText,
                DEFAULT_STATUSES,
                resultView
        );
    }

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
                false,
                true,
                DEFAULT_STATUSES,
                DEFAULT.resultView()
        );
    }

    public OftTraceSettingsSnapshot(
            final OftTraceScopeMode scopeMode,
            final boolean includeSourceRoots,
            final boolean includeTestRoots,
            final String additionalPathsText,
            final String artifactTypesText,
            final String tagsText,
            final boolean includeUntagged,
            final boolean showTransitiveDefects,
            final OftTraceResultView resultView
    ) {
        this(
                scopeMode,
                includeSourceRoots,
                includeTestRoots,
                additionalPathsText,
                artifactTypesText,
                tagsText,
                includeUntagged,
                showTransitiveDefects,
                DEFAULT_STATUSES,
                resultView
        );
    }

    public OftTraceSettingsSnapshot(
            final OftTraceScopeMode scopeMode,
            final boolean includeSourceRoots,
            final boolean includeTestRoots,
            final String additionalPathsText,
            final String artifactTypesText,
            final String tagsText,
            final boolean includeUntagged,
            final OftTraceResultView resultView
    ) {
        this(
                scopeMode,
                includeSourceRoots,
                includeTestRoots,
                additionalPathsText,
                artifactTypesText,
                tagsText,
                includeUntagged,
                true,
                DEFAULT_STATUSES,
                resultView
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
