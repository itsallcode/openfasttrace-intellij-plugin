package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

// [impl->dsn~specification-item-navigation-runtime~1]
public final class OftGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
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
