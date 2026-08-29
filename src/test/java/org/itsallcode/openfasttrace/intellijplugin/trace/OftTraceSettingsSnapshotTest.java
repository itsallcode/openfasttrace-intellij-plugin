package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.ItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.List;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OftTraceSettingsSnapshotTest {
    // [utest->dsn~filter-trace-by-item-statuses~1]
    @ParameterizedTest
    @EnumSource(ItemStatus.class)
    void testGivenSingleSelectedStatusWhenCreatingSnapshotThenItPreservesTheStatus(final ItemStatus status) {
        final OftTraceSettingsSnapshot snapshot = snapshot(Set.of(status));

        assertThat(snapshot.selectedStatuses(), is(Set.of(status)));
    }

    // [utest->dsn~reject-run-configuration-without-item-status~1]
    @Test
    void testGivenEmptyStatusSelectionWhenCreatingSnapshotThenItRejectsTheSelection() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> snapshot(Set.of())
        );

        assertThat(exception.getMessage(), containsString("At least one specification item status"));
    }

    // [utest->dsn~reject-run-configuration-without-item-status~1]
    @Test
    void testGivenEmptyStatusSelectionAtExecutionBoundaryWhenCreatingInputsThenItRejectsTypeAndMessage() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OftTraceInputs.wholeProject(Path.of("."), List.of(), List.of(), Set.of())
        );

        assertThat(exception.getMessage(), is("At least one specification item status must be selected."));
    }

    private static OftTraceSettingsSnapshot snapshot(final Set<ItemStatus> statuses) {
        return new OftTraceSettingsSnapshot(
                OftTraceScopeMode.WHOLE_PROJECT,
                true,
                true,
                "doc/",
                "",
                "",
                statuses,
                OftTraceResultView.TEST_RUNNER
        );
    }
}
