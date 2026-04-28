package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

import java.util.function.Function;

// [impl->dsn~trace-action-integration~1]
// [impl->dsn~show-trace-project-action-in-tools-menu~1]
// [impl->dsn~disable-trace-project-action-without-open-project~1]
public final class OftTraceProjectAction extends DumbAwareAction {
    public static final String ACTION_ID = "Oft.TraceProject.ToolsMenu";
    public static final String TOOLS_GROUP_ID = "Oft.ToolsMenu";

    private final Function<Project, OftTraceInputResolution> projectInputResolver;
    private final OftTraceRunner traceRunner;
    private final OftTraceOutputPresenter outputPresenter;

    public OftTraceProjectAction() {
        this(
                OftTraceInputResolver::resolve,
                new OftTraceBackgroundRunner(
                        new OftTraceService(),
                        new OftTraceRunContentOutputPresenter()
                ),
                new OftTraceRunContentOutputPresenter()
        );
    }

    OftTraceProjectAction(
            final Function<Project, OftTraceInputResolution> projectInputResolver,
            final OftTraceRunner traceRunner,
            final OftTraceOutputPresenter outputPresenter
    ) {
        this.projectInputResolver = projectInputResolver;
        this.traceRunner = traceRunner;
        this.outputPresenter = outputPresenter;
    }

    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Project project = event.getProject();
        if (project == null) {
            return;
        }
        final String contentTitle = createContentTitle(project);
        final OftTraceInputResolution resolution = projectInputResolver.apply(project);
        if (!resolution.isValid()) {
            // [impl->dsn~reject-trace-project-without-valid-project-path~2]
            outputPresenter.show(project, contentTitle, OftTraceResult.invalidInput(resolution.errorMessage()));
            return;
        }
        traceRunner.run(project, resolution.inputs(), contentTitle);
    }

    @Override
    public void update(final AnActionEvent event) {
        event.getPresentation().setEnabled(event.getProject() != null);
    }

    static String createContentTitle(final Project project) {
        return "OpenFastTrace Trace: " + project.getName();
    }
}
