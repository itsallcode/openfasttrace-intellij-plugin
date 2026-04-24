package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftCoverageTagMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftKeywordMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
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

    static Optional<OftSpecificationItem> findReferenceAt(final CharSequence text, final int offset) {
        return findCoverageTagReferenceAt(text, offset)
                .or(() -> OftSyntaxCore.findSpecificationItems(text).stream()
                        .filter(match -> contains(match.span(), offset))
                        .map(OftSpecificationItemMatch::item)
                        .findFirst());
    }

    static Optional<OftSpecificationItem> findDeclaredItem(final PsiElement element) {
        if (element == null || element.getContainingFile() == null || element.getContainingFile().getVirtualFile() == null) {
            return Optional.empty();
        }
        if (!OftSupportedFiles.isSpecificationFile(element.getContainingFile().getVirtualFile())) {
            return Optional.empty();
        }
        final int offset = element.getTextRange().getStartOffset();
        return OftSyntaxCore.findDefinitionSpecificationItems(element.getContainingFile().getViewProvider().getContents()).stream()
                .filter(match -> contains(match.span(), offset))
                .map(OftSpecificationItemMatch::item)
                .findFirst();
    }

    private static Optional<OftSpecificationItem> findCoverageTagReferenceAt(final CharSequence text, final int offset) {
        return OftSyntaxCore.findCoverageTags(text).stream()
                .filter(match -> contains(match.span(), offset))
                .findFirst()
                .flatMap(match -> referenceFromCoverageTag(match, offset));
    }

    private static Optional<OftSpecificationItem> referenceFromCoverageTag(final OftCoverageTagMatch match, final int offset) {
        if (contains(match.sourceSpan(), offset)) {
            return Optional.of(match.tag().effectiveSource());
        }
        if (contains(match.targetSpan(), offset)) {
            return Optional.of(match.tag().target());
        }
        return Optional.empty();
    }

    private static boolean contains(final OftTextSpan span, final int offset) {
        return span.startOffset() <= offset && offset < span.endOffset();
    }

    static PsiElement[] resolveDeclarations(final Project project, final OftSpecificationItem reference) {
        return resolveDeclarationElements(project, reference).toArray(PsiElement[]::new);
    }

    static ResolveResult[] resolveDeclarationResults(final Project project, final OftSpecificationItem reference) {
        return resolveDeclarationElements(project, reference).stream()
                .map(PsiElementResolveResult::new)
                .toArray(ResolveResult[]::new);
    }

    private static List<PsiElement> resolveDeclarationElements(final Project project, final OftSpecificationItem reference) {
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        final PsiManager psiManager = PsiManager.getInstance(project);
        final List<PsiElement> targets = new ArrayList<>();
        final Set<String> seenTargets = new LinkedHashSet<>();
        FileBasedIndex.getInstance().processValues(
                OftSpecificationIndex.SPECIFICATION_ID,
                reference.id(),
                null,
                (file, values) -> {
                    for (OftIndexedSpecification value : values) {
                        if (!value.matches(reference)) {
                            continue;
                        }
                        final PsiElement target = findPsiElementAt(psiManager, file, value.offset());
                        if (target != null && seenTargets.add(file.getPath() + ":" + value.offset())) {
                            targets.add(target);
                        }
                    }
                    return true;
                },
                scope
        );
        return targets;
    }

    static boolean processCoverageOccurrences(
            final Project project,
            final OftSpecificationItem declaration,
            final SearchScope scope,
            final Processor<? super PsiElement> processor
    ) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final Set<String> seenTargets = new LinkedHashSet<>();
        return ProjectFileIndex.getInstance(project).iterateContent(file -> {
            if (!isInScope(scope, file)) {
                return true;
            }
            if (OftSupportedFiles.isSpecificationFile(file)) {
                return processSpecificationCoverageOccurrences(psiManager, file, declaration, processor, seenTargets);
            }
            if (OftSupportedFiles.isCoverageTagFile(file)) {
                return processCoverageTagOccurrences(psiManager, file, declaration, processor, seenTargets);
            }
            return true;
        });
    }

    private static boolean processSpecificationCoverageOccurrences(
            final PsiManager psiManager,
            final VirtualFile file,
            final OftSpecificationItem declaration,
            final Processor<? super PsiElement> processor,
            final Set<String> seenTargets
    ) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            return true;
        }
        for (OftTextSpan span : findCoveredSpecificationItemSpans(psiFile.getViewProvider().getContents(), declaration)) {
            final PsiElement target = findPsiElementAt(psiManager, file, span.startOffset());
            if (target != null && seenTargets.add(file.getPath() + ":" + span.startOffset()) && !processor.process(target)) {
                return false;
            }
        }
        return true;
    }

    private static boolean processCoverageTagOccurrences(
            final PsiManager psiManager,
            final VirtualFile file,
            final OftSpecificationItem declaration,
            final Processor<? super PsiElement> processor,
            final Set<String> seenTargets
    ) {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            return true;
        }
        for (OftCoverageTagMatch match : OftSyntaxCore.findCoverageTags(psiFile.getViewProvider().getContents())) {
            if (declaration.id().equals(match.tag().effectiveSource().id())) {
                final PsiElement target = findPsiElementAt(psiManager, file, match.sourceSpan().startOffset());
                if (target != null && seenTargets.add(file.getPath() + ":" + match.sourceSpan().startOffset()) && !processor.process(target)) {
                    return false;
                }
            }
            if (declaration.id().equals(match.tag().target().id())) {
                final PsiElement target = findPsiElementAt(psiManager, file, match.targetSpan().startOffset());
                if (target != null && seenTargets.add(file.getPath() + ":" + match.targetSpan().startOffset()) && !processor.process(target)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<OftTextSpan> findCoveredSpecificationItemSpans(
            final CharSequence text,
            final OftSpecificationItem declaration
    ) {
        return findCoveredSpecificationItems(text).stream()
                .filter(match -> declaration.id().equals(match.item().id()))
                .map(OftSpecificationItemMatch::span)
                .toList();
    }

    static List<OftSpecificationItemMatch> findCoveredSpecificationItems(final CharSequence text) {
        final List<OftSpecificationItemMatch> matches = new ArrayList<>();
        int lineStart = 0;
        boolean insideCoversSection = false;
        while (lineStart <= text.length()) {
            final int lineEnd = findLineEnd(text, lineStart);
            final CharSequence line = text.subSequence(lineStart, lineEnd);
            insideCoversSection = updateSectionState(line, insideCoversSection);
            if (insideCoversSection) {
                for (OftSpecificationItemMatch match : OftSyntaxCore.findSpecificationItems(line)) {
                    matches.add(new OftSpecificationItemMatch(
                            match.item(),
                            new OftTextSpan(lineStart + match.span().startOffset(), lineStart + match.span().endOffset())
                    ));
                }
            }
            lineStart = lineEnd + 1;
        }
        return List.copyOf(matches);
    }

    private static int findLineEnd(final CharSequence text, final int lineStart) {
        int lineEnd = lineStart;
        while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
            lineEnd++;
        }
        return lineEnd;
    }

    private static boolean updateSectionState(final CharSequence line, final boolean insideCoversSection) {
        if (line.toString().trim().isEmpty()) {
            return false;
        }
        if (!OftSyntaxCore.findDefinitionSpecificationItems(line).isEmpty()) {
            return false;
        }
        final List<OftKeywordMatch> keywords = OftSyntaxCore.findKeywords(line);
        if (!keywords.isEmpty()) {
            return "Covers".equals(keywords.get(0).keyword());
        }
        return insideCoversSection;
    }

    private static boolean isInScope(final SearchScope scope, final VirtualFile file) {
        return !(scope instanceof GlobalSearchScope globalSearchScope) || globalSearchScope.contains(file);
    }

    static PsiElement findPsiElementAt(final PsiManager psiManager, final VirtualFile file, final int offset) {
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
