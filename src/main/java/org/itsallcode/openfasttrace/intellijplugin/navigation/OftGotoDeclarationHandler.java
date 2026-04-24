package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.jetbrains.annotations.Nullable;

public final class OftGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    // [impl->dsn~open-specification-item-from-coverage-definition~1]
    // [impl->dsn~stay-on-specification-item-declaration~1]
    // [impl->dsn~open-specification-item-from-coverage-tag-left-side~1]
    // [impl->dsn~open-specification-item-from-coverage-tag-right-side~1]
    public PsiElement @Nullable [] getGotoDeclarationTargets(
            final PsiElement sourceElement,
            final int offset,
            final Editor editor
    ) {
        if (sourceElement != null
                && sourceElement.getContainingFile() != null
                && sourceElement.getContainingFile().getVirtualFile() != null
                && OftSupportedFiles.isSpecificationFile(sourceElement.getContainingFile().getVirtualFile())
                && OftDeclarationResolver.findDeclaredItem(sourceElement).isPresent()) {
            return null;
        }
        final Project project = sourceElement != null ? sourceElement.getProject() : editor.getProject();
        if (project == null) {
            return null;
        }
        final CharSequence text = editor.getDocument().getCharsSequence();
        return OftDeclarationResolver.findReferenceAt(text, offset)
                .map(reference -> OftDeclarationResolver.resolveDeclarations(project, reference))
                .filter(targets -> targets.length > 0)
                .orElse(null);
    }
}
