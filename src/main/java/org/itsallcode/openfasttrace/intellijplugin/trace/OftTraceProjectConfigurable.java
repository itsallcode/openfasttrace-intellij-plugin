package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;

import javax.swing.JComponent;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

// [impl->dsn~configure-trace-scope-in-project-settings~1]
public final class OftTraceProjectConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private static final Logger LOG = Logger.getInstance(OftTraceProjectConfigurable.class);
    static final String ID = "org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceProjectConfigurable";

    private final Project project;
    private OftTraceSettingsComponent component;

    public OftTraceProjectConfigurable(final Project project) {
        this.project = project;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "OpenFastTrace";
    }

    @Override
    public JComponent createComponent() {
        if (component == null) {
            component = new OftTraceSettingsComponent(projectRoot());
        }
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(component.getSettings(), OftTraceProjectSettings.getInstance(project).snapshot());
    }

    @Override
    @SuppressWarnings("java:S1162") // IntelliJ settings validation reports rejected values through this API type.
    public void apply() throws ConfigurationException {
        validate(component.getSettings());
        OftTraceProjectSettings.getInstance(project).updateFrom(component.getSettings());
    }

    @Override
    public void reset() {
        component.setSettings(OftTraceProjectSettings.getInstance(project).snapshot());
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

    @SuppressWarnings("java:S1162") // IntelliJ Configurable callers expect ConfigurationException for validation errors.
    private static void validate(final OftTraceSettingsSnapshot settings) throws ConfigurationException {
        for (final String additionalPath : settings.additionalPaths()) {
            final Path path;
            try {
                path = Path.of(additionalPath);
            } catch (final InvalidPathException exception) {
                LOG.debug("Rejecting invalid OpenFastTrace additional trace path: " + additionalPath, exception);
                throw new ConfigurationException(
                        "Additional trace path is invalid: " + exception.getInput()
                );
            }
            if (path.isAbsolute()) {
                throw new ConfigurationException(
                        "Additional trace path must be project-relative: " + additionalPath
                );
            }
        }
    }

    private Path projectRoot() {
        final String basePath = project.getBasePath();
        if (basePath == null || basePath.isBlank()) {
            return null;
        }
        try {
            return Path.of(basePath);
        } catch (final InvalidPathException exception) {
            LOG.debug("Ignoring invalid OpenFastTrace project base path: " + basePath, exception);
            return null;
        }
    }
}
