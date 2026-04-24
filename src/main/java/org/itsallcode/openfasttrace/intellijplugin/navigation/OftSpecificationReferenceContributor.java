package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class OftSpecificationReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(),
                new SpecificationReferenceProvider()
        );
    }

    private static final class SpecificationReferenceProvider extends PsiReferenceProvider {
        @Override
        // [impl->dsn~open-specification-item-from-coverage-definition~1]
        public PsiReference @NotNull [] getReferencesByElement(
                @NotNull final PsiElement element,
                @NotNull final ProcessingContext context
        ) {
            if (!isSpecificationElement(element)) {
                return PsiReference.EMPTY_ARRAY;
            }
            final TextRange elementRange = element.getTextRange();
            if (elementRange == null || elementRange.isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }
            final CharSequence fileText = element.getContainingFile().getViewProvider().getContents();
            final List<PsiReference> references = new ArrayList<>();
            for (OftSpecificationItemMatch match : OftDeclarationResolver.findCoveredSpecificationItems(fileText)) {
                addReferenceIfCovered(references, element, elementRange, match);
            }
            return references.toArray(PsiReference[]::new);
        }

        private void addReferenceIfCovered(
                final List<PsiReference> references,
                final PsiElement element,
                final TextRange elementRange,
                final OftSpecificationItemMatch match
        ) {
            final OftTextSpan referenceSpan = match.span();
            final int start = Math.max(elementRange.getStartOffset(), referenceSpan.startOffset());
            final int end = Math.min(elementRange.getEndOffset(), referenceSpan.endOffset());
            if (start >= end) {
                return;
            }
            references.add(new OftCoverageTagReference(
                    element,
                    new TextRange(start - elementRange.getStartOffset(), end - elementRange.getStartOffset()),
                    match.item()
            ));
        }

        private boolean isSpecificationElement(final PsiElement element) {
            return element.getContainingFile() != null
                    && element.getContainingFile().getVirtualFile() != null
                    && OftSupportedFiles.isSpecificationFile(element.getContainingFile().getVirtualFile());
        }
    }
}
