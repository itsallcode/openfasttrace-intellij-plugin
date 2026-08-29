package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;

final class OftCoverageTagReference extends OftSpecificationIdReference {
    OftCoverageTagReference(
            final PsiElement element,
            final TextRange rangeInElement,
            final OftSpecificationItem target,
            final boolean renameable
    ) {
        super(element, rangeInElement, target, renameable);
    }
}
