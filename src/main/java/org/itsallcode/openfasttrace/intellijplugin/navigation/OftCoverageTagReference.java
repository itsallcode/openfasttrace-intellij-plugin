package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.jetbrains.annotations.NotNull;

final class OftCoverageTagReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private final OftSpecificationItem target;

    OftCoverageTagReference(
            final PsiElement element,
            final TextRange rangeInElement,
            final OftSpecificationItem target
    ) {
        super(element, rangeInElement);
        this.target = target;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(final boolean incompleteCode) {
        return OftDeclarationResolver.resolveDeclarationResults(myElement.getProject(), target);
    }
}
