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
    static final String DEFAULT_ADDITIONAL_PATH = "doc/";

    private State state = new State();

    static OftTraceProjectSettings getInstance(final Project project) {
        return project.getService(OftTraceProjectSettings.class);
    }

    OftTraceSettingsSnapshot snapshot() {
        return new OftTraceSettingsSnapshot(
                parseScopeMode(state.traceScopeMode),
                state.includeSourceRoots,
                state.includeTestRoots,
                state.additionalPathsText
        );
    }

    void updateFrom(final OftTraceSettingsSnapshot snapshot) {
        state.traceScopeMode = snapshot.scopeMode().name();
        state.includeSourceRoots = snapshot.includeSourceRoots();
        state.includeTestRoots = snapshot.includeTestRoots();
        state.additionalPathsText = snapshot.additionalPathsText();
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
            this.state.additionalPathsText = DEFAULT_ADDITIONAL_PATH;
        }
    }

    private static OftTraceScopeMode parseScopeMode(final String value) {
        if (value == null || value.isBlank()) {
            return OftTraceScopeMode.WHOLE_PROJECT;
        }
        try {
            return OftTraceScopeMode.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return OftTraceScopeMode.WHOLE_PROJECT;
        }
    }

    static final class State {
        String traceScopeMode = OftTraceScopeMode.WHOLE_PROJECT.name();
        boolean includeSourceRoots = true;
        boolean includeTestRoots = true;
        String additionalPathsText = DEFAULT_ADDITIONAL_PATH;
    }
}
