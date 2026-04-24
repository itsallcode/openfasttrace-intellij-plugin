package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class OftDeclarationResolverPlatformTest extends AbstractOftPlatformTestCase {
    public void testGivenElementInUnsupportedFileWhenFindingDeclaredItemThenItReturnsEmpty() {
        myFixture.configureByText("Main.java", "class Main {}");

        assertThat(OftDeclarationResolver.findDeclaredItem(requiredElementAt(0)).isEmpty(), is(true));
    }

    public void testGivenEmptyFileWhenFindingPsiElementThenItReturnsNull() {
        myFixture.configureByText("empty.md", "");

        assertThat(
                OftDeclarationResolver.findPsiElementAt(
                        PsiManager.getInstance(getProject()),
                        myFixture.getFile().getVirtualFile(),
                        0
                ),
                nullValue()
        );
    }

    public void testGivenScopeExcludingCoverageFilesWhenProcessingCoverageOccurrencesThenItReturnsTrue() {
        final PsiFile declarationFile = myFixture.configureByText("spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.addFileToProject("impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);

        final boolean processed = OftDeclarationResolver.processCoverageOccurrences(
                getProject(),
                requiredDeclaredItem(declarationFile),
                GlobalSearchScope.filesScope(getProject(), java.util.List.of(declarationFile.getVirtualFile())),
                element -> false
        );

        assertThat(processed, is(true));
    }

    public void testGivenProcessorStoppingOnSpecificationOccurrenceWhenProcessingCoverageOccurrencesThenItReturnsFalse() {
        final PsiFile declarationFile = myFixture.configureByText("spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.addFileToProject("impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);

        final boolean processed = OftDeclarationResolver.processCoverageOccurrences(
                getProject(),
                requiredDeclaredItem(declarationFile),
                GlobalSearchScope.projectScope(getProject()),
                element -> false
        );

        assertThat(processed, is(false));
    }

    public void testGivenProcessorStoppingOnCoverageTagOccurrenceWhenProcessingCoverageOccurrencesThenItReturnsFalse() {
        final PsiFile declarationFile = myFixture.configureByText("spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final String coverageTag = "[impl" + "->req~openfasttrace_navigation_target~1]";
        myFixture.addFileToProject("Main.java", """
                // %s
                class Main {
                }
                """.formatted(coverageTag));

        final boolean processed = OftDeclarationResolver.processCoverageOccurrences(
                getProject(),
                requiredDeclaredItem(declarationFile),
                GlobalSearchScope.projectScope(getProject()),
                element -> false
        );

        assertThat(processed, is(false));
    }

    public void testGivenInvalidSearchParametersWhenExecutingDefinitionsScopedSearchThenItReturnsTrue() {
        myFixture.configureByText("spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiElement declarationElement = requiredElementAt(0);

        final boolean processed = new OftDefinitionsScopedSearch().execute(
                new DefinitionsScopedSearch.SearchParameters(declarationElement) {
                    @Override
                    public boolean isQueryValid() {
                        return false;
                    }
                },
                element -> false
        );

        assertThat(processed, is(true));
    }

    private PsiElement requiredElementAt(final int offset) {
        final PsiElement element = myFixture.getFile().findElementAt(offset);
        if (element == null) {
            org.junit.jupiter.api.Assertions.fail("Expected PSI element at offset " + offset);
        }
        return element;
    }

    private OftSpecificationItem requiredDeclaredItem(final PsiFile file) {
        final PsiElement element = file.findElementAt(0);
        if (element == null) {
            org.junit.jupiter.api.Assertions.fail("Expected declaration element");
        }
        return OftDeclarationResolver.findDeclaredItem(element).orElseThrow();
    }
}
