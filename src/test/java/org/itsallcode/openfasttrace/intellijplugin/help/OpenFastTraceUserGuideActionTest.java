package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// [itest->dsn~user-guide-action-runtime~1]
public class OpenFastTraceUserGuideActionTest extends AbstractOftPlatformTestCase {
    public void testActionOpensUserGuideInHtmlEditor() throws Exception {
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

        final ProjectCall actual = call.get();
        assertThat(actual.project(), is(getProject()));
        assertThat(actual.title(), is(OpenFastTraceUserGuide.TITLE));
        assertThat(actual.url(), is(OpenFastTraceUserGuide.URL));
    }

    private record ProjectCall(com.intellij.openapi.project.Project project, String title, String url) {
    }
}
