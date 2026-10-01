package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.openapi.vfs.VirtualFile;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.syntax.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// [impl->dsn~specification-item-rename~1]
// [impl->dsn~rename-specification-item-id~1]
// [impl->dsn~show-renamed-specification-item-id-in-navigation~1]
public final class OftRenamePsiElementProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(final @NonNull PsiElement element) {
        if (element instanceof PsiFileSystemItem) {
            return false;
        }
        return OftDeclarationResolver.findDeclaredItem(element).isPresent();
    }

    @Override
    public PsiElement substituteElementToRename(final @NonNull PsiElement element, final com.intellij.openapi.editor.Editor editor) {
        if (element instanceof OftDeclarationNavigationElement) {
            return element;
        }
        if (editor == null || !isSpecificationElement(element)) {
            return element;
        }
        final var virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return element;
        }
        final int offset = editor.getCaretModel().getOffset();
        final CharSequence fileText = element.getContainingFile().getViewProvider().getContents();
        for (OftSpecificationItemMatch match : OftSyntaxCore.findDefinitionSpecificationItems(fileText)) {
            final int start = match.span().startOffset();
            final int end = match.span().endOffset();
            final int startBound = (start > 0 && fileText.charAt(start - 1) == '`') ? start - 1 : start;
            final int endBound = (end < fileText.length() && fileText.charAt(end) == '`') ? end + 1 : end;
            if (offset >= startBound && offset <= endBound) {
                final PsiElement target = OftDeclarationResolver.findPsiElementAt(
                        PsiManager.getInstance(element.getProject()),
                        virtualFile,
                        start
                );
                final OftIndexedSpecification spec = new OftIndexedSpecification(
                        match.item().artifactType(),
                        match.item().name(),
                        match.item().revision(),
                        start
                );
                return new OftDeclarationNavigationElement(
                        target != null ? target : element,
                        spec
                );
            }
        }
        return element;
    }

    @Override
    public void renameElement(
            final @NonNull PsiElement element,
            final @NonNull String newName,
            final UsageInfo @Nullable [] usages,
            final RefactoringElementListener listener
    ) throws IncorrectOperationException {
        final OftSpecificationItem oldItem = OftDeclarationResolver.findDeclaredItem(element)
                .orElseThrow(() -> new IncorrectOperationException(
                        "OpenFastTrace specification item declaration not found at rename target."
                ));
        if (OftSyntaxCore.classifySpecificationItem(newName) != OftFragmentStatus.VALID) {
            throw new IncorrectOperationException("Invalid OpenFastTrace specification item ID: " + newName);
        }
        final Project project = element.getProject();
        final PsiFile psiFile = element.getContainingFile();
        final String oldName = oldItem.id();
        replaceDeclarationText(project, psiFile, oldName, newName);
        updateProjectReferences(project, oldName, newName);
        if (usages != null) {
            Arrays.stream(usages)
                    .map(UsageInfo::getReference)
                    .filter(Objects::nonNull)
                    .forEach(reference -> replaceReferenceText(reference, newName));
        }
    }

    private static void replaceDeclarationText(
            final Project project,
            final PsiFile psiFile,
            final String oldName,
            final String newText
    ) {
        if (psiFile == null) {
            return;
        }
        final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }
        final CharSequence fileText = psiFile.getViewProvider().getContents();
        final List<OftTextSpan> spans = new ArrayList<>();
        for (OftSpecificationItemMatch match : OftSyntaxCore.findDefinitionSpecificationItems(fileText)) {
            if (oldName.equals(match.item().id())) {
                spans.add(match.span());
            }
        }
        spans.sort(Comparator.comparingInt(OftTextSpan::startOffset).reversed());
        for (OftTextSpan span : spans) {
            replaceSpan(document, span, newText);
        }
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    private static void replaceReferenceText(final PsiReference reference, final String newText) {
        reference.handleElementRename(newText);
    }

    private static void updateProjectReferences(final Project project, final String oldName, final String newName) {
        ProjectFileIndex.getInstance(project).iterateContent(file -> {
            updateFileIfRelevant(project, file, oldName, newName);
            return true;
        });
    }

    private static void updateFileIfRelevant(
            final Project project,
            final VirtualFile file,
            final String oldName,
            final String newName
    ) {
        if (!file.isValid()) {
            return;
        }
        if (file.isDirectory()) {
            for (VirtualFile child : file.getChildren()) {
                updateFileIfRelevant(project, child, oldName, newName);
            }
            return;
        }
        final String fileName = file.getName();
        if (OftSupportedFiles.isSpecificationFileName(fileName)) {
            updateSpecificationFile(project, file, oldName, newName);
        } else if (OftSupportedFiles.isCoverageTagFileName(fileName)) {
            updateCoverageTagFile(project, file, oldName, newName);
        }
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
        final List<OftTextSpan> spans = new ArrayList<>();
        for (OftSpecificationItemMatch match : OftDeclarationResolver.findCoveredSpecificationItems(
                psiFile.getViewProvider().getContents()
        )) {
            if (oldName.equals(match.item().id())) {
                spans.add(match.span());
            }
        }
        spans.sort(Comparator.comparingInt(OftTextSpan::startOffset).reversed());
        for (OftTextSpan span : spans) {
            replaceSpan(document, span, newName);
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
        final List<OftTextSpan> spans = new ArrayList<>();
        for (OftCoverageTagMatch match : OftSyntaxCore.findCoverageTags(fileText)) {
            if (oldName.equals(match.tag().target().id())) {
                spans.add(match.targetSpan());
            }
            if (oldName.equals(match.tag().effectiveSource().id())
                    && isFullIdText(fileText, match.sourceSpan())) {
                spans.add(match.sourceSpan());
            }
        }
        spans.sort(Comparator.comparingInt(OftTextSpan::startOffset).reversed());
        for (OftTextSpan span : spans) {
            replaceSpan(document, span, newName);
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
