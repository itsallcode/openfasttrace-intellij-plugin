package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class OftNavigationInfrastructureTest extends AbstractOftPlatformTestCase {
    public void testGivenDeclarationNavigationElementWhenQueryingMetadataThenItDelegatesToThePsiElement() {
        myFixture.configureByText("spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiElement delegate = findRequiredElementAt(0);
        final OftIndexedSpecification specification =
                new OftIndexedSpecification("req", "openfasttrace_navigation_target", 1, 0);
        final OftDeclarationNavigationElement element = new OftDeclarationNavigationElement(delegate, specification);

        Assertions.assertAll(
                () -> assertThat(element.getParent(), is(delegate.getParent())),
                () -> assertThat(element.getContainingFile(), is(myFixture.getFile())),
                () -> assertThat(element.getNavigationElement(), is(delegate)),
                () -> assertThat(element.getTextOffset(), is(0)),
                () -> assertThat(element.getName(), is(specification.id())),
                () -> assertThat(element.getPresentableText(), is(specification.id())),
                () -> assertThat(requireLocationString(element.getLocationString()), endsWith("spec.md")),
                () -> assertThat(element.getIcon(false), notNullValue()),
                () -> assertThat(element.isValid(), is(true)),
                () -> assertThat(element.getManager(), is(delegate.getManager())),
                () -> assertThat(element.canNavigate(), is(true)),
                () -> assertThat(element.canNavigateToSource(), is(true))
        );
    }

    public void testGivenDeclarationNavigationElementWithoutDelegateWhenQueryingFallbackStateThenItReturnsSafeDefaults() {
        final OftDeclarationNavigationElement element =
                new OftDeclarationNavigationElement(null, new OftIndexedSpecification("req", "missing", 1, 0));

        Assertions.assertAll(
                () -> assertThat(element.getParent(), is((PsiElement) null)),
                () -> assertThat(element.getContainingFile(), nullValue()),
                () -> assertThat(element.getLocationString(), nullValue()),
                () -> assertThat(element.getIcon(false), nullValue()),
                () -> assertThat(element.isValid(), is(false)),
                () -> assertThat(element.canNavigate(), is(false)),
                () -> assertThat(element.canNavigateToSource(), is(false))
        );

        element.navigate(true);
    }

    public void testGivenDeclarationNavigationElementWithoutDelegateWhenGettingNavigationElementThenItThrowsWithMessage() {
        final OftDeclarationNavigationElement element =
                new OftDeclarationNavigationElement(null, new OftIndexedSpecification("req", "missing", 1, 0));

        final NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                element::getNavigationElement
        );

        assertThat(exception.getMessage(), is("navigation delegate is unavailable"));
    }

    public void testGivenDeclarationNavigationElementWithoutDelegateWhenGettingManagerThenItThrowsWithMessage() {
        final OftDeclarationNavigationElement element =
                new OftDeclarationNavigationElement(null, new OftIndexedSpecification("req", "missing", 1, 0));

        final NullPointerException exception = Assertions.assertThrows(NullPointerException.class, element::getManager);

        assertThat(exception.getMessage(), is("navigation delegate is unavailable"));
    }

    public void testGivenNavigationItemWhenQueryingMetadataThenItExposesSpecificationMetadata() {
        myFixture.configureByText("spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final OftIndexedSpecification specification =
                new OftIndexedSpecification("req", "openfasttrace_navigation_target", 1, 0);
        final OftNavigationItem item =
                new OftNavigationItem(getProject(), myFixture.getFile().getVirtualFile(), specification);

        Assertions.assertAll(
                () -> assertThat(item.getName(), is(specification.id())),
                () -> assertThat(item.getPresentation().getPresentableText(), is(specification.id())),
                () -> assertThat(requireLocationString(item.getPresentation().getLocationString()), endsWith("spec.md")),
                () -> assertThat(item.canNavigate(), is(true)),
                () -> assertThat(item.canNavigateToSource(), is(true)),
                () -> assertThat(item.getFile(), is(myFixture.getFile().getVirtualFile())),
                () -> assertThat(item.getSpecification(), is(specification))
        );
    }

    public void testGivenSpecificationReferenceWhenResolvingThenItResolvesToTheSpecificationFile() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final var specificationReferenceFile = myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        myFixture.configureFromExistingVirtualFile(specificationReferenceFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(
                myFixture.getFile().getText().indexOf("req~openfasttrace_navigation_target~1")
        );

        final PsiReference specificationReference = findRequiredReferenceAtCaret();

        assertThat(resolveRequiredReference(specificationReference).getContainingFile().getName(), is("spec.md"));
    }

    public void testGivenCoverageTagReferenceWhenResolvingThenItResolvesToTheSpecificationFile() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final String coverageTag = "[impl" + "->req~openfasttrace_navigation_target~1]";
        final var coverageReferenceFile = myFixture.addFileToProject("src/Main.java", """
                // %s
                class Main {
                }
                """.formatted(coverageTag));
        myFixture.configureFromExistingVirtualFile(coverageReferenceFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(
                myFixture.getFile().getText().indexOf("req~openfasttrace_navigation_target~1")
        );

        final PsiReference coverageReference = findRequiredReferenceAtCaret();

        assertThat(resolveRequiredReference(coverageReference).getContainingFile().getName(), is("spec.md"));
    }

    private PsiElement findRequiredElementAt(final int offset) {
        final PsiElement element = myFixture.getFile().findElementAt(offset);
        if (element == null) {
            Assertions.fail("Expected PSI element at offset " + offset);
        }
        return element;
    }

    private PsiReference findRequiredReferenceAtCaret() {
        final int offset = myFixture.getEditor().getCaretModel().getOffset();
        final PsiReference reference = myFixture.getFile().findReferenceAt(offset);
        if (reference == null) {
            Assertions.fail("Expected PSI reference at caret offset " + offset);
        }
        return reference;
    }

    private PsiElement resolveRequiredReference(final PsiReference reference) {
        final PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            Assertions.fail("Expected reference to resolve to a PSI element");
        }
        return resolvedElement;
    }

    private String requireLocationString(final String locationString) {
        if (locationString == null) {
            Assertions.fail("Expected navigation location string");
        }
        return locationString;
    }
}
