package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.options.ConfigurationException;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.junit.jupiter.api.Assertions;

public class OftTraceProjectConfigurableTest extends AbstractOftPlatformTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        OftTraceProjectSettings.getInstance(getProject()).loadState(new OftTraceProjectSettings.State());
    }

    // [itest->dsn~configure-trace-scope-in-project-settings~1]
    public void testGivenDefaultSettingsWhenResettingConfigurableThenItShowsTheDocumentedDefaults() {
        final OftTraceProjectConfigurable configurable = new OftTraceProjectConfigurable(getProject());
        configurable.createComponent();

        configurable.reset();

        final OftTraceSettingsSnapshot settings = configurableSettings(configurable);
        assertEquals(OftTraceScopeMode.WHOLE_PROJECT, settings.scopeMode());
        assertEquals(true, settings.includeSourceRoots());
        assertEquals(true, settings.includeTestRoots());
        assertEquals("doc/", settings.additionalPathsText());
    }

    public void testGivenDefaultSettingsWhenResettingConfigurableThenSelectedResourceControlsAreDisabled() {
        final OftTraceProjectConfigurable configurable = new OftTraceProjectConfigurable(getProject());
        configurable.createComponent();

        configurable.reset();

        assertFalse(component(configurable).isSelectedResourcesEnabled());
    }

    public void testGivenSelectedResourceSettingsWhenApplyingConfigurableThenItUpdatesTheProjectSettings()
            throws ConfigurationException {
        final OftTraceProjectConfigurable configurable = new OftTraceProjectConfigurable(getProject());
        configurable.createComponent();
        component(configurable).setSettings(new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                true,
                false,
                "doc/\nspec/"
        ));

        assertTrue(configurable.isModified());
        configurable.apply();

        final OftTraceSettingsSnapshot snapshot = OftTraceProjectSettings.getInstance(getProject()).snapshot();
        assertEquals(OftTraceScopeMode.SELECTED_RESOURCES, snapshot.scopeMode());
        assertEquals(true, snapshot.includeSourceRoots());
        assertEquals(false, snapshot.includeTestRoots());
        assertEquals("doc/\nspec/", snapshot.additionalPathsText());
    }

    public void testGivenAbsoluteAdditionalPathWhenApplyingConfigurableThenItRejectsTheSettings() {
        final OftTraceProjectConfigurable configurable = new OftTraceProjectConfigurable(getProject());
        configurable.createComponent();
        component(configurable).setSettings(new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                true,
                true,
                "/absolute/path"
        ));

        final ConfigurationException exception =
                Assertions.assertThrows(ConfigurationException.class, configurable::apply);

        assertTrue(exception.getMessage().contains("must be project-relative"));
    }

    private static OftTraceSettingsSnapshot configurableSettings(final OftTraceProjectConfigurable configurable) {
        return component(configurable).getSettings();
    }

    private static OftTraceSettingsComponent component(final OftTraceProjectConfigurable configurable) {
        try {
            final var field = OftTraceProjectConfigurable.class.getDeclaredField("component");
            field.setAccessible(true);
            return (OftTraceSettingsComponent) field.get(configurable);
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to access configurable component", exception);
        }
    }
}
