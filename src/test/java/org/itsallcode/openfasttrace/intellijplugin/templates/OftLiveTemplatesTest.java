package org.itsallcode.openfasttrace.intellijplugin.templates;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.junit.jupiter.api.Assertions;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OftLiveTemplatesTest extends AbstractOftPlatformTestCase {
    private static final String DESIGN_TEMPLATE_KEY = "dsn";

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

    public void testGivenCoversTemplatesWhenQueryingTemplateSettingsThenCoveredVariablesDoNotForceCompletion() {
        final TemplateSettings templateSettings = TemplateSettings.getInstance();
        final List<String> templateKeysWithCoveredVariable = List.of("arch", "dsn", "qs", "req", "scn");

        final List<String> coveredVariableExpressions = templateKeysWithCoveredVariable.stream()
                .map(key -> templateSettings.getTemplate(key, OftLiveTemplates.GROUP_NAME))
                .map(template -> template.getExpressionStringAt(indexOfVariable(template, "COVERED")))
                .toList();

        assertThat(coveredVariableExpressions, Matchers.everyItem(is("")));
    }

    // [itest->dsn~complete-specification-item-id-in-active-live-template-covers-field~1]
    public void testGivenActiveLiveTemplateCoveredFieldWhenCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        myFixture.addFileToProject("doc/spec.md", """
                req~live-template-alpha.feature~1
                Needs: scn

                req~live-template-beta.feature~1
                Needs: scn
                """);
        myFixture.configureByText("current.md", "<caret>");

        final TemplateImpl designTemplate = TemplateSettings.getInstance()
                .getTemplate(DESIGN_TEMPLATE_KEY, OftLiveTemplates.GROUP_NAME);
        TemplateManagerImpl.setTemplateTesting(getTestRootDisposable());
        TemplateManager.getInstance(getProject()).startTemplate(myFixture.getEditor(), designTemplate);

        final TemplateState templateState = Objects.requireNonNull(TemplateManagerImpl.getTemplateState(myFixture.getEditor()));
        advanceTemplateToVariable(getProject(), templateState, "COVERED");
        myFixture.type("req~live-template-");
        final LookupElement[] elements = myFixture.completeBasic();

        Assertions.assertAll(
                () -> assertThat(templateState, notNullValue()),
                () -> assertThat(activeVariableName(templateState), is("COVERED")),
                () -> assertThat(elements.length, is(2)),
                () -> assertThat(
                        myFixture.getLookupElementStrings(),
                        contains("req~live-template-alpha.feature~1", "req~live-template-beta.feature~1")
                ),
                () -> assertThat(
                        TemplateManager.getInstance(getProject()).getActiveTemplate(myFixture.getEditor()),
                        notNullValue(Template.class)
                )
        );
    }

    private static void advanceTemplateToVariable(
            final Project project,
            final TemplateState templateState,
            final String variableName
    ) {
        int remainingVariables = templateState.getTemplate().getVariableCount();
        while (!templateState.isFinished()
                && !variableName.equals(activeVariableName(templateState))
                && remainingVariables > 0) {
            WriteCommandAction.runWriteCommandAction(project, templateState::nextTab);
            remainingVariables--;
        }
    }

    private static int indexOfVariable(final TemplateImpl template, final String variableName) {
        return IntStream.range(0, template.getVariableCount())
                .filter(index -> variableName.equals(template.getVariableNameAt(index)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Template " + template.getKey() + " does not define variable " + variableName
                ));
    }

    private static String activeVariableName(final TemplateState templateState) {
        final int currentVariableNumber = templateState.getCurrentVariableNumber();
        return templateState.getTemplate().getVariableNameAt(currentVariableNumber);
    }
}
