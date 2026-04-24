package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// [impl->dsn~specification-item-navigation~1]
public final class OftChooseByNameContributor implements ChooseByNameContributor, DumbAware {
    @Override
    // [impl->dsn~show-specification-item-in-go-to-symbol~1]
    public String @NonNull [] getNames(final Project project, final boolean includeNonProjectItems) {
        final List<String> names = new ArrayList<>();
        FileBasedIndex.getInstance().processAllKeys(OftSpecificationIndex.SPECIFICATION_ID, key -> {
            names.add(key);
            return true;
        }, project);
        names.sort(String::compareTo);
        return names.toArray(String[]::new);
    }

    @Override
    // [impl->dsn~open-specification-item-from-go-to-symbol~1]
    // [impl->dsn~open-specification-item-from-search-everywhere~1]
    public NavigationItem @NonNull [] getItemsByName(
            final String name,
            final String pattern,
            final Project project,
            final boolean includeNonProjectItems
    ) {
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        final List<OftNavigationItem> items = new ArrayList<>();
        FileBasedIndex.getInstance().processValues(
                OftSpecificationIndex.SPECIFICATION_ID,
                name,
                null,
                (file, values) -> {
                    for (OftIndexedSpecification value : values) {
                        items.add(new OftNavigationItem(project, file, value));
                    }
                    return true;
                },
                scope
        );
        items.sort(Comparator
                .comparing((OftNavigationItem item) -> item.getSpecification().id())
                .thenComparing(item -> item.getFile().getPath()));
        return items.toArray(NavigationItem[]::new);
    }
}
