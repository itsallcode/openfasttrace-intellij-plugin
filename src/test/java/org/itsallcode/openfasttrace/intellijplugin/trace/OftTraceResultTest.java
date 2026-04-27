package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftTraceResultTest {
    @Test
    void testGivenAllResultKindsThenTheyExposeTheExpectedStatus() {
        assertResult(OftTraceResult.success("ok"), true, false, "OpenFastTrace trace completed successfully.", "ok");
        assertResult(OftTraceResult.failure("defects"), false, true, "OpenFastTrace trace completed with defects.", "defects");
        assertResult(OftTraceResult.error("error"), false, true, "OpenFastTrace trace failed unexpectedly.", "error");
        assertResult(OftTraceResult.invalidInput("invalid"), false, true, "OpenFastTrace trace could not start.", "invalid");
        assertResult(
                OftTraceResult.cancelled(),
                false,
                true,
                "OpenFastTrace trace was cancelled.",
                "OpenFastTrace trace was cancelled."
        );
    }

    private static void assertResult(
            final OftTraceResult result,
            final boolean successful,
            final boolean requiresAttention,
            final String statusMessage,
            final String output
    ) {
        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(successful)),
                () -> assertThat(result.requiresAttention(), is(requiresAttention)),
                () -> assertThat(result.statusMessage(), is(statusMessage)),
                () -> assertThat(result.output(), is(output))
        );
    }
}
