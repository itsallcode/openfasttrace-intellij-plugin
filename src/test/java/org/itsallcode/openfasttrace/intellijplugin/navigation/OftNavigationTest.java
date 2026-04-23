package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// [itest->dsn~specification-item-navigation-runtime~1]
public class OftNavigationTest extends AbstractOftPlatformTestCase {
    public void testGoToSymbolReturnsProjectLocalSpecificationItems() throws Exception {
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

        final OftChooseByNameContributor contributor = new OftChooseByNameContributor();

        assertThat(Arrays.asList(contributor.getNames(getProject(), false)), hasItem(expectedId));
        assertThat(Arrays.asList(contributor.getNames(getProject(), false)), hasItem(secondExpectedId));

        final NavigationItem[] items = contributor.getItemsByName(expectedId, expectedId, getProject(), false);
        assertThat(items.length, is(1));

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
}
