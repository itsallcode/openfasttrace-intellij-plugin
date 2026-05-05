package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;

import java.util.Optional;

// [impl->dsn~specification-item-completion~1]
// [impl->dsn~complete-specification-item-id-in-covers-section~1]
// [impl->dsn~complete-specification-item-id-in-active-live-template-covers-field~1]
// [impl->dsn~complete-specification-item-id-in-coverage-tag-target~1]
// [impl->dsn~complete-specification-item-id-in-spaced-coverage-tag-target~1]
// [impl->dsn~complete-specification-item-id-in-incomplete-coverage-tag-target~1]
// [impl->dsn~suppress-coverage-tag-target-completion-outside-target-context~1]
public final class OftSpecificationCompletionProvider extends CompletionContributor implements DumbAware {
    public OftSpecificationCompletionProvider() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new SpecificationItemCompletionProvider());
    }

    private static final class SpecificationItemCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(
                final CompletionParameters parameters,
                final ProcessingContext context,
                final CompletionResultSet result
        ) {
            final CharSequence fileText = parameters.getEditor().getDocument().getCharsSequence();
            final int offset = parameters.getEditor().getCaretModel().getOffset();
            final Optional<String> prefix = completionPrefix(parameters, fileText, offset);
            if (prefix.isEmpty()) {
                return;
            }
            final String completionPrefix = prefix.get();
            final Project project = parameters.getPosition().getProject();
            final CompletionResultSet unrestrictedResult = result.withPrefixMatcher(new PlainPrefixMatcher(""));
            for (OftIndexedSpecification specification :
                    OftSpecificationCompletionSupport.findMatchingSpecifications(project, completionPrefix)) {
                final OftSpecificationCompletionSupport.MatchKind matchKind =
                        OftSpecificationCompletionSupport.matchKind(specification, completionPrefix);
                final LookupElementBuilder lookupElement = LookupElementBuilder.create(specification.id())
                        .withTypeText(specification.artifactType(), true)
                        .withInsertHandler((insertionContext, item) ->
                                replacePrefix(insertionContext, completionPrefix, specification.id()));
                unrestrictedResult.addElement(PrioritizedLookupElement.withPriority(
                        lookupElement,
                        OftSpecificationCompletionSupport.priorityOf(matchKind)
                ));
            }
            result.stopHere();
        }

        private static Optional<String> completionPrefix(
                final CompletionParameters parameters,
                final CharSequence fileText,
                final int offset
        ) {
            final PsiFile originalFile = parameters.getOriginalFile();
            if (OftSupportedFiles.isSpecificationFileName(originalFile.getName())
                    && OftDeclarationResolver.isInsideCoversSection(fileText, offset)) {
                return Optional.of(OftSpecificationCompletionSupport.specificationPrefixAt(fileText, offset));
            }
            if (OftSupportedFiles.isCoverageTagFileName(originalFile.getName())) {
                return OftCoverageTagCompletionContext.findAt(fileText, offset)
                        .filter(completionContext ->
                                isInsideCommentOrFallbackText(parameters, fileText, completionContext))
                        .map(OftCoverageTagCompletionContext::prefix);
            }
            return Optional.empty();
        }

        private static boolean isInsideCommentOrFallbackText(
                final CompletionParameters parameters,
                final CharSequence fileText,
                final OftCoverageTagCompletionContext completionContext
        ) {
            if (isInsidePsiComment(parameters.getPosition())) {
                return true;
            }
            return hasTextCommentMarkerBeforeTag(fileText, completionContext.bracketStart());
        }

        private static boolean isInsidePsiComment(final PsiElement position) {
            PsiElement current = position;
            while (current != null && !(current instanceof PsiFile)) {
                if (current instanceof PsiComment) {
                    return true;
                }
                current = current.getParent();
            }
            return false;
        }

        private static boolean hasTextCommentMarkerBeforeTag(final CharSequence text, final int bracketStart) {
            final int lineStart = findLineStart(text, bracketStart);
            final String beforeTag = text.subSequence(lineStart, bracketStart).toString().trim();
            return !isInsideQuotedText(text, lineStart, bracketStart)
                    && (beforeTag.endsWith("//")
                    || beforeTag.endsWith("#")
                    || beforeTag.endsWith("--")
                    || beforeTag.endsWith(";")
                    || beforeTag.endsWith("'")
                    || beforeTag.endsWith("/*")
                    || beforeTag.endsWith("<!--")
                    || beforeTag.endsWith("*"));
        }

        private static int findLineStart(final CharSequence text, final int offset) {
            int lineStart = offset;
            while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }
            return lineStart;
        }

        private static boolean isInsideQuotedText(
                final CharSequence text,
                final int startOffset,
                final int endOffset
        ) {
            boolean insideSingleQuotes = false;
            boolean insideDoubleQuotes = false;
            boolean escaped = false;
            for (int index = startOffset; index < endOffset; index++) {
                final char character = text.charAt(index);
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '\'' && !insideDoubleQuotes) {
                    insideSingleQuotes = !insideSingleQuotes;
                } else if (character == '"' && !insideSingleQuotes) {
                    insideDoubleQuotes = !insideDoubleQuotes;
                }
            }
            return insideSingleQuotes || insideDoubleQuotes;
        }

        private static void replacePrefix(
                final InsertionContext context,
                final String prefix,
                final String specificationId
        ) {
            final int startOffset = Math.max(0, context.getStartOffset() - prefix.length());
            final int endOffset = context.getTailOffset();
            context.getDocument().replaceString(startOffset, endOffset, specificationId);
            context.getEditor().getCaretModel().moveToOffset(startOffset + specificationId.length());
        }
    }
}
