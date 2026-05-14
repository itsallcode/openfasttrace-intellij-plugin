package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.jdom.Element;

public class OftRunConfigurationTest extends AbstractOftPlatformTestCase {
    public void testGivenRunConfigurationWhenUpdatingFromSnapshotThenItStoresTheSettings() {
        final OftRunConfiguration configuration = createConfiguration("Test");
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                true,
                "additional",
                "dsn",
                "mvp"
        );

        configuration.updateFrom(snapshot);

        final OftTraceSettingsSnapshot stored = configuration.snapshot();
        assertEquals(snapshot.scopeMode(), stored.scopeMode());
        assertEquals(snapshot.includeSourceRoots(), stored.includeSourceRoots());
        assertEquals(snapshot.includeTestRoots(), stored.includeTestRoots());
        assertEquals(snapshot.additionalPathsText(), stored.additionalPathsText());
        assertEquals(snapshot.artifactTypesText(), stored.artifactTypesText());
        assertEquals(snapshot.tagsText(), stored.tagsText());
    }

    public void testGivenRunConfigurationWithSettingsWhenWritingAndReadingExternalThenItPreservesSettings()
            throws WriteExternalException, InvalidDataException {
        final OftRunConfiguration configuration = createConfiguration("Test");
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                true,
                "additional",
                "dsn",
                "mvp"
        );
        configuration.updateFrom(snapshot);

        final Element element = new Element("configuration");
        configuration.writeExternal(element);

        final OftRunConfiguration otherConfiguration = createConfiguration("Other");
        otherConfiguration.readExternal(element);

        final OftTraceSettingsSnapshot stored = otherConfiguration.snapshot();
        assertEquals(snapshot.scopeMode(), stored.scopeMode());
        assertEquals(snapshot.includeSourceRoots(), stored.includeSourceRoots());
        assertEquals(snapshot.includeTestRoots(), stored.includeTestRoots());
        assertEquals(snapshot.additionalPathsText(), stored.additionalPathsText());
        assertEquals(snapshot.artifactTypesText(), stored.artifactTypesText());
        assertEquals(snapshot.tagsText(), stored.tagsText());
    }

    private OftRunConfiguration createConfiguration(final String name) {
        final OftRunConfigurationType type = new OftRunConfigurationType();
        final OftRunConfigurationFactory factory = (OftRunConfigurationFactory) type.getConfigurationFactories()[0];
        return new OftRunConfiguration(getProject(), factory, name);
    }
}
