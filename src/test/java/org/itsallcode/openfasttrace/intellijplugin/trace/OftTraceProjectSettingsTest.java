package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OftTraceProjectSettingsTest extends AbstractOftPlatformTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        OftTraceProjectSettings.getInstance(getProject()).loadState(new OftTraceProjectSettings.State());
    }

    public void testGivenFreshProjectSettingsWhenReadingThenItReturnsTheDocumentedDefaults() {
        final OftTraceSettingsSnapshot snapshot = OftTraceProjectSettings.getInstance(getProject()).snapshot();

        assertThat(snapshot.scopeMode(), is(OftTraceScopeMode.WHOLE_PROJECT));
        assertThat(snapshot.includeSourceRoots(), is(true));
        assertThat(snapshot.includeTestRoots(), is(true));
        assertThat(snapshot.additionalPathsText(), is("doc/"));
    }

    public void testGivenStoredStateWhenLoadingThenItNormalizesTheSnapshot() {
        final OftTraceProjectSettings settings = OftTraceProjectSettings.getInstance(getProject());
        final OftTraceProjectSettings.State state = new OftTraceProjectSettings.State();
        state.traceScopeMode = OftTraceScopeMode.SELECTED_RESOURCES.name();
        state.includeSourceRoots = false;
        state.includeTestRoots = true;
        state.additionalPathsText = "spec/\ndoc/";

        settings.loadState(state);

        final OftTraceSettingsSnapshot snapshot = settings.snapshot();
        assertThat(snapshot.scopeMode(), is(OftTraceScopeMode.SELECTED_RESOURCES));
        assertThat(snapshot.includeSourceRoots(), is(false));
        assertThat(snapshot.includeTestRoots(), is(true));
        assertThat(snapshot.additionalPathsText(), is("spec/\ndoc/"));
    }
}
