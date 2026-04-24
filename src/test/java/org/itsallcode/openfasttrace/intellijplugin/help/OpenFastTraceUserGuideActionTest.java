package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class OpenFastTraceUserGuideActionTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~show-oft-user-guide-in-help-menu~1]
    public void testGivenPluginIsLoadedWhenHelpMenuActionIsQueriedThenTheUserGuideActionIsRegistered() {
        assertThat(ActionManager.getInstance().getAction("Oft.OpenUserGuide.HelpMenu") != null, is(true));
    }

    // [itest->dsn~open-oft-user-guide-in-integrated-web-view~1]
    public void testGivenUserInvokesTheHelpActionWhenActionPerformsThenTheUserGuideIsOpened() throws Exception {
        final AtomicReference<ProjectCall> call = new AtomicReference<>();
        final OpenFastTraceUserGuideAction action = new OpenFastTraceUserGuideAction(
                (project, title, url) -> call.set(new ProjectCall(project, title, url))
        );
        final AnActionEvent event = AnActionEvent.createEvent(
                SimpleDataContext.builder()
                        .add(CommonDataKeys.PROJECT, getProject())
                        .build(),
                new Presentation(),
                ActionPlaces.UNKNOWN,
                ActionUiKind.NONE,
                null
        );

        ApplicationManager.getApplication().invokeAndWait(() -> action.actionPerformed(event));

        assertThat(call.get(), is(new ProjectCall(getProject(), OpenFastTraceUserGuide.TITLE, OpenFastTraceUserGuide.URL)));
    }

    public void testGivenNoProjectWhenActionPerformsThenTheUserGuideIsNotOpened() {
        final AtomicReference<ProjectCall> call = new AtomicReference<>();
        final OpenFastTraceUserGuideAction action = new OpenFastTraceUserGuideAction(
                (project, title, url) -> call.set(new ProjectCall(project, title, url))
        );
        final AnActionEvent event = AnActionEvent.createEvent(
                SimpleDataContext.builder().build(),
                new Presentation(),
                ActionPlaces.UNKNOWN,
                ActionUiKind.NONE,
                null
        );

        ApplicationManager.getApplication().invokeAndWait(() -> action.actionPerformed(event));

        assertThat(call.get(), is(nullValue()));
    }

    public void testGivenProjectWhenActionUpdatesThenPresentationIsEnabled() {
        final OpenFastTraceUserGuideAction action = new OpenFastTraceUserGuideAction();
        final Presentation presentation = new Presentation();
        final AnActionEvent event = AnActionEvent.createEvent(
                SimpleDataContext.builder()
                        .add(CommonDataKeys.PROJECT, getProject())
                        .build(),
                presentation,
                ActionPlaces.UNKNOWN,
                ActionUiKind.NONE,
                null
        );

        action.update(event);

        assertThat(presentation.isEnabled(), is(true));
    }

    public void testGivenNoProjectWhenActionUpdatesThenPresentationIsDisabled() {
        final OpenFastTraceUserGuideAction action = new OpenFastTraceUserGuideAction();
        final Presentation presentation = new Presentation();
        final AnActionEvent event = AnActionEvent.createEvent(
                SimpleDataContext.builder().build(),
                presentation,
                ActionPlaces.UNKNOWN,
                ActionUiKind.NONE,
                null
        );

        action.update(event);

        assertThat(presentation.isEnabled(), is(false));
    }

    private record ProjectCall(com.intellij.openapi.project.Project project, String title, String url) {
    }
}
