package org.itsallcode.openfasttrace.intellijplugin.templates;

import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.junit.jupiter.api.Assertions;

import java.net.URL;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OftLiveTemplatesTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~show-oft-live-templates-in-live-template-settings~1]
    public void testGivenPluginIsLoadedWhenQueryingTemplateSettingsThenTheOftTemplateGroupIsRegistered() {
        final URL templateResource = getClass().getClassLoader().getResource(OftLiveTemplates.RESOURCE_PATH);
        final TemplateSettings templateSettings = TemplateSettings.getInstance();
        final List<String> templateKeys = List.of("arch", "bconstr", "constr", "dsn", "feat", "qg", "qs", "req", "scn");

        Assertions.assertAll(
                () -> assertThat(templateResource, notNullValue()),
                () -> assertThat(
                        templateKeys.stream()
                                .map(key -> templateSettings.getTemplate(key, OftLiveTemplates.GROUP_NAME))
                                .toList(),
                        Matchers.everyItem(notNullValue())
                ),
                () -> assertThat(
                        templateSettings.getTemplatesAsList().stream()
                                .filter(template -> OftLiveTemplates.GROUP_NAME.equals(template.getGroupName()))
                                .map(TemplateImpl::getKey)
                                .toList(),
                        hasItems(templateKeys.toArray(new String[0]))
                )
        );
    }

    // [itest->dsn~insert-oft-scenario-live-template~1]
    public void testGivenScenarioTemplateWhenQueryingTemplateSettingsThenItContainsTheExpectedScenarioSkeleton() {
        final TemplateImpl scenarioTemplate = TemplateSettings.getInstance()
                .getTemplate(OftLiveTemplates.SCENARIO_TEMPLATE_KEY, OftLiveTemplates.GROUP_NAME);

        Assertions.assertAll(
                () -> assertThat(scenarioTemplate, notNullValue()),
                () -> assertThat(Objects.requireNonNull(scenarioTemplate).getDescription(), is("OFT Scenario")),
                () -> assertThat(
                        scenarioTemplate.getString(),
                        is("""
                                $HEADER_LEVEL$ $TITLE$
                                `scn~$NAME$~1`

                                **Given** $GIVEN$
                                **When** $WHEN$
                                **Then** $THEN$

                                Covers:
                                - `$COVERED$`

                                Needs: dsn

                                $END$""")
                ),
                () -> assertThat(scenarioTemplate.getVariableCount(), is(7)),
                () -> assertThat(
                        List.of(
                                scenarioTemplate.getVariableNameAt(0),
                                scenarioTemplate.getVariableNameAt(1),
                                scenarioTemplate.getVariableNameAt(2),
                                scenarioTemplate.getVariableNameAt(3),
                                scenarioTemplate.getVariableNameAt(4),
                                scenarioTemplate.getVariableNameAt(5),
                                scenarioTemplate.getVariableNameAt(6)
                        ),
                        is(List.of("HEADER_LEVEL", "TITLE", "NAME", "GIVEN", "WHEN", "THEN", "COVERED"))
                )
        );
    }
}
