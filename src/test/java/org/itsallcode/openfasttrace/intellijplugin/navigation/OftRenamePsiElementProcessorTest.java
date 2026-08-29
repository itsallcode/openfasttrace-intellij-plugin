package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

// [itest->dsn~rename-specification-item-id~1]
public class OftRenamePsiElementProcessorTest extends AbstractOftPlatformTestCase {
    private final OftRenamePsiElementProcessor processor = new OftRenamePsiElementProcessor();

    public void testGivenSpecificationElementWhenCheckingCanProcessThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);

        assertThat(processor.canProcessElement(specFile), is(true));
    }

    public void testGivenNonSpecificationElementWhenCheckingCanProcessThenItReturnsFalse() {
        final PsiFile javaFile = myFixture.addFileToProject("src/Main.java", "class Main {}");

        assertThat(processor.canProcessElement(javaFile), is(false));
    }

    public void testGivenNullEditorWhenSubstitutingElementThenItReturnsOriginalElement() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);

        assertThat(processor.substituteElementToRename(specFile, null), sameInstance(specFile));
    }

    public void testGivenNonSpecificationFileWhenSubstitutingElementThenItReturnsOriginalElement() {
        final PsiFile javaFile = myFixture.addFileToProject("src/Main.java", "class Main {}");
        myFixture.configureFromExistingVirtualFile(javaFile.getVirtualFile());

        assertThat(processor.substituteElementToRename(javaFile, myFixture.getEditor()), sameInstance(javaFile));
    }

    public void testGivenCaretOnSpecificationDefinitionWhenSubstitutingElementThenItReturnsResolvedElement() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~target_item~1
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(4);

        final PsiElement substituted = processor.substituteElementToRename(specFile, myFixture.getEditor());

        Assertions.assertAll(
                () -> assertThat(substituted, notNullValue()),
                () -> assertThat(processor.canProcessElement(substituted), is(true))
        );
    }

    public void testGivenCaretOutsideSpecificationDefinitionWhenSubstitutingElementThenItReturnsOriginalElement() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                Some text before
                
                req~target_item~1
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(2);

        final PsiElement substituted = processor.substituteElementToRename(specFile, myFixture.getEditor());

        assertThat(substituted, sameInstance(specFile));
    }

    public void testGivenElementWithNoDeclaredItemWhenRenamingThenItThrowsException() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", "Plain text without spec item\n");
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        final PsiElement element = Objects.requireNonNull(specFile.findElementAt(2));

        final IncorrectOperationException exception = Assertions.assertThrows(
                IncorrectOperationException.class,
                () -> processor.renameElement(element, "req~new_id~1", null, null)
        );

        assertThat(
                exception.getMessage(),
                is("OpenFastTrace specification item declaration not found at rename target.")
        );
    }

    public void testGivenInvalidNewIdWhenRenamingThenItThrowsException() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~valid_target~1
                Needs: dsn
                """);
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());

        final IncorrectOperationException exception = Assertions.assertThrows(
                IncorrectOperationException.class,
                () -> processor.renameElement(specFile, "invalid_specification_id", null, null)
        );

        assertThat(
                exception.getMessage(),
                is("Invalid OpenFastTrace specification item ID: invalid_specification_id")
        );
    }

    public void testGivenUsagesAndNestedDirectoriesWhenRenamingThenAllReferencesAndTagsUpdate() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~rename_target~1
                Needs: dsn
                """);
        final PsiFile nestedSpec = myFixture.addFileToProject("doc/sub/nested.md", """
                dsn~nested_design~1
                Covers:
                - req~rename_target~1
                """);
        final PsiFile javaFile = myFixture.addFileToProject("src/sub/pkg/Service.java",
                "// " + "[" + "req~rename_target~1->req~other~1]\n"
                        + "// " + "[" + "impl~service~1->req~rename_target~1]\n"
                        + "class Service {}\n"
        );
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());

        final OftSpecificationItem targetItem = new OftSpecificationItem("req", "rename_target", 1);
        final OftSpecificationIdReference reference = new OftSpecificationIdReference(
                nestedSpec,
                new TextRange(nestedSpec.getText().indexOf("req~rename_target~1"),
                        nestedSpec.getText().indexOf("req~rename_target~1") + "req~rename_target~1".length()),
                targetItem,
                true
        );
        final UsageInfo usageInfo = new UsageInfo(reference);

        EdtTestUtil.runInEdtAndWait(() -> WriteCommandAction.runWriteCommandAction(
                getProject(),
                () -> processor.renameElement(
                        specFile,
                        "req~renamed_item~1",
                        new UsageInfo[]{usageInfo},
                        null
                )
        ));

        Assertions.assertAll(
                () -> assertThat(documentText(specFile), containsString("req~renamed_item~1")),
                () -> assertThat(documentText(nestedSpec), containsString("req~renamed_item~1")),
                () -> assertThat(documentText(javaFile), containsString("[" + "req~renamed_item~1->req~other~1]")),
                () -> assertThat(documentText(javaFile), containsString("[" + "impl~service~1->req~renamed_item~1]"))
        );
    }

    public void testGivenUnrenameableReferenceWhenHandlingElementRenameThenItLeavesDocumentUnchanged() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~unrenameable_target~1
                Needs: dsn
                """);
        final OftSpecificationItem targetItem = new OftSpecificationItem("req", "unrenameable_target", 1);
        final OftSpecificationIdReference reference = new OftSpecificationIdReference(
                specFile,
                new TextRange(0, "req~unrenameable_target~1".length()),
                targetItem,
                false
        );

        final PsiElement result = reference.handleElementRename("req~new_target~1");

        Assertions.assertAll(
                () -> assertThat(result, sameInstance(specFile)),
                () -> assertThat(documentText(specFile), containsString("req~unrenameable_target~1"))
        );
    }

    public void testGivenRenameableReferenceWhenHandlingElementRenameThenItUpdatesDocument() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~renameable_target~1
                Needs: dsn
                """);
        final OftSpecificationItem targetItem = new OftSpecificationItem("req", "renameable_target", 1);
        final OftSpecificationIdReference reference = new OftSpecificationIdReference(
                specFile,
                new TextRange(0, "req~renameable_target~1".length()),
                targetItem,
                true
        );

        EdtTestUtil.runInEdtAndWait(() -> WriteCommandAction.runWriteCommandAction(
                getProject(),
                (Runnable) () -> reference.handleElementRename("req~renamed_item~1")
        ));

        assertThat(documentText(specFile), containsString("req~renamed_item~1"));
    }

    private String documentText(final PsiFile file) {
        final Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) {
            throw new IllegalStateException("Missing document for " + file.getVirtualFile().getPath());
        }
        return document.getText();
    }
}
