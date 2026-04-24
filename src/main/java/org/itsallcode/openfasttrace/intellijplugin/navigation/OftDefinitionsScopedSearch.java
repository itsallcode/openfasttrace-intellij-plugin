package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jspecify.annotations.NonNull;

// [impl->dsn~specification-item-navigation~1]
public final class OftDefinitionsScopedSearch
        implements QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters>, DumbAware {
    @Override
    // [impl->dsn~show-covering-occurrences-from-declaration~1]
    public boolean execute(
            final DefinitionsScopedSearch.@NonNull SearchParameters queryParameters,
            final @NonNull Processor<? super PsiElement> consumer
    ) {
        return ReadAction.computeBlocking(() -> executeSearch(queryParameters, consumer));
    }

    private static boolean executeSearch(
            final DefinitionsScopedSearch.@NonNull SearchParameters queryParameters,
            final @NonNull Processor<? super PsiElement> consumer
    ) {
        if (!queryParameters.isQueryValid()) {
            return true;
        }
        return OftDeclarationResolver.findDeclaredItem(queryParameters.getElement())
                .map(declaration -> OftDeclarationResolver.processCoverageOccurrences(
                        queryParameters.getProject(),
                        declaration,
                        queryParameters.getScope(),
                        consumer
                ))
                .orElse(true);
    }
}
