package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.ItemStatus;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OftTraceSettingsComponentTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~trace-configuration-integration~3]
    @ParameterizedTest
    @CsvSource({
            "DRAFT, Draft",
            "PROPOSED, Proposed",
            "APPROVED, Approved",
            "REJECTED, Rejected"
    })
    void testGivenStatusCheckboxWhenReadingItsLabelThenItUsesTheStatusDisplayName(
            final ItemStatus status,
            final String label
    ) {
        final OftTraceSettingsComponent component = new OftTraceSettingsComponent(null, true);

        assertThat(component.statusLabel(status), is(label));
    }

    // [itest->dsn~trace-configuration-integration~3]
    @ParameterizedTest
    @CsvSource({
            "DRAFT",
            "PROPOSED",
            "APPROVED",
            "REJECTED"
    })
    void testGivenSelectedStatusWhenReadingSettingsThenItPreservesTheSelection(final ItemStatus selectedStatus) {
        final OftTraceSettingsComponent component = new OftTraceSettingsComponent(null, true);
        Arrays.stream(ItemStatus.values()).forEach(status -> component.setStatusSelected(status, false));
        component.setStatusSelected(selectedStatus, true);

        assertThat(component.getSettings().selectedStatuses(), is(Set.of(selectedStatus)));
    }

    // [itest->dsn~reject-run-configuration-without-item-status~1]
    public void testGivenNoSelectedStatusWhenCheckingSelectionThenItReportsInvalidState() {
        final OftTraceSettingsComponent component = new OftTraceSettingsComponent(null, true);
        Arrays.stream(ItemStatus.values()).forEach(status -> component.setStatusSelected(status, false));

        assertThat(component.hasSelectedStatuses(), is(false));
    }
}
