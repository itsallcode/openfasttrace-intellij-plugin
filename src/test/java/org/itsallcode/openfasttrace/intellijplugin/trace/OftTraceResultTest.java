package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.Trace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

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

    // [itest->dsn~trace-test-runner-presentation~1]
    @Test
    void testGivenSuccessfulOrDefectiveTraceResultThenItExposesStructuredTraceData() {
        final Trace trace = emptyTrace();

        Assertions.assertAll(
                () -> assertThat(OftTraceResult.success("ok", trace).trace().orElseThrow(), sameInstance(trace)),
                () -> assertThat(OftTraceResult.failure("defects", trace).trace().orElseThrow(), sameInstance(trace))
        );
    }

    // [itest->dsn~trace-test-runner-presentation~1]
    @Test
    void testGivenResultWithoutACompletedTraceThenItDoesNotExposeStructuredTraceData() {
        Assertions.assertAll(
                () -> assertThat(OftTraceResult.success("ok").trace().isEmpty(), is(true)),
                () -> assertThat(OftTraceResult.failure("defects").trace().isEmpty(), is(true)),
                () -> assertThat(OftTraceResult.error("error").trace().isEmpty(), is(true)),
                () -> assertThat(OftTraceResult.invalidInput("invalid").trace().isEmpty(), is(true)),
                () -> assertThat(OftTraceResult.cancelled().trace().isEmpty(), is(true))
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

    private static Trace emptyTrace() {
        return Trace.builder()
                .items(List.of())
                .defectItems(List.of())
                .build();
    }
}
