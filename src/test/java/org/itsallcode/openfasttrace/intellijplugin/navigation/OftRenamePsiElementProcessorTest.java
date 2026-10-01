package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
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

    public void testGivenSpecificationFileWhenCheckingCanProcessThenItReturnsFalse() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);

        assertThat(processor.canProcessElement(specFile), is(false));
    }

    public void testGivenDirectoryWhenCheckingCanProcessThenItReturnsFalse() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);

        assertThat(processor.canProcessElement(Objects.requireNonNull(specFile.getParent())), is(false));
    }

    public void testGivenSpecificationDeclarationElementWhenCheckingCanProcessThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

        assertThat(processor.canProcessElement(declarationElement), is(true));
    }

    public void testGivenDeclarationNavigationElementWhenCheckingCanProcessThenItReturnsTrue() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));
        final OftDeclarationNavigationElement navigationElement = new OftDeclarationNavigationElement(
                declarationElement,
                new org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification(
                        "req",
                        "test_item",
                        1,
                        0
                )
        );

        assertThat(processor.canProcessElement(navigationElement), is(true));
    }

    public void testGivenNonDeclarationElementInSpecificationFileWhenCheckingCanProcessThenItReturnsFalse() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                Some leading documentation text.

                req~test_item~1
                Needs: dsn
                """);
        final PsiElement textElement = Objects.requireNonNull(specFile.findElementAt(5));

        assertThat(processor.canProcessElement(textElement), is(false));
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
                () -> assertThat(processor.canProcessElement(Objects.requireNonNull(substituted)), is(true))
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
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

        final IncorrectOperationException exception = Assertions.assertThrows(
                IncorrectOperationException.class,
                () -> processor.renameElement(declarationElement, "invalid_specification_id", null, null)
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
        final String tag1 = "[impl" + "~service~1->req~rename_target~1]";
        final String tag2 = "[" + "req~rename_target~1->req~other~1]";
        final PsiFile javaFile = myFixture.addFileToProject("src/sub/pkg/Service.java", """
                // %s
                // %s
                class Service {}
                """.formatted(tag2, tag1));
        myFixture.configureFromExistingVirtualFile(specFile.getVirtualFile());
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

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
                        declarationElement,
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

    public void testGivenFileRenameForMarkdownJavaAndTextFilesThenProcessorDoesNotInterfere() {
        final PsiFile markdownFile = myFixture.addFileToProject("doc/spec.md", """
                req~rename_target~1
                Needs: dsn
                """);
        final PsiFile javaFile = myFixture.addFileToProject("src/Main.java", "class Main {}");
        final PsiFile textFile = myFixture.addFileToProject("doc/notes.txt", "Some plain notes.");

        Assertions.assertAll(
                () -> assertThat(processor.canProcessElement(markdownFile), is(false)),
                () -> assertThat(processor.canProcessElement(javaFile), is(false)),
                () -> assertThat(processor.canProcessElement(textFile), is(false))
        );

        myFixture.renameElement(markdownFile, "renamed-spec.md");
        myFixture.renameElement(javaFile, "RenamedMain.java");
        myFixture.renameElement(textFile, "renamed-notes.txt");

        Assertions.assertAll(
                () -> assertThat(markdownFile.getName(), is("renamed-spec.md")),
                () -> assertThat(javaFile.getName(), is("RenamedMain.java")),
                () -> assertThat(textFile.getName(), is("renamed-notes.txt"))
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

    public void testGivenDeclarationNavigationElementWhenSubstitutingElementThenReturnsSameInstance() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~test_item~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));
        final OftDeclarationNavigationElement navElement = new OftDeclarationNavigationElement(
                declarationElement,
                new org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification(
                        "req", "test_item", 1, 0
                )
        );

        final PsiElement substituted = processor.substituteElementToRename(navElement, myFixture.getEditor());

        assertThat(substituted, sameInstance(navElement));
    }

    public void testGivenInMemorySpecificationFileWithoutVirtualFileWhenSubstitutingElementThenReturnsOriginalElement() {
        final PsiFile inMemorySpec = PsiFileFactory.getInstance(getProject())
                .createFileFromText("spec.md", PlainTextFileType.INSTANCE, "req~test~1\n");
        final PsiElement substituted = processor.substituteElementToRename(inMemorySpec, myFixture.getEditor());
        assertThat(substituted, sameInstance(inMemorySpec));
    }

    public void testGivenNullUsagesArrayWhenRenamingThenRefactoringSucceeds() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~null_usages_target~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

        EdtTestUtil.runInEdtAndWait(() -> WriteCommandAction.runWriteCommandAction(
                getProject(),
                () -> processor.renameElement(declarationElement, "req~renamed_null_usages~1", null, null)
        ));

        assertThat(documentText(specFile), containsString("req~renamed_null_usages~1"));
    }

    public void testGivenUsagesArrayWithNonNullAndNullReferencesWhenRenamingThenOnlyNonNullReferencesAreHandled() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~multi_usage_target~1
                Needs: dsn
                """);
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

        final OftSpecificationItem targetItem = new OftSpecificationItem("req", "multi_usage_target", 1);
        final OftSpecificationIdReference reference = new OftSpecificationIdReference(
                specFile,
                new TextRange(0, "req~multi_usage_target~1".length()),
                targetItem,
                true
        );
        final UsageInfo usageWithRef = new UsageInfo(specFile) {
            @Override
            public com.intellij.psi.PsiReference getReference() {
                return reference;
            }
        };
        final UsageInfo usageWithoutRef = new UsageInfo(declarationElement);

        EdtTestUtil.runInEdtAndWait(() -> WriteCommandAction.runWriteCommandAction(
                getProject(),
                () -> processor.renameElement(
                        declarationElement,
                        "req~renamed_multi_usage~1",
                        new UsageInfo[]{usageWithRef, usageWithoutRef},
                        null
                )
        ));

        assertThat(documentText(specFile), containsString("req~renamed_multi_usage~1"));
    }

    public void testGivenCoverageTagsWithFullIdSourceAndShortSourceWhenRenamingThenOnlyMatchingFullIdSourceIsRenamed() {
        final PsiFile specFile = myFixture.addFileToProject("doc/spec.md", """
                req~full_id_source~1
                Needs: dsn
                """);
        final String tagA = "[" + "req~full_id_source~1->dsn~target~1]";
        final String tagB = "[impl" + "->req~full_id_source~1]";
        final String tagC = "[" + "req->req~other_target~1]";
        final PsiFile javaFile = myFixture.addFileToProject("src/Source.java", """
                // %s
                // %s
                // %s
                class Source {}
                """.formatted(tagA, tagB, tagC));
        final PsiElement declarationElement = Objects.requireNonNull(specFile.findElementAt(0));

        EdtTestUtil.runInEdtAndWait(() -> WriteCommandAction.runWriteCommandAction(
                getProject(),
                () -> processor.renameElement(declarationElement, "req~renamed_source~2", null, null)
        ));

        Assertions.assertAll(
                () -> assertThat(documentText(specFile), containsString("req~renamed_source~2")),
                () -> assertThat(documentText(javaFile), containsString("[" + "req~renamed_source~2->dsn~target~1]")),
                () -> assertThat(documentText(javaFile), containsString("[" + "impl->req~renamed_source~2]")),
                () -> assertThat(documentText(javaFile), containsString("[" + "req->req~other_target~1]"))
        );
    }

    private String documentText(final PsiFile file) {
        final Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) {
            throw new IllegalStateException("Missing document for " + file.getVirtualFile().getPath());
        }
        return document.getText();
    }
}
