package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.Trace;

import java.util.Optional;

public final class OftTraceResult {
    private final Status status;
    private final String output;
    private final Trace trace;

    private OftTraceResult(final Status status, final String output, final Trace trace) {
        this.status = status;
        this.output = output;
        this.trace = trace;
    }

    public static OftTraceResult success(final String output) {
        return success(output, null);
    }

    public static OftTraceResult success(final String output, final Trace trace) {
        return new OftTraceResult(Status.SUCCESS, output, trace);
    }

    public static OftTraceResult failure(final String output) {
        return failure(output, null);
    }

    public static OftTraceResult failure(final String output, final Trace trace) {
        return new OftTraceResult(Status.FAILURE, output, trace);
    }

    public static OftTraceResult error(final String output) {
        return new OftTraceResult(Status.ERROR, output, null);
    }

    public static OftTraceResult invalidInput(final String output) {
        return new OftTraceResult(Status.INVALID_INPUT, output, null);
    }

    public static OftTraceResult cancelled() {
        return new OftTraceResult(Status.CANCELLED, "OpenFastTrace trace was cancelled.", null);
    }

    boolean isSuccessful() {
        return status == Status.SUCCESS;
    }

    boolean requiresAttention() {
        return status != Status.SUCCESS;
    }

    public String statusMessage() {
        return switch (status) {
            case SUCCESS -> "OpenFastTrace trace completed successfully.";
            case FAILURE -> "OpenFastTrace trace completed with defects.";
            case ERROR -> "OpenFastTrace trace failed unexpectedly.";
            case INVALID_INPUT -> "OpenFastTrace trace could not start.";
            case CANCELLED -> "OpenFastTrace trace was cancelled.";
        };
    }

    public String output() {
        return output;
    }

    public Optional<Trace> trace() {
        return Optional.ofNullable(trace);
    }

    private enum Status {
        SUCCESS,
        FAILURE,
        ERROR,
        INVALID_INPUT,
        CANCELLED
    }
}
