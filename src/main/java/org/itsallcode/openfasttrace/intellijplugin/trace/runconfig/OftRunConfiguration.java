package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

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
import org.jdom.Element;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

public final class OftRunConfiguration extends LocatableConfigurationBase<OftRunProfileState> {
    private final State state = new State();

    public OftRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
        super(project, factory, name);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OftRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @NonNull RunProfileState getState(
            @NotNull final Executor executor,
            @NotNull final ExecutionEnvironment environment
    ) {
        return new OftRunProfileState(environment, snapshot());
    }

    public OftTraceSettingsSnapshot snapshot() {
        return new OftTraceSettingsSnapshot(
                parseScopeMode(state.getTraceScopeMode()),
                state.isIncludeSourceRoots(),
                state.isIncludeTestRoots(),
                state.getAdditionalPathsText(),
                state.getArtifactTypesText(),
                state.getTagsText()
        );
    }

    public void updateFrom(final OftTraceSettingsSnapshot snapshot) {
        state.setTraceScopeMode(snapshot.scopeMode().name());
        state.setIncludeSourceRoots(snapshot.includeSourceRoots());
        state.setIncludeTestRoots(snapshot.includeTestRoots());
        state.setAdditionalPathsText(snapshot.additionalPathsText());
        state.setArtifactTypesText(snapshot.artifactTypesText());
        state.setTagsText(snapshot.tagsText());
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

    private static final class State implements Serializable {
        private String traceScopeMode = OftTraceSettingsSnapshot.DEFAULT.scopeMode().name();
        private boolean includeSourceRoots = OftTraceSettingsSnapshot.DEFAULT.includeSourceRoots();
        private boolean includeTestRoots = OftTraceSettingsSnapshot.DEFAULT.includeTestRoots();
        private String additionalPathsText = OftTraceSettingsSnapshot.DEFAULT.additionalPathsText();
        private String artifactTypesText = OftTraceSettingsSnapshot.DEFAULT.artifactTypesText();
        private String tagsText = OftTraceSettingsSnapshot.DEFAULT.tagsText();

        public String getTraceScopeMode() {
            return traceScopeMode;
        }

        public void setTraceScopeMode(final String traceScopeMode) {
            this.traceScopeMode = traceScopeMode;
        }

        public boolean isIncludeSourceRoots() {
            return includeSourceRoots;
        }

        public void setIncludeSourceRoots(final boolean includeSourceRoots) {
            this.includeSourceRoots = includeSourceRoots;
        }

        public boolean isIncludeTestRoots() {
            return includeTestRoots;
        }

        public void setIncludeTestRoots(final boolean includeTestRoots) {
            this.includeTestRoots = includeTestRoots;
        }

        public String getAdditionalPathsText() {
            return additionalPathsText;
        }

        public void setAdditionalPathsText(final String additionalPathsText) {
            this.additionalPathsText = additionalPathsText;
        }

        public String getArtifactTypesText() {
            return artifactTypesText;
        }

        public void setArtifactTypesText(final String artifactTypesText) {
            this.artifactTypesText = artifactTypesText;
        }

        public String getTagsText() {
            return tagsText;
        }

        public void setTagsText(final String tagsText) {
            this.tagsText = tagsText;
        }
    }
}
