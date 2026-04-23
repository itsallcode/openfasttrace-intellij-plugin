package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.project.Project;

@FunctionalInterface
interface OpenFastTraceUserGuideHtmlEditorOpener {
    void open(Project project, String title, String url, String timeoutHtml);
}
