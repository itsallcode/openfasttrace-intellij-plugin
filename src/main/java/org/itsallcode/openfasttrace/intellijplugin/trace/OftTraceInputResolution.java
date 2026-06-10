package org.itsallcode.openfasttrace.intellijplugin.trace;

final class OftTraceInputResolution {
    private final OftTraceInputs inputs;
    private final String errorMessage;

    private OftTraceInputResolution(final OftTraceInputs inputs, final String errorMessage) {
        this.inputs = inputs;
        this.errorMessage = errorMessage;
    }

    static OftTraceInputResolution valid(final OftTraceInputs inputs) {
        return new OftTraceInputResolution(inputs, null);
    }

    static OftTraceInputResolution invalid(final String errorMessage) {
        return new OftTraceInputResolution(null, errorMessage);
    }

    boolean isValid() {
        return inputs != null;
    }

    OftTraceInputs inputs() {
        return inputs;
    }

    String errorMessage() {
        return errorMessage;
    }
}
