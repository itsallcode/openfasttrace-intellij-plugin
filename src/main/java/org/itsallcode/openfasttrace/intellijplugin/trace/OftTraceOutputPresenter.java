package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;

interface OftTraceOutputPresenter {
    void show(Project project, String contentTitle, OftTraceResult result);
}
