package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;
import org.itsallcode.openfasttrace.intellijplugin.OftIcons;

// [impl->dsn~openfasttrace-run-configuration~1]
public final class OftRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "OpenFastTraceRunConfiguration";

    public OftRunConfigurationType() {
        super(ID, "OpenFastTrace", "OpenFastTrace run configuration",
                NotNullLazyValue.createValue(() -> OftIcons.OPEN_FAST_TRACE));
        addFactory(new OftRunConfigurationFactory(this));
    }
}
