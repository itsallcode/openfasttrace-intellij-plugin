package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.testFramework.EdtTestUtil;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// [itest->dsn~specification-item-navigation-runtime~1]
public class OftNavigationTest extends AbstractOftPlatformTestCase {
    public void testGoToSymbolReturnsProjectLocalSpecificationItems() throws Exception {
        final String uniqueName = "openfasttrace_navigation_test_item";
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_test_item~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/design.rst", """
                dsn~openfasttrace_navigation_test_item~2
                Covers:
                - req~openfasttrace_navigation_test_item~1
                """);

        final OftChooseByNameContributor contributor = new OftChooseByNameContributor();

        assertThat(Arrays.asList(contributor.getNames(getProject(), false)), hasItem(uniqueName));

        final NavigationItem[] items = contributor.getItemsByName(uniqueName, uniqueName, getProject(), false);
        assertThat(items.length, is(2));

        final OftNavigationItem item = Arrays.stream(items)
                .map(OftNavigationItem.class::cast)
                .filter(candidate -> candidate.getSpecification().id().equals("req~" + uniqueName + "~1"))
                .findFirst()
                .orElse(null);
        assertThat(item, is(notNullValue()));

        EdtTestUtil.runInEdtAndWait(() -> item.navigate(true));

        final Editor selectedEditor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        assertThat(selectedEditor, is(notNullValue()));
        assertThat(FileDocumentManager.getInstance().getFile(selectedEditor.getDocument()).getName(), is("spec.md"));
        assertThat(selectedEditor.getCaretModel().getOffset(), is(0));
    }
}
