package org.itsallcode.openfasttrace.intellijplugin.trace;

public final class OftTraceInputResolution {
    private final OftTraceInputs inputs;
    private final String errorMessage;

    private OftTraceInputResolution(final OftTraceInputs inputs, final String errorMessage) {
        this.inputs = inputs;
        this.errorMessage = errorMessage;
    }

    public static OftTraceInputResolution valid(final OftTraceInputs inputs) {
        return new OftTraceInputResolution(inputs, null);
    }

    public static OftTraceInputResolution invalid(final String errorMessage) {
        return new OftTraceInputResolution(null, errorMessage);
    }

    public boolean isValid() {
        return inputs != null;
    }

    public OftTraceInputs inputs() {
        return inputs;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
