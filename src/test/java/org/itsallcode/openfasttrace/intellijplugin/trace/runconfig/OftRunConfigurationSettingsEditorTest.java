package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResultView;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.itsallcode.openfasttrace.api.core.ItemStatus;
import com.intellij.openapi.options.ConfigurationException;
import org.junit.jupiter.api.Assertions;

import javax.swing.JComponent;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OftRunConfigurationSettingsEditorTest extends AbstractOftPlatformTestCase {
    private OftRunConfigurationSettingsEditor editor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editor = new OftRunConfigurationSettingsEditor(getProject());
    }

    public void testGivenEditorWhenCreatingEditorThenItReturnsNonNullComponent() {
        final JComponent component = editor.createEditor();
        assertThat(component, is(notNullValue()));
    }

    // [itest->dsn~select-test-runner-trace-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenEditorWhenResettingFromConfigurationThenItUpdatesUI() {
        editor.createEditor(); // Initialize component
        final OftRunConfiguration configuration = createConfiguration("Test");
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                true,
                "additional",
                "dsn",
                "mvp",
                true,
                false,
                OftTraceResultView.TEST_RUNNER
        );
        configuration.updateFrom(snapshot);

        editor.resetEditorFrom(configuration);

        // Verify UI matches snapshot
        final OftTraceSettingsSnapshot uiSettings = editor.component.getSettings();
        Assertions.assertAll(
                () -> assertThat(uiSettings.scopeMode(), is(snapshot.scopeMode())),
                () -> assertThat(uiSettings.includeSourceRoots(), is(snapshot.includeSourceRoots())),
                () -> assertThat(uiSettings.includeTestRoots(), is(snapshot.includeTestRoots())),
                () -> assertThat(uiSettings.additionalPathsText(), is(snapshot.additionalPathsText())),
                () -> assertThat(uiSettings.artifactTypesText(), is(snapshot.artifactTypesText())),
                () -> assertThat(uiSettings.tagsText(), is(snapshot.tagsText())),
                () -> assertThat(uiSettings.includeUntagged(), is(snapshot.includeUntagged())),
                () -> assertThat(uiSettings.showTransitiveDefects(), is(snapshot.showTransitiveDefects())),
                () -> assertThat(uiSettings.resultView(), is(snapshot.resultView()))
        );
    }

    // [itest->dsn~select-test-runner-trace-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenEditorWhenApplyingToConfigurationThenItUpdatesConfiguration() throws ConfigurationException {
        editor.createEditor(); // Initialize component
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                true,
                false,
                "more paths",
                "req",
                "tag",
                true,
                false,
                OftTraceResultView.TEST_RUNNER
        );
        editor.component.setSettings(snapshot);

        final OftRunConfiguration configuration = createConfiguration("Test");
        editor.applyEditorTo(configuration);

        final OftTraceSettingsSnapshot stored = configuration.snapshot();
        Assertions.assertAll(
                () -> assertThat(stored.scopeMode(), is(snapshot.scopeMode())),
                () -> assertThat(stored.includeSourceRoots(), is(snapshot.includeSourceRoots())),
                () -> assertThat(stored.includeTestRoots(), is(snapshot.includeTestRoots())),
                () -> assertThat(stored.additionalPathsText(), is(snapshot.additionalPathsText())),
                () -> assertThat(stored.artifactTypesText(), is(snapshot.artifactTypesText())),
                () -> assertThat(stored.tagsText(), is(snapshot.tagsText())),
                () -> assertThat(stored.includeUntagged(), is(snapshot.includeUntagged())),
                () -> assertThat(stored.showTransitiveDefects(), is(snapshot.showTransitiveDefects())),
                () -> assertThat(stored.resultView(), is(snapshot.resultView()))
        );
    }

    // [itest->dsn~reject-run-configuration-without-item-status~1]
    public void testGivenNoSelectedStatusWhenApplyingEditorThenItReportsTypeAndMessage() {
        editor.createEditor();
        Arrays.stream(ItemStatus.values()).forEach(status -> editor.component.setStatusSelected(status, false));

        final ConfigurationException exception = Assertions.assertThrows(
                ConfigurationException.class,
                () -> editor.applyEditorTo(createConfiguration("Test"))
        );

        assertThat(exception.getMessage(), is(OftRunConfiguration.STATUS_SELECTION_ERROR));
    }

    // [itest->dsn~show-per-line-validation-for-additional-trace-paths~1]
    public void testGivenEditorWithMissingAdditionalPathWhenUpdatingSettingsThenItShowsPerLineValidation() {
        editor.createEditor(); // Initialize component
        editor.component.setSettings(new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                true,
                true,
                "missing",
                "",
                "",
                false,
                true,
                OftTraceResultView.TEST_RUNNER
        ));

        assertThat(editor.component.validationMessagesText(), containsString("Line 1: 'missing' not found"));
    }

    private OftRunConfiguration createConfiguration(final String name) {
        final OftRunConfigurationType type = new OftRunConfigurationType();
        final OftRunConfigurationFactory factory = (OftRunConfigurationFactory) type.getConfigurationFactories()[0];
        return new OftRunConfiguration(getProject(), factory, name);
    }
}
