package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.process.NopProcessHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

import java.util.function.Function;

// [impl->dsn~trace-action-integration~2]
// [impl->dsn~show-trace-project-action-in-tools-menu~1]
// [impl->dsn~disable-trace-project-action-without-open-project~1]
// [impl->dsn~show-trace-project-in-test-runner-ui-by-default~1]
public final class OftTraceProjectAction extends DumbAwareAction {
    public static final String ACTION_ID = "Oft.TraceProject.ToolsMenu";
    public static final String TOOLS_GROUP_ID = "Oft.ToolsMenu";

    private final Function<Project, OftTraceInputResolution> projectInputResolver;
    private final OftTraceRunner traceRunner;
    private final OftTraceOutputPresenter outputPresenter;

    public OftTraceProjectAction() {
        this(defaultDependencies());
    }

    private OftTraceProjectAction(final DefaultDependencies dependencies) {
        this(
                OftTraceInputResolver::resolve,
                dependencies.traceRunner(),
                dependencies.outputPresenter()
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

    static OftTraceOutputPresenter createDefaultOutputPresenter() {
        return new OftTraceTestRunnerRunContentOutputPresenter();
    }

    private static DefaultDependencies defaultDependencies() {
        final OftTraceOutputPresenter presenter = createDefaultOutputPresenter();
        return new DefaultDependencies(
                new OftTraceBackgroundRunner(
                        new OftTraceService(),
                        presenter,
                        new NopProcessHandler()
                ),
                presenter
        );
    }

    private record DefaultDependencies(OftTraceRunner traceRunner, OftTraceOutputPresenter outputPresenter) {
    }
}
