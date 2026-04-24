package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.EdtTestUtil;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// [itest->dsn~specification-item-navigation-runtime~1]
public class OftNavigationTest extends AbstractOftPlatformTestCase {
    public void testGoToSymbolReturnsProjectLocalSpecificationItems() {
        final String uniqueName = "openfasttrace_navigation_test_item";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_test_item~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/design.rst", """
                dsn~openfasttrace_navigation_test_item~2
                Covers:
                - req~openfasttrace_navigation_test_item~1
                """);

        final String[] names = new OftChooseByNameContributor().getNames(getProject(), false);

        assertThat(Arrays.asList(names), hasItem(uniqueName));
    }

    public void testGoToSymbolMatchesBySpecificationNameAndListsFullIds() {
        final String uniqueName = "openfasttrace_navigation_test_item";
        final String expectedId = "req~" + uniqueName + "~1";
        final String secondExpectedId = "dsn~" + uniqueName + "~2";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_test_item~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/design.rst", """
                dsn~openfasttrace_navigation_test_item~2
                Covers:
                - req~openfasttrace_navigation_test_item~1
                """);
        final PsiFile contextFile = myFixture.addFileToProject("src/Main.java", "class Main {}");
        myFixture.configureFromExistingVirtualFile(contextFile.getVirtualFile());

        final OftChooseByNameContributor contributor = new OftChooseByNameContributor();
        final GotoSymbolModel2 model = new GotoSymbolModel2(getProject(), List.of(contributor), getTestRootDisposable());
        final List<Object> matches = new ArrayList<>();
        final boolean completed = new DefaultChooseByNameItemProvider(myFixture.getFile()).filterElements(
                createViewModel(model),
                uniqueName,
                false,
                new ProgressIndicatorBase(),
                item -> {
                    matches.add(item);
                    return true;
                }
        );

        assertThat(completed, is(true));
        assertThat(matches.stream()
                .map(OftNavigationItem.class::cast)
                .map(item -> item.getSpecification().id())
                .toList(), containsInAnyOrder(expectedId, secondExpectedId));

        final NavigationItem[] items = contributor.getItemsByName(uniqueName, uniqueName, getProject(), false);
        assertThat(items.length, is(2));

        final OftNavigationItem item = Arrays.stream(items)
                .map(OftNavigationItem.class::cast)
                .filter(candidate -> candidate.getSpecification().id().equals(expectedId))
                .findFirst()
                .orElse(null);
        assertThat(item, is(notNullValue()));

        EdtTestUtil.runInEdtAndWait(() -> item.navigate(true));

        final Editor selectedEditor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        assertThat(selectedEditor, is(notNullValue()));
        assertThat(FileDocumentManager.getInstance().getFile(selectedEditor.getDocument()).getName(), is("spec.md"));
        assertThat(selectedEditor.getCaretModel().getOffset(), is(0));
    }

    public void testGoToSymbolSupportsSpecificationNamesWithDashes() {
        final String uniqueName = "openfasttrace-navigation-test-item";
        final String expectedId = "req~" + uniqueName + "~1";
        final String secondExpectedId = "dsn~" + uniqueName + "~2";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace-navigation-test-item~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/design.rst", """
                dsn~openfasttrace-navigation-test-item~2
                Covers:
                - req~openfasttrace-navigation-test-item~1
                """);
        final PsiFile contextFile = myFixture.addFileToProject("src/Main.java", "class Main {}");
        myFixture.configureFromExistingVirtualFile(contextFile.getVirtualFile());

        final String[] names = new OftChooseByNameContributor().getNames(getProject(), false);
        assertThat(Arrays.asList(names), hasItem(uniqueName));

        final OftChooseByNameContributor contributor = new OftChooseByNameContributor();
        final GotoSymbolModel2 model = new GotoSymbolModel2(getProject(), List.of(contributor), getTestRootDisposable());
        final List<Object> matches = new ArrayList<>();
        final boolean completed = new DefaultChooseByNameItemProvider(myFixture.getFile()).filterElements(
                createViewModel(model),
                uniqueName,
                false,
                new ProgressIndicatorBase(),
                item -> {
                    matches.add(item);
                    return true;
                }
        );

        assertThat(completed, is(true));
        assertThat(matches.stream()
                .map(OftNavigationItem.class::cast)
                .map(item -> item.getSpecification().id())
                .toList(), containsInAnyOrder(expectedId, secondExpectedId));
    }

    private ChooseByNameViewModel createViewModel(final ChooseByNameModel model) {
        return new ChooseByNameViewModel() {
            @Override
            public Project getProject() {
                return OftNavigationTest.this.getProject();
            }

            @Override
            public ChooseByNameModel getModel() {
                return model;
            }

            @Override
            public boolean isSearchInAnyPlace() {
                return true;
            }

            @Override
            public String transformPattern(final String pattern) {
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

    public void testGoToDeclarationFromSpecificationReferenceOpensDefinition() throws Exception {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        final PsiFile referencingFile = myFixture.addFileToProject("doc/design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - <caret>req~openfasttrace_navigation_target~1
                """);
        myFixture.configureFromExistingVirtualFile(referencingFile.getVirtualFile());

