package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.testFramework.EdtTestUtil;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class OftNavigationTest extends AbstractOftPlatformTestCase {
    private int searchContextCounter;

    // [itest->dsn~show-specification-item-in-go-to-symbol~1]
    public void testGivenDeclarationAndCoverageOccurrencesWhenGoToSymbolSearchesByFullIdThenOnlyTheDeclarationAppears() {
        final String specificationId = "req~openfasttrace_navigation_target~1";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);

        assertThat(searchSymbolMatches(specificationId), contains(specificationId));
    }

    // [itest->dsn~show-markdown-declaration-variants-in-go-to-symbol~1]
    public void testGivenPlainAndBacktickMarkdownDeclarationsWhenGoToSymbolSearchesByFullIdThenBothDeclarationsAppear() {
        final String plainSpecificationId = "req~plain_markdown~1";
        final String quotedSpecificationId = "req~quoted_markdown~1";
        myFixture.addFileToProject("doc/spec.md", """
                req~plain_markdown~1
                Needs: dsn

                `req~quoted_markdown~1`
                Needs: dsn
                """);

        assertAll(
                () -> assertThat(searchSymbolMatches(plainSpecificationId), contains(plainSpecificationId)),
                () -> assertThat(searchSymbolMatches(quotedSpecificationId), contains(quotedSpecificationId))
        );
    }

    // [itest->dsn~open-specification-item-from-go-to-symbol~1]
    public void testGivenGoToSymbolResultWhenUserSelectsDeclarationThenEditorOpensTheDeclarationAnchor() {
        final String specificationId = "req~openfasttrace_navigation_target~1";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);

        final NavigationItem item = new OftChooseByNameContributor().getItemsByName(
                specificationId,
                specificationId,
                getProject(),
                false
        )[0];

        EdtTestUtil.runInEdtAndWait(() -> item.navigate(true));

        assertThat(selectedEditorLocation(), is(new EditorLocation("spec.md", 0)));
    }

    // [itest->dsn~open-specification-item-from-search-everywhere~1]
    public void testGivenSearchEverywhereSymbolsResultWhenUserSelectsDeclarationThenEditorOpensTheDeclarationAnchor() {
        final String specificationId = "req~openfasttrace_navigation_target~1";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiFile contextFile = myFixture.addFileToProject("src/Main.java", "class Main {}");
        myFixture.configureFromExistingVirtualFile(contextFile.getVirtualFile());

        final NavigationItem item = searchSymbolItems(specificationId).getFirst();

        EdtTestUtil.runInEdtAndWait(() -> item.navigate(true));

        assertThat(selectedEditorLocation(), is(new EditorLocation("spec.md", 0)));
    }

    // [itest->dsn~open-specification-item-from-coverage-definition~1]
    public void testGivenCoverageDefinitionWhenGoToDeclarationInvokesOnCoveredItemThenEditorOpensTheDeclarationAnchor() {
        specificationFileSuffixes().forEach(fileSuffix ->
                assertCoverageDefinitionNavigation(fileSuffix, "plain")
        );
    }

    // [itest->dsn~stay-on-specification-item-declaration~1]
    public void testGivenDeclarationAnchorWhenGoToDeclarationInvokesThenEditorStaysOnThatDeclaration() {
        final PsiFile declarationFile = myFixture.addFileToProject("doc/spec.md", """
                <caret>req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        myFixture.configureFromExistingVirtualFile(declarationFile.getVirtualFile());

        EdtTestUtil.runInEdtAndWait(() -> myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION));

        assertThat(selectedEditorLocation(), is(new EditorLocation("spec.md", 0)));
    }

    // [itest->dsn~show-covering-occurrences-from-declaration~1]
    public void testGivenDeclarationWithCoverageOccurrencesWhenGoToImplementationsSearchesThenCoverageDefinitionAndCoverageTagAreReturned() {
        final PsiFile declarationFile = myFixture.addFileToProject("doc/spec.md", """
                <caret>req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        final String coverageTag = "[impl" + "~openfasttrace_navigation_target~1->req~openfasttrace_navigation_target~1]";
        myFixture.addFileToProject("src/Main.java", """
                // %s
                class Main {
                }
                """.formatted(coverageTag));
        myFixture.configureFromExistingVirtualFile(declarationFile.getVirtualFile());

        assertThat(implementationTargetFilesAtCaret(), containsInAnyOrder("Main.java", "impl.md"));
    }

    // [itest->dsn~open-specification-item-from-coverage-tag-left-side~1]
    public void testGivenCoverageTagLeftSideWhenGoToDeclarationInvokesThenEditorOpensTheCoveringDeclaration() {
        myFixture.addFileToProject("doc/impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: impl
                """);
        final PsiFile sourceFile = myFixture.addFileToProject("src/Main.java", """
                // [<caret>impl->req~openfasttrace_navigation_target~1]
                class Main {
                }
                """);
        myFixture.configureFromExistingVirtualFile(sourceFile.getVirtualFile());

        EdtTestUtil.runInEdtAndWait(() -> myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION));

        assertThat(selectedEditorLocation(), is(new EditorLocation("impl.md", 0)));
    }

    // [itest->dsn~open-specification-item-from-coverage-tag-right-side~1]
    public void testGivenCoverageTagRightSideWhenGoToDeclarationInvokesThenEditorOpensTheCoveredDeclaration() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiFile sourceFile = myFixture.addFileToProject("src/Main.java", """
                // [impl-><caret>req~openfasttrace_navigation_target~1]
                class Main {
                }
                """);
        myFixture.configureFromExistingVirtualFile(sourceFile.getVirtualFile());

        EdtTestUtil.runInEdtAndWait(() -> myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION));

        assertThat(selectedEditorLocation(), is(new EditorLocation("spec.md", 0)));
    }

    public void testGivenCoverageTagLeftSideWhenGotoDeclarationHandlerReceivesNullSourceElementThenItStillResolvesTheCoveringDeclaration() {
        myFixture.addFileToProject("doc/impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        final PsiFile sourceFile = myFixture.addFileToProject("src/Main.java", """
                // [<caret>impl->req~openfasttrace_navigation_target~1]
                class Main {
                }
                """);
        myFixture.configureFromExistingVirtualFile(sourceFile.getVirtualFile());

        final Editor editor = myFixture.getEditor();
        final int offset = editor.getCaretModel().getOffset();
        final PsiElement[] targets = new OftGotoDeclarationHandler().getGotoDeclarationTargets(null, offset, editor);

        assertThat(Objects.requireNonNull(targets)[0].getContainingFile().getName(), is("impl.md"));
    }

    public void testGivenCoverageTagLeftSideWhenPsiReferenceResolvesThenItOpensTheCoveringDeclaration() {
        myFixture.addFileToProject("doc/impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: impl
                """);
        final PsiFile sourceFile = myFixture.addFileToProject("src/Main.java", """
                // [<caret>impl->req~openfasttrace_navigation_target~1]
                class Main {
                }
                """);
        myFixture.configureFromExistingVirtualFile(sourceFile.getVirtualFile());

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());

        assertThat(Objects.requireNonNull(Objects.requireNonNull(reference).resolve()).getContainingFile().getName(), is("impl.md"));
    }

    public void testGivenCoverageTagRightSideWhenPsiReferenceResolvesThenItOpensTheCoveredDeclaration() {
        myFixture.addFileToProject("doc/impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: impl
                """);
        final PsiFile sourceFile = myFixture.addFileToProject("src/Main.java", """
                // [impl-><caret>req~openfasttrace_navigation_target~1]
                class Main {
                }
                """);
        myFixture.configureFromExistingVirtualFile(sourceFile.getVirtualFile());

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());

        assertThat(Objects.requireNonNull(Objects.requireNonNull(reference).resolve()).getContainingFile().getName(), is("spec.md"));
    }

    public void testGivenQuotedCoverageDefinitionWhenPsiReferenceResolvesThenItOpensTheCoveredDeclaration() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiFile referencingFile = myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - `req~openfasttrace_navigation_target~1`
                """);
        myFixture.configureFromExistingVirtualFile(referencingFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(
                myFixture.getFile().getText().indexOf("req~openfasttrace_navigation_target~1")
        );

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());

        assertThat(Objects.requireNonNull(Objects.requireNonNull(reference).resolve()).getContainingFile().getName(), is("spec.md"));
    }

    public void testGivenQuotedCoverageDefinitionWhenGotoDeclarationHandlerResolvesThenItDefersToPlatformNavigation() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiFile referencingFile = myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - `req~openfasttrace_navigation_target~1`
                """);
        myFixture.configureFromExistingVirtualFile(referencingFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(
                myFixture.getFile().getText().indexOf("req~openfasttrace_navigation_target~1")
        );

        final Editor editor = myFixture.getEditor();
        final int offset = editor.getCaretModel().getOffset();
        final PsiElement sourceElement = myFixture.getFile().findElementAt(offset);
        final PsiElement[] targets = new OftGotoDeclarationHandler().getGotoDeclarationTargets(sourceElement, offset, editor);

        assertThat(targets == null, is(true));
    }

    public void testGivenQuotedCoverageDefinitionWhenRegisteredGotoDeclarationHandlersAreQueriedThenNoHandlerResolvesTheSpecTarget() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiFile referencingFile = myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - `req~openfasttrace_navigation_target~1`
                """);
        myFixture.configureFromExistingVirtualFile(referencingFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(
                myFixture.getFile().getText().indexOf("req~openfasttrace_navigation_target~1")
        );

        final PsiElement sourceElement = myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset());
        final int offset = myFixture.getEditor().getCaretModel().getOffset();
        final List<String> handlerResults = ExtensionPointName.<GotoDeclarationHandler>create("com.intellij.gotoDeclarationHandler")
                .getExtensionList().stream()
                .map(handler -> describeHandlerResult(handler, sourceElement, offset, myFixture.getEditor()))
                .filter(result -> !result.endsWith(":[]"))
                .toList();

        assertThat(handlerResults, is(List.of()));
    }

    private List<String> searchSymbolMatches(final String pattern) {
        return searchSymbolItems(pattern).stream()
                .map(item -> ((OftNavigationItem) item).getSpecification().id())
                .toList();
    }

    private static List<String> specificationFileSuffixes() {
        return Stream.of("md", "rst").toList();
    }

    private void assertCoverageDefinitionNavigation(
            final String fileExtension,
            final String scenarioSuffix
    ) {
        final String targetId = "req~openfasttrace_navigation_target_%s_%s~1".formatted(scenarioSuffix, fileExtension);
        final PsiFile declarationFile = myFixture.addFileToProject("doc/spec-" + scenarioSuffix + "." + fileExtension, """
                %s
                Needs: dsn
                """.formatted(targetId));
        final PsiFile referencingFile = myFixture.addFileToProject("doc/design-" + scenarioSuffix + "." + fileExtension, """
                dsn~openfasttrace_navigation_design_%s~1
                Covers:
                - %s
                """.formatted(scenarioSuffix, targetId));
        myFixture.configureFromExistingVirtualFile(referencingFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(
                myFixture.getFile().getText().indexOf(targetId)
        );

        EdtTestUtil.runInEdtAndWait(() -> myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION));

        assertThat(selectedEditorLocation(), is(new EditorLocation(declarationFile.getName(), 0)));
    }

    private String describeHandlerResult(
            final GotoDeclarationHandler handler,
            final PsiElement sourceElement,
            final int offset,
            final Editor editor
    ) {
        final PsiElement[] targets = handler.getGotoDeclarationTargets(sourceElement, offset, editor);
        final List<String> fileNames = targets == null
                ? List.of()
                : Arrays.stream(targets).map(target -> target.getContainingFile().getName()).toList();
        return handler.getClass().getName() + ":" + fileNames;
    }

    private List<NavigationItem> searchSymbolItems(final String pattern) {
        final String fileName = "src/SearchContext" + searchContextCounter++ + ".java";
        final PsiFile contextFile = myFixture.addFileToProject(fileName, "class SearchContext {}");
        myFixture.configureFromExistingVirtualFile(contextFile.getVirtualFile());

        final OftChooseByNameContributor contributor = new OftChooseByNameContributor();
        final GotoSymbolModel2 model = new GotoSymbolModel2(getProject(), List.of(contributor), getTestRootDisposable());
        final List<NavigationItem> matches = new ArrayList<>();
        new DefaultChooseByNameItemProvider(myFixture.getFile()).filterElements(
                createViewModel(model),
                pattern,
                false,
                new ProgressIndicatorBase(),
                item -> {
                    matches.add((NavigationItem) item);
                    return true;
                }
        );
        return List.copyOf(matches);
    }

    private ChooseByNameViewModel createViewModel(final ChooseByNameModel model) {
        return new ChooseByNameViewModel() {
            @Override
            public Project getProject() {
                return OftNavigationTest.this.getProject();
            }

            @Override
            public @NonNull ChooseByNameModel getModel() {
                return model;
            }

            @Override
            public boolean isSearchInAnyPlace() {
                return true;
            }

            @Override
            public @NonNull String transformPattern(final @NonNull String pattern) {
                return pattern;
            }

            @Override
            public boolean canShowListForEmptyPattern() {
                return false;
            }

            @Override
            public int getMaximumListSizeLimit() {
                return 100;
            }
        };
    }

    private EditorLocation selectedEditorLocation() {
        final Editor selectedEditor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        return new EditorLocation(
                Objects.requireNonNull(FileDocumentManager.getInstance().getFile(Objects.requireNonNull(selectedEditor).getDocument())).getName(),
                selectedEditor.getCaretModel().getOffset()
        );
    }

    private List<String> implementationTargetFilesAtCaret() {
        final int offset = myFixture.getEditor().getCaretModel().getOffset();
        final PsiElement declarationElement = myFixture.getFile().findElementAt(offset);
        return DefinitionsScopedSearch.search(Objects.requireNonNull(declarationElement)).findAll().stream()
                .map(target -> target.getContainingFile().getName())
                .toList();
    }

    private record EditorLocation(String fileName, int offset) {
    }
}
