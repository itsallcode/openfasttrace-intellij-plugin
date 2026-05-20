package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.junit.jupiter.api.Assertions;

import javax.swing.JComponent;

import static org.hamcrest.MatcherAssert.assertThat;
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

    public void testGivenEditorWhenResettingFromConfigurationThenItUpdatesUI() {
        editor.createEditor(); // Initialize component
        final OftRunConfiguration configuration = createConfiguration("Test");
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                true,
                "additional",
                "dsn",
                "mvp"
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
                () -> assertThat(uiSettings.tagsText(), is(snapshot.tagsText()))
        );
    }

    public void testGivenEditorWhenApplyingToConfigurationThenItUpdatesConfiguration() {
        editor.createEditor(); // Initialize component
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                true,
                false,
                "more paths",
                "req",
                "tag"
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
                () -> assertThat(stored.tagsText(), is(snapshot.tagsText()))
        );
    }

    private OftRunConfiguration createConfiguration(final String name) {
        final OftRunConfigurationType type = new OftRunConfigurationType();
        final OftRunConfigurationFactory factory = (OftRunConfigurationFactory) type.getConfigurationFactories()[0];
        return new OftRunConfiguration(getProject(), factory, name);
    }
}
