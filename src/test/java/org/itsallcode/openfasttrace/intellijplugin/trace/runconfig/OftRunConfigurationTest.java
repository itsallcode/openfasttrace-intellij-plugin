package org.itsallcode.openfasttrace.intellijplugin.trace.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.OftIcons;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceResultView;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceScopeMode;
import org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceSettingsSnapshot;
import org.itsallcode.openfasttrace.api.core.ItemStatus;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import org.jdom.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

// [itest->dsn~openfasttrace-run-configuration~2]
public class OftRunConfigurationTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~test-runner-as-default-run-configuration-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenNewRunConfigurationWhenReadingSnapshotThenItDefaultsToTestRunner() {
        final OftRunConfiguration configuration = createConfiguration("Test");

        Assertions.assertAll(
                () -> assertThat(configuration.snapshot().resultView(), is(OftTraceResultView.TEST_RUNNER)),
                () -> assertThat(configuration.snapshot().showTransitiveDefects(), is(true))
        );
    }

    // [itest->dsn~select-test-runner-trace-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenRunConfigurationWhenUpdatingFromSnapshotThenItStoresTheSettings() {
        final OftRunConfiguration configuration = createConfiguration("Test");
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                true,
                "additional",
                "dsn",
                "mvp",
                true,
                false,
                Set.of(ItemStatus.DRAFT, ItemStatus.PROPOSED),
                OftTraceResultView.TEST_RUNNER
        );

        configuration.updateFrom(snapshot);

        final OftTraceSettingsSnapshot stored = configuration.snapshot();
        Assertions.assertAll(
                () -> assertThat(stored.scopeMode(), is(snapshot.scopeMode())),
                () -> assertThat(stored.includeSourceRoots(), is(snapshot.includeSourceRoots())),
                () -> assertThat(stored.includeTestRoots(), is(snapshot.includeTestRoots())),
                () -> assertThat(stored.additionalPathsText(), is(snapshot.additionalPathsText())),
                () -> assertThat(stored.artifactTypesText(), is(snapshot.artifactTypesText())),
                () -> assertThat(stored.tagsText(), is(snapshot.tagsText())),
                () -> assertThat(stored.includeUntagged(), is(snapshot.includeUntagged())),
                () -> assertThat(stored.showTransitiveDefects(), is(snapshot.showTransitiveDefects())),
                () -> assertThat(stored.selectedStatuses(), is(snapshot.selectedStatuses())),
                () -> assertThat(stored.resultView(), is(snapshot.resultView()))
        );
    }

    // [itest->dsn~select-test-runner-trace-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenRunConfigurationWithSettingsWhenWritingAndReadingExternalThenItPreservesSettings()
            throws WriteExternalException, InvalidDataException {
        final OftRunConfiguration configuration = createConfiguration("Test");
        final OftTraceSettingsSnapshot snapshot = new OftTraceSettingsSnapshot(
                OftTraceScopeMode.SELECTED_RESOURCES,
                false,
                true,
                "additional",
                "dsn",
                "mvp",
                true,
                false,
                Set.of(ItemStatus.DRAFT, ItemStatus.REJECTED),
                OftTraceResultView.TEST_RUNNER
        );
        configuration.updateFrom(snapshot);

        final Element element = new Element("configuration");
        configuration.writeExternal(element);

        final OftRunConfiguration otherConfiguration = createConfiguration("Other");
        otherConfiguration.readExternal(element);

        final OftTraceSettingsSnapshot stored = otherConfiguration.snapshot();
        Assertions.assertAll(
                () -> assertThat(stored.scopeMode(), is(snapshot.scopeMode())),
                () -> assertThat(stored.includeSourceRoots(), is(snapshot.includeSourceRoots())),
                () -> assertThat(stored.includeTestRoots(), is(snapshot.includeTestRoots())),
                () -> assertThat(stored.additionalPathsText(), is(snapshot.additionalPathsText())),
                () -> assertThat(stored.artifactTypesText(), is(snapshot.artifactTypesText())),
                () -> assertThat(stored.tagsText(), is(snapshot.tagsText())),
                () -> assertThat(stored.includeUntagged(), is(snapshot.includeUntagged())),
                () -> assertThat(stored.showTransitiveDefects(), is(snapshot.showTransitiveDefects())),
                () -> assertThat(stored.selectedStatuses(), is(snapshot.selectedStatuses())),
                () -> assertThat(stored.resultView(), is(snapshot.resultView()))
        );
    }

    // [itest->dsn~test-runner-as-default-run-configuration-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenRunConfigurationWithNoStoredResultViewWhenReadingExternalThenItDefaultsToTestRunner()
            throws InvalidDataException {
        final OftRunConfiguration configuration = createConfiguration("Test");

        configuration.readExternal(new Element("configuration"));

        Assertions.assertAll(
                () -> assertThat(configuration.snapshot().resultView(), is(OftTraceResultView.TEST_RUNNER)),
                () -> assertThat(configuration.snapshot().showTransitiveDefects(), is(true)),
                () -> assertThat(configuration.snapshot().selectedStatuses(), is(Set.of(ItemStatus.APPROVED)))
        );
    }

    // [itest->dsn~test-runner-as-default-run-configuration-result-view~1]
    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenRunConfigurationWithInvalidStoredResultViewWhenReadingExternalThenItDefaultsToTestRunner()
            throws InvalidDataException {
        final Element element = new Element("configuration");
        element.addContent(new Element("option")
                .setAttribute("name", "resultView")
                .setAttribute("value", "UNKNOWN_RESULT_VIEW"));
        final OftRunConfiguration configuration = createConfiguration("Test");

        configuration.readExternal(element);

        Assertions.assertAll(
                () -> assertThat(configuration.snapshot().resultView(), is(OftTraceResultView.TEST_RUNNER)),
                () -> assertThat(configuration.snapshot().showTransitiveDefects(), is(true))
        );
    }

    // [itest->dsn~openfasttrace-run-configuration-icon~1]
    public void testGivenRunConfigurationTypeWhenReadingIconThenItUsesTheOpenFastTraceIcon() {
        final OftRunConfigurationType type = new OftRunConfigurationType();

        Assertions.assertAll(
                () -> assertThat(type.getIcon(), sameInstance(OftIcons.OPEN_FAST_TRACE)),
                () -> assertThat(type.getIcon(), is(not(sameInstance(AllIcons.Actions.Execute))))
        );
    }

    // [itest->dsn~use-run-configuration-templates~2]
    public void testGivenRunConfigurationTypeWhenCheckingFactoriesThenItContainsAllTemplates() {
        final OftRunConfigurationType type = new OftRunConfigurationType();
        final String[] factoryNames = Arrays.stream(type.getConfigurationFactories())
                .map(ConfigurationFactory::getName)
                .toArray(String[]::new);

        assertThat(Arrays.asList(factoryNames), containsInAnyOrder(
                "User requirements",
                "Design and above",
                "Typical project",
                "Unfiltered"
        ));
    }

    // [itest->dsn~trace-configuration-integration~3]
    public void testGivenRunConfigurationTypeWhenCheckingFactorySingletonPolicyThenItDisallowsMultipleInstances() {
        final OftRunConfigurationType type = new OftRunConfigurationType();

        Assertions.assertAll(
                Arrays.stream(type.getConfigurationFactories())
                        .map(factory -> (Executable) () -> {
                            final OftRunConfigurationFactory oftFactory = (OftRunConfigurationFactory) factory;
                            assertThat(oftFactory.getSingletonPolicy(), is(RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY));
                        })
                        .toList()
        );
    }

    public void testGivenUserRequirementsTemplateWhenCreatingConfigurationThenItHasCorrectSettings() {
        assertTemplateSettings(
                "User requirements",
                false,
                false,
                false,
                "doc/",
                "feat, req, scn, bconstr",
                true
        );
    }

    public void testGivenDesignAndAboveTemplateWhenCreatingConfigurationThenItHasCorrectSettings() {
        assertTemplateSettings(
                "Design and above",
                false,
                false,
                false,
                "doc/",
                "feat, req, scn, bconstr, arch, dsn, constr, bld",
                true
        );
    }

    public void testGivenTypicalProjectTemplateWhenCreatingConfigurationThenItHasCorrectSettings() {
        assertTemplateSettings(
                "Typical project",
                false,
                true,
                true,
                "doc/",
                "",
                true
        );
    }

    public void testGivenUnfilteredTemplateWhenCreatingConfigurationThenItHasCorrectSettings() {
        assertTemplateSettings(
                "Unfiltered",
                true,
                false,
                false,
                ".",
                "",
                true
        );
    }

    // [itest->dsn~show-transitive-defects-by-default-in-run-configuration-templates~1]
    private void assertTemplateSettings(
            final String templateName,
            final boolean wholeProject,
            final boolean includeSourceRoots,
            final boolean includeTestRoots,
            final String additionalPathsText,
            final String artifactTypesText,
            final boolean showTransitiveDefects
    ) {
        final OftRunConfiguration configuration = createConfigurationFromTemplate(templateName);
        final OftTraceSettingsSnapshot snapshot = configuration.snapshot();

        Assertions.assertAll(
                () -> assertThat(snapshot.scopeMode(), is(wholeProject
                        ? OftTraceScopeMode.WHOLE_PROJECT
                        : OftTraceScopeMode.SELECTED_RESOURCES)),
                () -> assertThat(snapshot.includeSourceRoots(), is(includeSourceRoots)),
                () -> assertThat(snapshot.includeTestRoots(), is(includeTestRoots)),
                () -> assertThat(snapshot.additionalPathsText(), is(additionalPathsText)),
                () -> assertThat(snapshot.artifactTypesText(), is(artifactTypesText)),
                () -> assertThat(snapshot.tagsText(), is("")),
                () -> assertThat(snapshot.includeUntagged(), is(false)),
                () -> assertThat(snapshot.showTransitiveDefects(), is(showTransitiveDefects)),
                () -> assertThat(snapshot.selectedStatuses(), is(Set.of(ItemStatus.APPROVED)))
        );
    }

    // [itest->dsn~reject-run-configuration-without-item-status~1]
    public void testGivenNoStoredStatusWhenCheckingConfigurationThenItReportsTypeAndMessage()
            throws InvalidDataException {
        final Element element = new Element("configuration");
        element.addContent(new Element("option")
                .setAttribute("name", "selectedStatusesText")
                .setAttribute("value", ""));
        final OftRunConfiguration configuration = createConfiguration("Test");
        configuration.readExternal(element);

        final RuntimeConfigurationError exception = Assertions.assertThrows(
                RuntimeConfigurationError.class,
                configuration::checkConfiguration
        );

        assertThat(exception.getMessage(), is(OftRunConfiguration.STATUS_SELECTION_ERROR));
    }

    public void testGivenInvalidStoredStatusWhenReadingExternalThenItDefaultsToApproved()
            throws InvalidDataException {
        final Element element = new Element("configuration");
        element.addContent(new Element("option")
                .setAttribute("name", "selectedStatusesText")
                .setAttribute("value", "NOT_A_VALID_STATUS"));
        final OftRunConfiguration configuration = createConfiguration("Test");
        configuration.readExternal(element);

        assertThat(configuration.snapshot().selectedStatuses(), is(Set.of(ItemStatus.APPROVED)));
    }

    public void testGivenInvalidStoredScopeModeWhenReadingExternalThenItDefaultsToDefaultScopeMode()
            throws InvalidDataException {
        final Element element = new Element("configuration");
        element.addContent(new Element("option")
                .setAttribute("name", "traceScopeMode")
                .setAttribute("value", "INVALID_SCOPE"));
        final OftRunConfiguration configuration = createConfiguration("Test");
        configuration.readExternal(element);

        assertThat(configuration.snapshot().scopeMode(), is(OftTraceSettingsSnapshot.DEFAULT.scopeMode()));
    }

    public void testGivenRunConfigurationWhenReadingSuggestedNameThenItMatchesFactoryName() {
        final OftRunConfiguration configuration = createConfiguration("Custom Name");

        assertThat(configuration.suggestedName(), is("User requirements"));
    }

    private OftRunConfiguration createConfiguration(final String name) {
        final OftRunConfigurationType type = new OftRunConfigurationType();
        final OftRunConfigurationFactory factory = (OftRunConfigurationFactory) type.getConfigurationFactories()[0];
        return new OftRunConfiguration(getProject(), factory, name);
    }

    private OftRunConfiguration createConfigurationFromTemplate(final String templateName) {
        final OftRunConfigurationType type = new OftRunConfigurationType();
        final OftRunConfigurationFactory factory = (OftRunConfigurationFactory) Arrays.stream(type.getConfigurationFactories())
                .filter(f -> f.getName().equals(templateName))
                .findFirst()
                .orElseThrow();
        final OftRunConfiguration configuration = (OftRunConfiguration) factory.createTemplateConfiguration(getProject());
        configuration.setGeneratedName();
        assertThat(configuration.getName(), is(templateName));
        return configuration;
    }
}
