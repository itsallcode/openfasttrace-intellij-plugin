package org.itsallcode.openfasttrace.intellijplugin.trace;

final class OftTraceResult {
    private final Status status;
    private final String output;

    private OftTraceResult(final Status status, final String output) {
        this.status = status;
        this.output = output;
    }

    static OftTraceResult success(final String output) {
        return new OftTraceResult(Status.SUCCESS, output);
    }

    static OftTraceResult failure(final String output) {
        return new OftTraceResult(Status.FAILURE, output);
    }

    static OftTraceResult error(final String output) {
        return new OftTraceResult(Status.ERROR, output);
    }

    static OftTraceResult invalidInput(final String output) {
        return new OftTraceResult(Status.INVALID_INPUT, output);
    }

    static OftTraceResult cancelled() {
        return new OftTraceResult(Status.CANCELLED, "OpenFastTrace trace was cancelled.");
    }

    boolean isSuccessful() {
        return status == Status.SUCCESS;
    }

    boolean requiresAttention() {
        return status != Status.SUCCESS;
    }

    String statusMessage() {
        return switch (status) {
            case SUCCESS -> "OpenFastTrace trace completed successfully.";
            case FAILURE -> "OpenFastTrace trace completed with defects.";
            case ERROR -> "OpenFastTrace trace failed unexpectedly.";
            case INVALID_INPUT -> "OpenFastTrace trace could not start.";
            case CANCELLED -> "OpenFastTrace trace was cancelled.";
        };
    }

    String output() {
        return output;
    }

    private enum Status {
        SUCCESS,
        FAILURE,
        ERROR,
        INVALID_INPUT,
        CANCELLED
    }
}
