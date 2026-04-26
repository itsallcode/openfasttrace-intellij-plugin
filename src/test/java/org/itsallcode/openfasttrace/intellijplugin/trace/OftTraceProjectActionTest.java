package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.junit.jupiter.api.Assertions;

import javax.swing.KeyStroke;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class OftTraceProjectActionTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~show-trace-project-action-in-tools-menu~1]
    public void testGivenPluginIsLoadedWhenToolsMenuActionIsQueriedThenTheTraceActionAndGroupAreRegistered() {
        final AnAction group = ActionManager.getInstance().getAction(OftTraceProjectAction.TOOLS_GROUP_ID);
        final AnAction action = ActionManager.getInstance().getAction(OftTraceProjectAction.ACTION_ID);

        Assertions.assertAll(
                () -> assertThat(group, notNullValue()),
                () -> assertThat(action, notNullValue()),
                () -> assertThat(group, instanceOf(com.intellij.openapi.actionSystem.DefaultActionGroup.class)),
                () -> assertThat(((com.intellij.openapi.actionSystem.DefaultActionGroup) group).isPopup(), is(true)),
                () -> assertThat(
                        Arrays.asList(
                                ((com.intellij.openapi.actionSystem.DefaultActionGroup) group)
                                        .getChildren((AnActionEvent) null)
                        ),
                        contains(action)
                )
        );
    }

    public void testGivenTraceActionShortcutWhenQueryingKeymapThenItIsBoundOnlyToTheTraceAction() {
        final Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
        final KeyStroke keyStroke = KeyStroke.getKeyStroke("ctrl alt shift O");
        final String[] actionIds = keymap.getActionIds(keyStroke);

        Assertions.assertAll(
                () -> assertThat(keymap.getShortcuts(OftTraceProjectAction.ACTION_ID).length, is(1)),
                () -> assertThat(actionIds, hasItemInArray(OftTraceProjectAction.ACTION_ID)),
                () -> assertThat(actionIds.length, is(1))
        );
    }

    public void testGivenProjectWhenActionUpdatesThenPresentationIsEnabled() {
        final OftTraceProjectAction action = new OftTraceProjectAction();
        final Presentation presentation = new Presentation();

        action.update(createEvent(getProject(), presentation));

        assertThat(presentation.isEnabled(), is(true));
    }

    // [itest->dsn~disable-trace-project-action-without-open-project~1]
    public void testGivenNoProjectWhenActionUpdatesThenPresentationIsDisabled() {
        final OftTraceProjectAction action = new OftTraceProjectAction();
        final Presentation presentation = new Presentation();

        action.update(createEvent(null, presentation));

        assertThat(presentation.isEnabled(), is(false));
    }

    // [itest->dsn~run-trace-project-in-background~1]
    public void testGivenProjectWhenActionPerformsThenItStartsTheTraceRunnerWithTheProjectBasePath() throws IOException {
        final AtomicReference<RunnerCall> runnerCall = new AtomicReference<>();
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final Path projectRoot = createManagedTempDirectory("oft-trace-action-project");
        final Project project = projectProxy(projectRoot.toString(), "valid-project");
        final OftTraceProjectAction action = new OftTraceProjectAction(
                testProject -> OftTraceInputResolver.resolve(testProject.getBasePath()),
                (runnerProject, inputPath, contentTitle) ->
                        runnerCall.set(new RunnerCall(runnerProject, inputPath, contentTitle)),
                (presentedProject, contentTitle, result) ->
                        presenterCall.set(new PresenterCall(presentedProject, contentTitle, result))
        );

        ApplicationManager.getApplication().invokeAndWait(() -> action.actionPerformed(createEvent(project)));

        Assertions.assertAll(
                () -> assertThat(runnerCall.get(), notNullValue()),
                () -> assertThat(runnerCall.get().project(), sameInstance(project)),
                () -> assertThat(runnerCall.get().inputPath(), is(projectRoot)),
                () -> assertThat(
                        runnerCall.get().contentTitle(),
                        is(OftTraceProjectAction.createContentTitle(project))
                ),
                () -> assertThat(presenterCall.get(), is(nullValue()))
        );
    }

    // [itest->dsn~reject-trace-project-without-valid-project-path~1]
    public void testGivenInvalidProjectBasePathWhenActionPerformsThenItReportsTheStartupFailureThroughThePresenter() {
        final AtomicReference<RunnerCall> runnerCall = new AtomicReference<>();
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final Project project = projectProxy("/definitely/missing/openfasttrace/project", "invalid-project");
        final OftTraceProjectAction action = new OftTraceProjectAction(
                testProject -> OftTraceInputResolver.resolve(testProject.getBasePath()),
                (runProject, inputPath, contentTitle) -> runnerCall.set(new RunnerCall(runProject, inputPath, contentTitle)),
                (presentedProject, contentTitle, result) ->
                        presenterCall.set(new PresenterCall(presentedProject, contentTitle, result))
        );

        ApplicationManager.getApplication().invokeAndWait(() -> action.actionPerformed(createEvent(project)));

        Assertions.assertAll(
                () -> assertThat(runnerCall.get(), is(nullValue())),
                () -> assertThat(presenterCall.get(), notNullValue()),
                () -> assertThat(presenterCall.get().project(), sameInstance(project)),
                () -> assertThat(presenterCall.get().contentTitle(), is("OpenFastTrace Trace: invalid-project")),
                () -> assertThat(presenterCall.get().result().requiresAttention(), is(true)),
                () -> assertThat(
                        presenterCall.get().result().output(),
                        Matchers.containsString("does not exist")
                )
        );
    }

    public void testGivenNoProjectWhenActionPerformsThenItDoesNothing() {
        final AtomicReference<RunnerCall> runnerCall = new AtomicReference<>();
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final OftTraceProjectAction action = new OftTraceProjectAction(
                testProject -> OftTraceInputResolver.resolve(testProject.getBasePath()),
                (project, inputPath, contentTitle) -> runnerCall.set(new RunnerCall(project, inputPath, contentTitle)),
                (project, contentTitle, result) -> presenterCall.set(new PresenterCall(project, contentTitle, result))
        );

        ApplicationManager.getApplication().invokeAndWait(() -> action.actionPerformed(createEvent(null)));

        Assertions.assertAll(
                () -> assertThat(runnerCall.get(), is(nullValue())),
                () -> assertThat(presenterCall.get(), is(nullValue()))
        );
    }

    private AnActionEvent createEvent(final Project project) {
        return createEvent(project, new Presentation());
    }

    private AnActionEvent createEvent(final Project project, final Presentation presentation) {
        final SimpleDataContext.Builder builder = SimpleDataContext.builder();
        if (project != null) {
            builder.add(CommonDataKeys.PROJECT, project);
        }
        return AnActionEvent.createEvent(
                builder.build(),
                presentation,
                ActionPlaces.UNKNOWN,
                ActionUiKind.NONE,
                null
        );
    }

    private Project projectProxy(final String basePath, final String name) {
        return (Project) Proxy.newProxyInstance(
                Project.class.getClassLoader(),
                new Class<?>[]{Project.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBasePath" -> basePath;
                    case "getName" -> name;
                    case "isDisposed" -> false;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "Project[" + name + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private Path createManagedTempDirectory(final String directoryName) throws IOException {
        return Files.createDirectories(Path.of(myFixture.getTempDirFixture().getTempDirPath()).resolve(directoryName));
    }

    private record RunnerCall(Project project, Path inputPath, String contentTitle) {
    }

    private record PresenterCall(Project project, String contentTitle, OftTraceResult result) {
    }
}
