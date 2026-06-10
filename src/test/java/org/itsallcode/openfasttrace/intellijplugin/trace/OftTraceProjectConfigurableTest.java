package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
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

        assertTrue(exception.getLocalizedMessage().contains("must be project-relative"));
    }

    // [itest->dsn~show-per-line-validation-for-additional-trace-paths~1]
    public void testGivenSelectedResourceSettingsWithMissingPathWhenResettingConfigurableThenItShowsPerLineValidation()
            throws Exception {
        final var validationRoot = createManagedTestArtifactDirectory("trace-settings-validation-root");
        final Project project = projectProxy(validationRoot.toString(), "validation-project");
        OftTraceProjectSettings.getInstance(project).updateFrom(new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                true,
                true,
                "doc/\nmissing"
        ));
        final OftTraceProjectConfigurable configurable = new OftTraceProjectConfigurable(project);
        configurable.createComponent();

        configurable.reset();

        assertEquals(
                "Resolved relative to: " + validationRoot.toAbsolutePath().normalize(),
                component(configurable).resolvedRelativeToText()
        );
        assertTrue(component(configurable).validationMessagesText().contains("Line 1: 'doc/' not found"));
        assertTrue(component(configurable).validationMessagesText().contains("Line 2: 'missing' not found"));
    }

    public void testGivenWholeProjectModeWhenResettingConfigurableThenItDoesNotShowPerLineValidation() {
        final OftTraceProjectConfigurable configurable = new OftTraceProjectConfigurable(getProject());
        configurable.createComponent();

        configurable.reset();

        assertEquals("", component(configurable).resolvedRelativeToText());
        assertEquals("", component(configurable).validationMessagesText());
    }

    private static OftTraceSettingsSnapshot configurableSettings(final OftTraceProjectConfigurable configurable) {
        return component(configurable).getSettings();
    }

    private Project projectProxy(final String basePath, final String name) {
        return (Project) java.lang.reflect.Proxy.newProxyInstance(
                Project.class.getClassLoader(),
                new Class<?>[]{Project.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBasePath" -> basePath;
                    case "getName" -> name;
                    case "getService" -> getProject().getService((Class<?>) args[0]);
                    case "isDisposed" -> false;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "Project[" + name + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
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
