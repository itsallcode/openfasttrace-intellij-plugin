package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class OftRunConfigurationFactory extends ConfigurationFactory {
    private final String factoryName;
    private final Consumer<OftRunConfiguration> initializer;

    public OftRunConfigurationFactory(final ConfigurationType type) {
        this(type, "General Scan", config -> {
        });
    }

    public OftRunConfigurationFactory(
            final ConfigurationType type,
            final String factoryName,
            final Consumer<OftRunConfiguration> initializer
    ) {
        super(type);
        this.factoryName = factoryName;
        this.initializer = initializer;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
        final OftRunConfiguration configuration = new OftRunConfiguration(project, this, getName());
        this.initializer.accept(configuration);
        return configuration;
    }

    @Override
    public @NotNull RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
    }

    @Override
    public @NotNull String getName() {
        return this.factoryName;
    }

    @Override
    public @NotNull String getId() {
        return OftRunConfigurationType.ID + "." + this.factoryName.replace(" ", "");
    }
}
