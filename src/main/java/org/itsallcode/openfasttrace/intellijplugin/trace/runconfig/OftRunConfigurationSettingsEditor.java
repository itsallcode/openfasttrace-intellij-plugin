package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsComponent;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.VisibleForTesting;

import javax.swing.JComponent;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class OftRunConfigurationSettingsEditor extends SettingsEditor<OftRunConfiguration> {
    private final Project project;
    @VisibleForTesting
    OftTraceSettingsComponent component;

    public OftRunConfigurationSettingsEditor(final Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull final OftRunConfiguration configuration) {
        component.setSettings(configuration.snapshot());
    }

    @Override
    protected void applyEditorTo(@NotNull final OftRunConfiguration configuration) {
        configuration.updateFrom(component.getSettings());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        final String basePath = project.getBasePath();
        final Path projectRoot = basePath != null ? Paths.get(basePath) : null;
        component = new OftTraceSettingsComponent(projectRoot);
        return component.getPanel();
    }
}
