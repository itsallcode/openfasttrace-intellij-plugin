package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.openapi.project.Project;

final class OpenFastTraceUserGuide {
    static final String TITLE = "OpenFastTrace User Guide";
    static final String URL = "https://openfasttrace.itsallcode.org/user_guide/user_guide.html";

    private OpenFastTraceUserGuide() {
    }

    static void open(final Project project, final String title, final String url) {
        open(project, title, url, HTMLEditorProvider::openEditor);
    }

    static void open(
            final Project project,
            final String title,
            final String url,
            final OpenFastTraceUserGuideHtmlEditorOpener opener
    ) {
        opener.open(project, title, url, null);
    }
}
