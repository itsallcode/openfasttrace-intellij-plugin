package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.Project;

final class OpenFastTraceUserGuide {
    static final String TITLE = "OpenFastTrace User Guide";
    static final String URL = "https://github.com/itsallcode/openfasttrace/blob/main/doc/user_guide.md";

    private OpenFastTraceUserGuide() {
    }

    static void open(Project project) {
        open(project, TITLE, URL, HTMLEditorProvider::openEditor);
    }

    static void open(Project project, String title, String url) {
        open(project, title, url, HTMLEditorProvider::openEditor);
    }

    static void open(Project project, String title, String url, OpenFastTraceUserGuideHtmlEditorOpener opener) {
        opener.open(project, title, url, null);
    }
}
