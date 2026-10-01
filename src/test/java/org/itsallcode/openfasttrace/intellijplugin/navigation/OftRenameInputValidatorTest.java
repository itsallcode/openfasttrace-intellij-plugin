package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

// [itest->dsn~specification-item-rename~1]
public class OftRenameInputValidatorTest extends AbstractOftPlatformTestCase {
    private final OftRenameInputValidator validator = new OftRenameInputValidator();

    public void testGivenValidSpecificationItemIdsWhenCheckingIsValidThenReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test-item.name_1~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));
        final ProcessingContext context = new ProcessingContext();

        assertAll(
                () -> assertThat(validator.isInputValid("req~tell-late-work-excuse~1", declarationElement, context), is(true)),
                () -> assertThat(validator.isInputValid("dsn~sub.item-2~1", declarationElement, context), is(true)),
                () -> assertThat(validator.isInputValid("feat~excuse_of_the_day~1", declarationElement, context), is(true)),
                () -> assertThat(validator.isInputValid("impl~core.module-task_a~99", declarationElement, context), is(true))
        );
    }

    public void testGivenInvalidSpecificationItemIdsWhenCheckingIsValidThenReturnsFalse() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test-item.name_1~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));
        final ProcessingContext context = new ProcessingContext();

        assertAll(
                () -> assertThat(validator.isInputValid("invalid_name", declarationElement, context), is(false)),
                () -> assertThat(validator.isInputValid("req~missing_revision", declarationElement, context), is(false)),
                () -> assertThat(validator.isInputValid("req~item~not_a_number", declarationElement, context), is(false)),
                () -> assertThat(validator.isInputValid("~item~1", declarationElement, context), is(false)),
                () -> assertThat(validator.isInputValid("123~item~1", declarationElement, context), is(false)),
                () -> assertThat(validator.isInputValid("", declarationElement, context), is(false))
        );
    }

    public void testGivenValidAndInvalidIdsWhenGettingErrorMessageThenReturnsExpectedMessages() {
        assertAll(
                () -> assertThat(validator.getErrorMessage("req~valid-item~1", getProject()), is(nullValue())),
                () -> assertThat(
                        validator.getErrorMessage("invalid_id", getProject()),
                        containsString("Specification item ID must have the form '<type>~<name>~<revision>'")
                )
        );
    }

    public void testGivenSpecificationDeclarationElementWhenTestingPatternAcceptanceThenReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));
        final OftDeclarationNavigationElement navElement = new OftDeclarationNavigationElement(
                declarationElement,
                new OftIndexedSpecification("req", "test_item", 1, 0)
        );

        final ProcessingContext context = new ProcessingContext();
        assertAll(
                () -> assertThat(validator.getPattern().accepts(declarationElement, context), is(true)),
                () -> assertThat(validator.getPattern().accepts(navElement, context), is(true))
        );
    }

    public void testGivenNonDeclarationElementWhenTestingPatternAcceptanceThenReturnsFalse() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                # Markdown Header

                Some regular body text.
                """);
        final PsiElement headerElement = Objects.requireNonNull(specFile.findElementAt(0));
        final ProcessingContext context = new ProcessingContext();

        assertThat(validator.getPattern().accepts(headerElement, context), is(false));
    }
}
