package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.jetbrains.annotations.NotNull;

class OftSpecificationIdReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private final OftSpecificationItem target;
    private final boolean renameable;

    OftSpecificationIdReference(
            final PsiElement element,
            final TextRange rangeInElement,
            final OftSpecificationItem target,
            final boolean renameable
    ) {
        super(element, rangeInElement);
        this.target = target;
        this.renameable = renameable;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(final boolean incompleteCode) {
        return OftDeclarationResolver.resolveDeclarationResults(myElement.getProject(), target);
    }

    @Override
    public PsiElement handleElementRename(final String newElementName) {
        if (!renameable) {
            return myElement;
        }
        final Project project = myElement.getProject();
        final PsiFile containingFile = myElement.getContainingFile();
        final Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
        final TextRange elementRange = myElement.getTextRange();
        if (document == null || elementRange == null) {
            return myElement;
        }
        final TextRange rangeInElement = getRangeInElement();
        final int startOffset = elementRange.getStartOffset() + rangeInElement.getStartOffset();
        final int endOffset = elementRange.getStartOffset() + rangeInElement.getEndOffset();
        document.replaceString(startOffset, endOffset, newElementName);
        PsiDocumentManager.getInstance(project).commitDocument(document);
        return myElement;
    }
}
