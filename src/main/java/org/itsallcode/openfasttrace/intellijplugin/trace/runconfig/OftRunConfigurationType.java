package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;
import org.itsallcode.openfasttrace.intellijplugin.OftIcons;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResultView;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;

// [impl->dsn~openfasttrace-run-configuration~2]
public final class OftRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "OpenFastTraceRunConfiguration";

    public OftRunConfigurationType() {
        super(ID, "OpenFastTrace", "OpenFastTrace run configuration",
                NotNullLazyValue.createValue(() -> OftIcons.OPEN_FAST_TRACE));

        addFactory(new OftRunConfigurationFactory(this, "User requirements", config -> {
            final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                    OftTraceScopeMode.SELECTED_RESOURCES,
                    false,
                    false,
                    "doc/",
                    "feat, req, scn, bconstr",
                    "",
                    OftTraceResultView.TEST_RUNNER
            );
            config.updateFrom(snapshot);
        }));

        addFactory(new OftRunConfigurationFactory(this, "Design and down", config -> {
            final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                    OftTraceScopeMode.SELECTED_RESOURCES,
                    false,
                    false,
                    "doc/",
                    "feat, req, scn, bconstr, arch, dsn, constr, bld",
                    "",
                    OftTraceResultView.TEST_RUNNER
            );
            config.updateFrom(snapshot);
        }));

        addFactory(new OftRunConfigurationFactory(this, "Typical project", config -> {
            final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                    OftTraceScopeMode.SELECTED_RESOURCES,
                    true,
                    true,
                    "doc/",
                    "",
                    "",
                    OftTraceResultView.TEST_RUNNER
            );
            config.updateFrom(snapshot);
        }));

        addFactory(new OftRunConfigurationFactory(this, "Unfiltered", config -> {
            final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                    OftTraceScopeMode.WHOLE_PROJECT,
                    false,
                    false,
                    ".",
                    "",
                    "",
                    OftTraceResultView.TEST_RUNNER
            );
            config.updateFrom(snapshot);
        }));
    }
}
