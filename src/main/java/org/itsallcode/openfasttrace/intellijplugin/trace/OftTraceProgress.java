package org.itsallcode.openfasttrace.intellijplugin.trace;

interface OftTraceProgress {
    OftTraceProgress NONE = new OftTraceProgress() {
        @Override
        public void phase(final String text, final double fraction) {
            // empty by intention
        }

        @Override
        public void checkCanceled() {
            // empty by intention
        }
    };

    void phase(String text, double fraction);

    void checkCanceled();
}
