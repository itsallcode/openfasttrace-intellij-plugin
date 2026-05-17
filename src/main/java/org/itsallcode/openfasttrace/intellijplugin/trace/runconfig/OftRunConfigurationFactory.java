package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class OftRunConfigurationFactory extends ConfigurationFactory {
    public OftRunConfigurationFactory(final ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
        return new OftRunConfiguration(project, this, "OpenFastTrace");
    }

    @Override
    public @NotNull String getId() {
        return OftRunConfigurationType.ID;
    }
}
