package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// [impl->dsn~specification-item-rename~1]
public final class OftRenameHandler implements RenameHandler {
    @Override
    public boolean isAvailableOnDataContext(final @NonNull DataContext dataContext) {
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor != null && isAvailableInEditor(editor, dataContext)) {
            return true;
        }
        final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (hasDeclaredItem(element)) {
            return true;
        }
        final PsiElement[] elements = CommonRefactoringUtil.getPsiElementArray(dataContext);
        return elements.length == 1 && hasDeclaredItem(elements[0]);
    }

    private static boolean isAvailableInEditor(final Editor editor, final DataContext dataContext) {
        PsiFile file = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (file == null) {
            final Project project = CommonDataKeys.PROJECT.getData(dataContext);
            if (project != null) {
                file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            }
        }
        if (file == null) {
            return false;
        }
        final VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null || !OftSupportedFiles.isSpecificationFile(virtualFile)) {
            return false;
        }
        final int offset = editor.getCaretModel().getOffset();
        return findDeclarationAt(file, offset).isPresent();
    }

    private static boolean hasDeclaredItem(final @Nullable PsiElement element) {
        return element != null && OftDeclarationResolver.findDeclaredItem(element).isPresent();
    }

    @Override
    public void invoke(
            final @NonNull Project project,
            final @Nullable Editor editor,
            @Nullable PsiFile file,
            final @NonNull DataContext dataContext
    ) {
        if (file == null && editor != null) {
            file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        }
        if (editor != null && file != null) {
            final int offset = editor.getCaretModel().getOffset();
            final Optional<OftDeclarationNavigationElement> declaration = findDeclarationAt(file, offset);
            if (declaration.isPresent()) {
                performRename(project, editor, declaration.get(), dataContext);
                return;
            }
        }
        final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (element != null) {
            resolveDeclarationTarget(element).ifPresent(target ->
                    performRename(project, editor, target, dataContext)
            );
            return;
        }
        final PsiElement[] elements = CommonRefactoringUtil.getPsiElementArray(dataContext);
        if (elements.length == 1 && elements[0] != null) {
            resolveDeclarationTarget(elements[0]).ifPresent(target ->
                    performRename(project, editor, target, dataContext)
            );
        }
    }

    @Override
    public void invoke(
            final @NonNull Project project,
            final PsiElement @NonNull [] elements,
            final @NonNull DataContext dataContext
    ) {
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (elements.length == 1 && elements[0] != null) {
            resolveDeclarationTarget(elements[0]).ifPresent(target ->
                    performRename(project, editor, target, dataContext)
            );
            return;
        }
        final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (element != null) {
            resolveDeclarationTarget(element).ifPresent(target ->
                    performRename(project, editor, target, dataContext)
            );
        }
    }

    private static void performRename(
            final Project project,
            final @Nullable Editor editor,
            final PsiElement target,
            final DataContext dataContext
    ) {
        final String defaultName = PsiElementRenameHandler.DEFAULT_NAME.getData(dataContext);
        if (defaultName != null) {
            PsiElementRenameHandler.rename(target, project, target, editor, defaultName);
        } else {
            PsiElementRenameHandler.rename(target, project, target, editor);
        }
    }

    static Optional<OftDeclarationNavigationElement> findDeclarationAt(
            final @Nullable PsiFile file,
            final int offset
    ) {
        if (file == null || file.getVirtualFile() == null
                || !OftSupportedFiles.isSpecificationFile(file.getVirtualFile())) {
            return Optional.empty();
        }
        final CharSequence text = file.getViewProvider().getContents();
        for (final OftSpecificationItemMatch match : OftSyntaxCore.findDefinitionSpecificationItems(text)) {
            if (isOffsetWithinBounds(text, match.span(), offset)) {
                return Optional.of(createDeclarationElement(file, match));
            }
        }
        return Optional.empty();
    }

    private static boolean isOffsetWithinBounds(
            final CharSequence text,
            final OftTextSpan span,
            final int offset
    ) {
        final int start = span.startOffset();
        final int end = span.endOffset();
        final int startBound = ((start > 0) && (text.charAt(start - 1) == '`')) ? (start - 1) : start;
        final int endBound = ((end < text.length()) && (text.charAt(end) == '`')) ? (end + 1) : end;
        return (offset >= startBound) && (offset <= endBound);
    }

    private static OftDeclarationNavigationElement createDeclarationElement(
            final PsiFile file,
            final OftSpecificationItemMatch match
    ) {
        final int start = match.span().startOffset();
        final PsiElement psiElement = file.findElementAt(start);
        final OftIndexedSpecification spec = new OftIndexedSpecification(
                match.item().artifactType(),
                match.item().name(),
                match.item().revision(),
                start
        );
        return new OftDeclarationNavigationElement(
                psiElement != null ? psiElement : file,
                spec
        );
    }

    private static Optional<PsiElement> resolveDeclarationTarget(final PsiElement element) {
        if (element instanceof OftDeclarationNavigationElement) {
            return Optional.of(element);
        }
        return OftDeclarationResolver.findDeclaredItem(element)
                .map(declaredItem -> createDeclarationNavigationElement(element, declaredItem));
    }

    private static OftDeclarationNavigationElement createDeclarationNavigationElement(
            final PsiElement element,
            final OftSpecificationItem declaredItem
    ) {
        final TextRange range = element.getTextRange();
        final int offset = range != null ? range.getStartOffset() : element.getTextOffset();
        final OftIndexedSpecification spec = new OftIndexedSpecification(
                declaredItem.artifactType(),
                declaredItem.name(),
                declaredItem.revision(),
                offset
        );
        return new OftDeclarationNavigationElement(element, spec);
    }
}
