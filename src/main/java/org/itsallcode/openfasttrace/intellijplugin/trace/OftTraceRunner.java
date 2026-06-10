package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;

interface OftTraceRunner {
    void run(final Project project, final OftTraceInputs inputs, final String contentTitle);
}
