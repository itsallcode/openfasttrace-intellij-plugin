package org.itsallcode.openfasttrace.intellijplugin;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * Shared constants for OpenFastTrace plugin icon resources.
 */
public final class OftIcons {
    public static final String PLUGIN_ICON_RESOURCE_PATH = "META-INF/pluginIcon.svg";
    public static final String PLUGIN_DARK_ICON_RESOURCE_PATH = "META-INF/pluginIcon_dark.svg";
    public static final String RUN_CONFIGURATION_ICON_RESOURCE_PATH = "icons/openfasttrace.svg";

    // [impl->dsn~openfasttrace-run-configuration-icon~1]
    public static final Icon OPEN_FAST_TRACE = IconLoader.getIcon(
            "/" + RUN_CONFIGURATION_ICON_RESOURCE_PATH,
            OftIcons.class
    );

    private OftIcons() {
        // Utility class.
    }
}