        EdtTestUtil.runInEdtAndWait(() -> myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION));

        final Editor selectedEditor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        assertThat(selectedEditor, is(notNullValue()));
        assertThat(FileDocumentManager.getInstance().getFile(selectedEditor.getDocument()).getName(), is("spec.md"));
        assertThat(selectedEditor.getCaretModel().getOffset(), is(0));
    }

    public void testGoToDeclarationFromCoverageTagOpensDefinition() throws Exception {
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

        final Editor selectedEditor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        assertThat(selectedEditor, is(notNullValue()));
        assertThat(FileDocumentManager.getInstance().getFile(selectedEditor.getDocument()).getName(), is("spec.md"));
        assertThat(selectedEditor.getCaretModel().getOffset(), is(0));
    }

    public void testGoToDeclarationFromCoverageTagLeftSideOpensCoveringDefinition() throws Exception {
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

        final Editor selectedEditor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        assertThat(selectedEditor, is(notNullValue()));
        assertThat(FileDocumentManager.getInstance().getFile(selectedEditor.getDocument()).getName(), is("impl.md"));
        assertThat(selectedEditor.getCaretModel().getOffset(), is(0));
    }

    public void testGotoDeclarationHandlerResolvesCoverageTagWithNullSourceElement() {
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

        assertThat(targets, is(notNullValue()));
        assertThat(targets.length, is(1));
        assertThat(targets[0].getContainingFile().getName(), is("impl.md"));
    }

    public void testCoverageTagLeftSideProvidesPsiReferenceThatResolvesToCoveringItem() {
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

        final int offset = myFixture.getEditor().getCaretModel().getOffset();
        final PsiElement elementAtOffset = myFixture.getFile().findElementAt(offset);
        final PsiReference reference = myFixture.getFile().findReferenceAt(offset);

        assertThat(elementAtOffset, is(notNullValue()));
        assertThat(elementAtOffset.getReferences().length > 0, is(true));
        assertThat(reference, is(notNullValue()));
        assertThat(reference.resolve(), is(notNullValue()));
        assertThat(reference.resolve().getContainingFile().getName(), is("impl.md"));
    }

    public void testCoverageTagRightSideProvidesPsiReferenceThatResolvesToCoveredItem() {
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

        final int offset = myFixture.getEditor().getCaretModel().getOffset();
        final PsiElement elementAtOffset = myFixture.getFile().findElementAt(offset);
        final PsiReference reference = myFixture.getFile().findReferenceAt(offset);

        assertThat(elementAtOffset, is(notNullValue()));
        assertThat(elementAtOffset.getReferences().length > 0, is(true));
        assertThat(reference, is(notNullValue()));
        assertThat(reference.resolve(), is(notNullValue()));
        assertThat(reference.resolve().getContainingFile().getName(), is("spec.md"));
    }
}
