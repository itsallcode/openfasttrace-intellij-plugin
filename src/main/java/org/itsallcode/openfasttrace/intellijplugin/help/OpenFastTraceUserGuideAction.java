package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

// [impl->dsn~user-guide-integration~1]
// [impl->dsn~user-guide-action-runtime~1]
public final class OpenFastTraceUserGuideAction extends DumbAwareAction {
    private final OpenFastTraceUserGuideOpener opener;

    public OpenFastTraceUserGuideAction() {
        this(OpenFastTraceUserGuide::open);
    }

    OpenFastTraceUserGuideAction(OpenFastTraceUserGuideOpener opener) {
        this.opener = opener;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();
        if (project != null) {
            opener.open(project, OpenFastTraceUserGuide.TITLE, OpenFastTraceUserGuide.URL);
        }
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
