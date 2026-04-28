package org.itsallcode.openfasttrace.intellijplugin.trace;

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
            component = new OftTraceSettingsComponent();
        }
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(component.getSettings(), OftTraceProjectSettings.getInstance(project).snapshot());
    }

    @Override
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

    private static void validate(final OftTraceSettingsSnapshot settings) throws ConfigurationException {
        for (final String additionalPath : settings.additionalPaths()) {
            final Path path;
            try {
                path = Path.of(additionalPath);
            } catch (final InvalidPathException exception) {
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
}
