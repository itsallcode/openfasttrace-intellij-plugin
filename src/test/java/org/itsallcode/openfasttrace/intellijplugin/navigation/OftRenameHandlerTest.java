package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.testFramework.EdtTestUtil;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

// [itest->dsn~specification-item-rename~1]
public class OftRenameHandlerTest extends AbstractOftPlatformTestCase {
    private final OftRenameHandler handler = new OftRenameHandler();

    public void testGivenCaretOnSpecificationDeclarationWhenCheckingIsAvailableThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(5);

        final DataContext dataContext = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        assertThat(handler.isAvailableOnDataContext(dataContext), is(true));
    }

    public void testGivenCaretOnBacktickDeclarationWhenCheckingIsAvailableThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                `req~test_item~1`
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(6);

        final DataContext dataContext = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        assertThat(handler.isAvailableOnDataContext(dataContext), is(true));
    }

    public void testGivenCaretAdjacentToBackticksWhenCheckingIsAvailableThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                `req~test_item~1`
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());

        myFixture.getEditor().getCaretModel().moveToOffset(0);
        final DataContext dataContextBeforeBacktick = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        myFixture.getEditor().getCaretModel().moveToOffset(17);
        final DataContext dataContextAfterBacktick = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        assertAll(
                () -> assertThat(handler.isAvailableOnDataContext(dataContextBeforeBacktick), is(true)),
                () -> assertThat(handler.isAvailableOnDataContext(dataContextAfterBacktick), is(true))
        );
    }

    public void testGivenCaretOnNonDeclarationTextInMarkdownWhenCheckingIsAvailableThenItReturnsFalse() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                # Header Title

                req~test_item~1
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(4);

        final DataContext dataContext = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        assertThat(handler.isAvailableOnDataContext(dataContext), is(false));
    }

    public void testGivenCaretInNonSpecificationFileWhenCheckingIsAvailableThenItReturnsFalse() {
        final PsiFile javaFile = myFixture.addFileToProject("src/Main.java", """
                // req~test_item~1
                class Main {}
                """);
        myFixture.configureFromExistingVirtualFile(javaFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(5);

        final DataContext dataContext = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        assertThat(handler.isAvailableOnDataContext(dataContext), is(false));
    }

    public void testGivenEmptyDataContextWhenCheckingIsAvailableThenItReturnsFalse() {
        assertThat(handler.isAvailableOnDataContext(DataContext.EMPTY_CONTEXT), is(false));
    }

    public void testGivenDataContextWithDeclarationElementWhenCheckingIsAvailableThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));
        final OftDeclarationNavigationElement navElement = new OftDeclarationNavigationElement(
                declarationElement,
                new OftIndexedSpecification("req", "test_item", 1, 0)
        );

        final DataContext dataContextWithPsiElement = SimpleDataContext.builder()
                .add(CommonDataKeys.PSI_ELEMENT, navElement)
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        final DataContext dataContextWithPsiElementArray = SimpleDataContext.builder()
                .add(PlatformCoreDataKeys.PSI_ELEMENT_ARRAY, new PsiElement[]{navElement})
                .add(CommonDataKeys.PROJECT, getProject())
                .build();

        assertAll(
                () -> assertThat(handler.isAvailableOnDataContext(dataContextWithPsiElement), is(true)),
                () -> assertThat(handler.isAvailableOnDataContext(dataContextWithPsiElementArray), is(true))
        );
    }

    public void testGivenCaretOnDeclarationWhenInvokingRenameViaHandlerThenDeclarationAndReferencesAreUpdated() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiFile designFile = myFixture.addFileToProject("doc/design.md", """
                dsn~design_item~1
                Covers:
                - req~test_item~1
                """);
        final PsiFile javaFile = myFixture.addFileToProject("src/Main.java",
                "// [" + "impl->req~test_item~1]\n"
                        + "class Main {}\n"
        );

        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(4);

        final DataContext dataContext = SimpleDataContext.builder()
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .add(CommonDataKeys.PROJECT, getProject())
                .add(PsiElementRenameHandler.DEFAULT_NAME, "req~renamed_item~2")
                .build();

        EdtTestUtil.runInEdtAndWait(() ->
                handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), dataContext)
        );

        assertAll(
                () -> assertThat(specFile.getText(), containsString("req~renamed_item~2")),
                () -> assertThat(specFile.getText(), not(containsString("req~test_item~1"))),
                () -> assertThat(designFile.getText(), containsString("req~renamed_item~2")),
                () -> assertThat(javaFile.getText(), containsString("[" + "impl->req~renamed_item~2]"))
        );
    }

    public void testGivenCaretOnDeclarationWhenRenamingAtCaretUsingHandlerThenRefactoringSucceeds() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                `feat~excuse_of_the_day~1`
                Needs: dsn
                """);
        final PsiFile designFile = myFixture.addFileToProject("doc/design.md", """
                dsn~excuse_design~1
                Covers:
                - feat~excuse_of_the_day~1
                """);

        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(5);

        EdtTestUtil.runInEdtAndWait(() ->
                myFixture.renameElementAtCaretUsingHandler("feat~better_excuse~2")
        );

        assertAll(
                () -> assertThat(specFile.getText(), containsString("`feat~better_excuse~2`")),
                () -> assertThat(specFile.getText(), not(containsString("feat~excuse_of_the_day~1"))),
                () -> assertThat(designFile.getText(), containsString("feat~better_excuse~2"))
        );
    }

    public void testGivenElementsArrayWhenInvokingRenameThenDeclarationAndReferencesAreUpdated() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiFile designFile = myFixture.addFileToProject("doc/design.md", """
                dsn~design_item~1
                Covers:
                - req~test_item~1
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

        final DataContext dataContext = SimpleDataContext.builder()
                .add(CommonDataKeys.PROJECT, getProject())
                .add(PsiElementRenameHandler.DEFAULT_NAME, "req~renamed_item~2")
                .build();

        EdtTestUtil.runInEdtAndWait(() ->
                handler.invoke(getProject(), new PsiElement[]{declarationElement}, dataContext)
        );

        assertAll(
                () -> assertThat(specFile.getText(), containsString("req~renamed_item~2")),
                () -> assertThat(designFile.getText(), containsString("req~renamed_item~2"))
        );
    }
}
