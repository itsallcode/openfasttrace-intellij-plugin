package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.openapi.vfs.VirtualFile;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftFragmentStatus;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftCoverageTagMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

// [impl->dsn~specification-item-rename~1]
public final class OftRenamePsiElementProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(final PsiElement element) {
        return isSpecificationElement(element);
    }

    @Override
    public PsiElement substituteElementToRename(final PsiElement element, final com.intellij.openapi.editor.Editor editor) {
        if (editor == null || !isSpecificationElement(element)) {
            return element;
        }
        final var virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return element;
        }
        final int offset = editor.getCaretModel().getOffset();
        final CharSequence fileText = element.getContainingFile().getViewProvider().getContents();
        return OftSyntaxCore.findDefinitionSpecificationItems(fileText).stream()
                .filter(match -> offset >= match.span().startOffset() && offset < match.span().endOffset())
                .findFirst()
                .map(match -> OftDeclarationResolver.findPsiElementAt(
                        PsiManager.getInstance(element.getProject()),
                        virtualFile,
                        match.span().startOffset()
                ))
                .orElse(element);
    }

    @Override
    public void renameElement(
            final PsiElement element,
            final String newName,
            final UsageInfo @Nullable [] usages,
            final RefactoringElementListener listener
    ) throws IncorrectOperationException {
        if (OftDeclarationResolver.findDeclaredItem(element).isEmpty()) {
            throw new IncorrectOperationException("OpenFastTrace specification item declaration not found at rename target.");
        }
        if (OftSyntaxCore.classifySpecificationItem(newName) != OftFragmentStatus.VALID) {
            throw new IncorrectOperationException("Invalid OpenFastTrace specification item ID: " + newName);
        }
        final String oldName = OftDeclarationResolver.findDeclaredItem(element)
                .map(item -> item.id())
                .orElseThrow(() -> new IncorrectOperationException(
                        "OpenFastTrace specification item declaration not found at rename target."
                ));
        replaceDeclarationText(element, oldName, newName);
        updateProjectReferences(element.getProject(), oldName, newName);
        if (usages != null) {
            Arrays.stream(usages)
                    .map(UsageInfo::getReference)
                    .filter(Objects::nonNull)
                    .forEach(reference -> replaceReferenceText(reference, newName));
        }
    }

    private static void replaceDeclarationText(
            final PsiElement element,
            final String oldName,
            final String newText
    ) {
        final PsiFile psiFile = element.getContainingFile();
        final Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(psiFile);
        if (document == null) {
            return;
        }
        final CharSequence fileText = psiFile.getViewProvider().getContents();
        for (OftSpecificationItemMatch match : OftSyntaxCore.findDefinitionSpecificationItems(fileText)) {
            if (oldName.equals(match.item().id())) {
                replaceSpan(document, match.span(), newText);
            }
        }
        PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
    }

    private static void replaceReferenceText(final PsiReference reference, final String newText) {
        reference.handleElementRename(newText);
    }

    private static void updateProjectReferences(final Project project, final String oldName, final String newName) {
        ProjectFileIndex.getInstance(project).iterateContent(file -> updateFileIfRelevant(project, file, oldName,
                newName));
    }

    private static boolean updateFileIfRelevant(
            final Project project,
            final VirtualFile file,
            final String oldName,
            final String newName
    ) {
        if (!file.isValid()) {
            return true;
        }
        if (file.isDirectory()) {
            for (VirtualFile child : file.getChildren()) {
                updateFileIfRelevant(project, child, oldName, newName);
            }
            return true;
        }
        final String fileName = file.getName();
        if (OftSupportedFiles.isSpecificationFileName(fileName)) {
            updateSpecificationFile(project, file, oldName, newName);
        } else if (OftSupportedFiles.isCoverageTagFileName(fileName)) {
            updateCoverageTagFile(project, file, oldName, newName);
        } else {
            // Intentionally empty
        }
        return true;
    }

    private static void updateSpecificationFile(
            final Project project,
            final VirtualFile file,
            final String oldName,
            final String newName
    ) {
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return;
        }
        final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }
        for (OftSpecificationItemMatch match : OftDeclarationResolver.findCoveredSpecificationItems(
                psiFile.getViewProvider().getContents()
        )) {
            if (oldName.equals(match.item().id())) {
                replaceSpan(document, match.span(), newName);
            }
        }
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    private static void updateCoverageTagFile(
            final Project project,
            final VirtualFile file,
            final String oldName,
            final String newName
    ) {
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return;
        }
        final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }
        final CharSequence fileText = psiFile.getViewProvider().getContents();
        for (OftCoverageTagMatch match : OftSyntaxCore.findCoverageTags(fileText)) {
            if (oldName.equals(match.tag().target().id())) {
                replaceSpan(document, match.targetSpan(), newName);
            }
            if (oldName.equals(match.tag().effectiveSource().id())
                    && isFullIdText(fileText, match.sourceSpan())) {
                replaceSpan(document, match.sourceSpan(), newName);
            }
        }
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    private static void replaceSpan(final Document document, final OftTextSpan span, final String newText) {
        document.replaceString(span.startOffset(), span.endOffset(), newText);
    }

    private static boolean isFullIdText(final CharSequence text, final OftTextSpan span) {
        return span.startOffset() >= 0
                && span.endOffset() <= text.length()
                && OftSyntaxCore.classifySpecificationItem(text.subSequence(span.startOffset(),
                span.endOffset()).toString())
                == OftFragmentStatus.VALID;
    }

    private static boolean isSpecificationElement(final PsiElement element) {
        return element.getContainingFile() != null
                && element.getContainingFile().getVirtualFile() != null
                && OftSupportedFiles.isSpecificationFile(element.getContainingFile().getVirtualFile());
    }
}
