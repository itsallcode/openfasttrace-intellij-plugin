package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jdom.Element;

public final class OftRunConfiguration extends LocatableConfigurationBase<OftRunProfileState> {
    private State state = new State();

    public OftRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
        super(project, factory, name);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OftRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(
            @NotNull final Executor executor,
            @NotNull final ExecutionEnvironment environment
    ) throws ExecutionException {
        return new OftRunProfileState(environment, snapshot());
    }

    public OftTraceSettingsSnapshot snapshot() {
        return new OftTraceSettingsSnapshot(
                parseScopeMode(state.traceScopeMode),
                state.includeSourceRoots,
                state.includeTestRoots,
                state.additionalPathsText,
                state.artifactTypesText,
                state.tagsText
        );
    }

    public void updateFrom(final OftTraceSettingsSnapshot snapshot) {
        state.traceScopeMode = snapshot.scopeMode().name();
        state.includeSourceRoots = snapshot.includeSourceRoots();
        state.includeTestRoots = snapshot.includeTestRoots();
        state.additionalPathsText = snapshot.additionalPathsText();
        state.artifactTypesText = snapshot.artifactTypesText();
        state.tagsText = snapshot.tagsText();
    }

    @Override
    public void readExternal(@NotNull final Element element) throws InvalidDataException {
        super.readExternal(element);
        XmlSerializer.deserializeInto(state, element);
    }

    @Override
    public void writeExternal(@NotNull final Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeInto(state, element);
    }

    private static OftTraceScopeMode parseScopeMode(final String value) {
        if (value == null || value.isBlank()) {
            return OftTraceSettingsSnapshot.DEFAULT.scopeMode();
        }
        try {
            return OftTraceScopeMode.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return OftTraceSettingsSnapshot.DEFAULT.scopeMode();
        }
    }

    public static final class State {
        public String traceScopeMode = OftTraceSettingsSnapshot.DEFAULT.scopeMode().name();
        public boolean includeSourceRoots = OftTraceSettingsSnapshot.DEFAULT.includeSourceRoots();
        public boolean includeTestRoots = OftTraceSettingsSnapshot.DEFAULT.includeTestRoots();
        public String additionalPathsText = OftTraceSettingsSnapshot.DEFAULT.additionalPathsText();
        public String artifactTypesText = OftTraceSettingsSnapshot.DEFAULT.artifactTypesText();
        public String tagsText = OftTraceSettingsSnapshot.DEFAULT.tagsText();
    }
}
