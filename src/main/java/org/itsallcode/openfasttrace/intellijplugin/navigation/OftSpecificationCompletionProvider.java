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
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;

// [impl->dsn~specification-item-completion~1]
// [impl->dsn~complete-specification-item-id-in-covers-section~1]
// [impl->dsn~complete-specification-item-id-in-active-live-template-covers-field~1]
public final class OftSpecificationCompletionProvider extends CompletionContributor implements DumbAware {
    public OftSpecificationCompletionProvider() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CoversCompletionProvider());
    }

    private static final class CoversCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(
                final CompletionParameters parameters,
                final ProcessingContext context,
                final CompletionResultSet result
        ) {
            final PsiFile originalFile = parameters.getOriginalFile();
            if (!OftSupportedFiles.isSpecificationFileName(originalFile.getName())) {
                return;
            }
            final CharSequence fileText = parameters.getEditor().getDocument().getCharsSequence();
            final int offset = parameters.getEditor().getCaretModel().getOffset();
            if (!OftDeclarationResolver.isInsideCoversSection(fileText, offset)) {
                return;
            }
            final String prefix = OftSpecificationCompletionSupport.specificationPrefixAt(fileText, offset);
            final Project project = parameters.getPosition().getProject();
            final CompletionResultSet unrestrictedResult = result.withPrefixMatcher(new PlainPrefixMatcher(""));
            for (OftIndexedSpecification specification :
                    OftSpecificationCompletionSupport.findMatchingSpecifications(project, prefix)) {
                final OftSpecificationCompletionSupport.MatchKind matchKind =
                        OftSpecificationCompletionSupport.matchKind(specification, prefix);
                final LookupElementBuilder lookupElement = LookupElementBuilder.create(specification.id())
                        .withTypeText(specification.artifactType(), true);
                unrestrictedResult.addElement(PrioritizedLookupElement.withPriority(
                        lookupElement,
                        OftSpecificationCompletionSupport.priorityOf(matchKind)
                ));
            }
            result.stopHere();
        }
    }
}
