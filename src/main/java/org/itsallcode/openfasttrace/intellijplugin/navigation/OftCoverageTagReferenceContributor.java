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
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftCoverageTagMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class OftCoverageTagReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), new CoverageTagReferenceProvider());
    }


    private static final class CoverageTagReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(
                @NotNull final PsiElement element,
                @NotNull final ProcessingContext context
        ) {
            if (!isCoverageTagElement(element)) {
                return PsiReference.EMPTY_ARRAY;
            }
            final TextRange elementRange = element.getTextRange();
            if (elementRange == null || elementRange.isEmpty()) {
                return PsiReference.EMPTY_ARRAY;
            }
            final CharSequence fileText = element.getContainingFile().getViewProvider().getContents();
            final List<PsiReference> references = new ArrayList<>();
            for (OftCoverageTagMatch match : OftSyntaxCore.findCoverageTags(fileText)) {
                addReferenceIfCovered(
                        references,
                        element,
                        elementRange,
                        match.sourceSpan(),
                        match.tag().effectiveSource()
                );
                addReferenceIfCovered(
                        references,
                        element,
                        elementRange,
                        match.targetSpan(),
                        match.tag().target()
                );
            }
            return references.toArray(PsiReference[]::new);
        }

        private void addReferenceIfCovered(
                final List<PsiReference> references,
                final PsiElement element,
                final TextRange elementRange,
                final OftTextSpan referenceSpan,
                final OftSpecificationItem target
        ) {
            final int start = Math.max(elementRange.getStartOffset(), referenceSpan.startOffset());
            final int end = Math.min(elementRange.getEndOffset(), referenceSpan.endOffset());
            if (start >= end) {
                return;
            }
            references.add(new OftCoverageTagReference(
                    element,
                    new TextRange(start - elementRange.getStartOffset(), end - elementRange.getStartOffset()),
                    target
            ));
        }
        private static boolean isCoverageTagElement(final PsiElement element) {
            return element.getContainingFile() != null
                    && element.getContainingFile().getVirtualFile() != null
                    && OftSupportedFiles.isCoverageTagFile(element.getContainingFile().getVirtualFile());
        }
    }
}
