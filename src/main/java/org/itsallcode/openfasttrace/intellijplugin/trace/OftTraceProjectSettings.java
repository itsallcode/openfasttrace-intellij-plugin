package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
@State(
        name = "org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceProjectSettings",
        storages = @Storage("openfasttrace.xml")
)
// [impl->dsn~trace-configuration-integration~1]
final class OftTraceProjectSettings implements PersistentStateComponent<OftTraceProjectSettings.State> {
    private State state = new State();

    static OftTraceProjectSettings getInstance(final Project project) {
        return project.getService(OftTraceProjectSettings.class);
    }

    OftTraceSettingsSnapshot snapshot() {
        return new OftTraceSettingsSnapshot(
                parseScopeMode(state.traceScopeMode),
                state.includeSourceRoots,
                state.includeTestRoots,
                state.additionalPathsText,
                state.artifactTypesText,
                state.tagsText
        );
    }

    void updateFrom(final OftTraceSettingsSnapshot snapshot) {
        state.traceScopeMode = snapshot.scopeMode().name();
        state.includeSourceRoots = snapshot.includeSourceRoots();
        state.includeTestRoots = snapshot.includeTestRoots();
        state.additionalPathsText = snapshot.additionalPathsText();
        state.artifactTypesText = snapshot.artifactTypesText();
        state.tagsText = snapshot.tagsText();
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(final State state) {
        this.state = state == null ? new State() : state;
        this.state.traceScopeMode = parseScopeMode(this.state.traceScopeMode).name();
        if (this.state.additionalPathsText == null) {
            this.state.additionalPathsText = OftTraceSettingsSnapshot.DEFAULT.additionalPathsText();
        }
        if (this.state.artifactTypesText == null) {
            this.state.artifactTypesText = "";
        }
        if (this.state.tagsText == null) {
            this.state.tagsText = "";
        }
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

    static final class State {
        String traceScopeMode = OftTraceSettingsSnapshot.DEFAULT.scopeMode().name();
        boolean includeSourceRoots = OftTraceSettingsSnapshot.DEFAULT.includeSourceRoots();
        boolean includeTestRoots = OftTraceSettingsSnapshot.DEFAULT.includeTestRoots();
        String additionalPathsText = OftTraceSettingsSnapshot.DEFAULT.additionalPathsText();
        String artifactTypesText = OftTraceSettingsSnapshot.DEFAULT.artifactTypesText();
        String tagsText = OftTraceSettingsSnapshot.DEFAULT.tagsText();
    }
}
