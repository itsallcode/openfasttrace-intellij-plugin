package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.jetbrains.annotations.NotNull;

// [impl->dsn~specification-item-completion~1]
// [impl->dsn~complete-specification-item-id-in-covers-section~1]
public final class OftSpecificationCompletionProvider extends CompletionContributor implements DumbAware {
    public OftSpecificationCompletionProvider() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CoversCompletionProvider());
    }

    private static final class CoversCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(
                @NotNull final CompletionParameters parameters,
                @NotNull final ProcessingContext context,
                @NotNull final CompletionResultSet result
        ) {
            final PsiElement position = parameters.getPosition();
            final PsiFile originalFile = parameters.getOriginalFile();
            if (!OftSupportedFiles.isSpecificationFile(originalFile.getVirtualFile())) {
                return;
            }
            final CharSequence fileText = originalFile.getViewProvider().getContents();
            final int offset = parameters.getOffset();
            if (!OftDeclarationResolver.isInsideCoversSection(fileText, offset)) {
                return;
            }
            final String prefix = specificationPrefixAt(fileText, offset);
            final Project project = position.getProject();
            final CompletionResultSet unrestrictedResult = result.withPrefixMatcher(new PlainPrefixMatcher(""));
            for (OftIndexedSpecification specification :
                    OftSpecificationCompletionSupport.findMatchingSpecifications(project, prefix)) {
                final OftSpecificationCompletionSupport.MatchKind matchKind =
                        OftSpecificationCompletionSupport.matchKind(specification, prefix);
                unrestrictedResult.addElement(PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(specification.id()).withTypeText(specification.artifactType(), true),
                        priorityOf(matchKind)
                ));
            }
            result.stopHere();
        }

        private static double priorityOf(final OftSpecificationCompletionSupport.MatchKind matchKind) {
            return switch (matchKind) {
                case FULL_ID_PREFIX -> 400;
                case NAME_PREFIX -> 300;
                case NAME_SUBSTRING -> 200;
                case ARTIFACT_TYPE_PREFIX -> 100;
                case NONE -> 0;
            };
        }

        private static String specificationPrefixAt(final CharSequence text, final int offset) {
            final int boundedOffset = Math.clamp(offset, 0, text.length());
            int start = boundedOffset;
            while (start > 0 && isSpecificationCharacter(text.charAt(start - 1))) {
                start--;
            }
            return text.subSequence(start, boundedOffset).toString();
        }

        private static boolean isSpecificationCharacter(final char character) {
            return Character.isLetterOrDigit(character)
                    || character == '~'
                    || character == '.'
                    || character == '_'
                    || character == '-';
        }
    }
}
