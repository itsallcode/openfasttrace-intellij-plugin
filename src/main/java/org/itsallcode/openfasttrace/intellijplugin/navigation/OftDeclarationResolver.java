package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftCoverageTagMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class OftDeclarationResolver {
    private OftDeclarationResolver() {
    }

    static Optional<OftSpecificationItem> findReferenceAt(CharSequence text, int offset) {
        return findCoverageTagReferenceAt(text, offset)
                .or(() -> OftSyntaxCore.findSpecificationItems(text).stream()
                        .filter(match -> contains(match.span(), offset))
                        .map(match -> match.item())
                        .findFirst());
    }

    private static Optional<OftSpecificationItem> findCoverageTagReferenceAt(CharSequence text, int offset) {
        return OftSyntaxCore.findCoverageTags(text).stream()
                .filter(match -> contains(match.span(), offset))
                .findFirst()
                .flatMap(match -> referenceFromCoverageTag(match, offset));
    }

    private static Optional<OftSpecificationItem> referenceFromCoverageTag(OftCoverageTagMatch match, int offset) {
        if (contains(match.sourceSpan(), offset)) {
            return Optional.of(match.tag().effectiveSource());
        }
        if (contains(match.targetSpan(), offset)) {
            return Optional.of(match.tag().target());
        }
        return Optional.empty();
    }

    private static boolean contains(OftTextSpan span, int offset) {
        return span.startOffset() <= offset && offset < span.endOffset();
    }

    static PsiElement[] resolveDeclarations(Project project, OftSpecificationItem reference) {
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        final PsiManager psiManager = PsiManager.getInstance(project);
        final List<PsiElement> targets = new ArrayList<>();
        final Set<String> seenTargets = new LinkedHashSet<>();
        FileBasedIndex.getInstance().processValues(
                OftSpecificationIndex.NAME,
                reference.id(),
                null,
                (file, values) -> {
                    for (OftIndexedSpecification value : values) {
                        final PsiElement target = findPsiElementAt(psiManager, file, value.offset());
                        if (target != null && seenTargets.add(file.getPath() + ":" + value.offset())) {
                            targets.add(target);
                        }
                    }
                    return true;
                },
                scope
        );
        return targets.toArray(PsiElement[]::new);
    }

    private static PsiElement findPsiElementAt(PsiManager psiManager, VirtualFile file, int offset) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null || psiFile.getTextLength() == 0) {
            return null;
        }
        final int clampedOffset = Math.min(offset, psiFile.getTextLength() - 1);
        final PsiElement element = psiFile.findElementAt(clampedOffset);
        if (element != null) {
            return element;
        }
        return clampedOffset > 0 ? psiFile.findElementAt(clampedOffset - 1) : null;
    }
}
