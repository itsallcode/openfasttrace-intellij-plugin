package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class OftCoverageTagCompletionTest extends AbstractOftPlatformTestCase {
    private static final String ARROW = "-" + ">";

    // [itest->dsn~complete-specification-item-id-in-coverage-tag-target~1]
    public void testGivenCoverageTagTargetInJavaCommentWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s
                }
                """.formatted(tagTarget("dsn~coverage-target<caret>")));

        final LookupElement[] elements = myFixture.completeBasic();

        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(elements.length, is(1)),
                () -> assertThat(lookupStrings(), contains("dsn~coverage-target~1"))
        );
    }

    // [itest->dsn~complete-specification-item-id-in-spaced-coverage-tag-target~1]
    public void testGivenSpacedArrowInCoverageTagTargetWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s
                }
                """.formatted(spacedTagTarget("dsn~coverage-target<caret>")));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    // [itest->dsn~complete-specification-item-id-in-incomplete-coverage-tag-target~1]
    public void testGivenIncompleteCoverageTagTargetWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s
                }
                """.formatted(incompleteTagTarget("dsn~coverage-target<caret>")));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenEmptyCoverageTagTargetWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s
                }
                """.formatted(tagTarget("<caret>")));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenCoverageTagTargetInPlainTextCommentWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("settings.cfg", "# " + tagTarget("dsn~coverage-target<caret>"));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenCoverageTagTargetInShellCommentWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("demo.sh", "# " + tagTarget("dsn~coverage-target<caret>"));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenCoverageTagTargetInJsonLineCommentWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("settings.json", "// " + tagTarget("dsn~coverage-target<caret>"));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenCoverageTagTargetInXmlCommentWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("project.xml", "<!-- " + tagTarget("dsn~coverage-target<caret>") + " -->");

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenCoverageTagTargetInNewTagImporterExtensionWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("suite.robot", "# " + tagTarget("dsn~coverage-target<caret>"));

        myFixture.completeBasic();

        assertThat(lookupStrings(), hasItem("dsn~coverage-target~1"));
    }

    public void testGivenCoverageTagTargetPrefixWhenCompletingThenItReplacesOnlyTheTargetPrefix() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s
                }
                """.formatted(tagTarget("dsn~coverage-target<caret>")));

        final LookupElement[] elements = myFixture.completeBasic();
        myFixture.getLookup().setCurrentItem(elements[0]);
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR);

        assertThat(myFixture.getEditor().getDocument().getText(), containsString("// " + tagTarget("dsn~coverage-target~1")));
    }

    // [itest->dsn~suppress-coverage-tag-target-completion-outside-target-context~1]
    public void testGivenCaretBeforeCoverageTagArrowWhenBasicCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s
                }
                """.formatted("[impl<caret>" + ARROW + "dsn~coverage-target]"));

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    public void testGivenCaretAfterClosedCoverageTagWhenBasicCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    // %s <caret>
                }
                """.formatted(tagTarget("dsn~coverage-target~1")));

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    public void testGivenCoverageTagTextInJavaStringWhenBasicCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    String tag = "%s";
                }
                """.formatted(tagTarget("dsn~coverage-target<caret>")));

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    public void testGivenCoverageTagCommentMarkerInJavaStringWhenBasicCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        addTargetDeclaration();
        configureJavaSource("""
                class Main {
                    String tag = "// %s";
                }
                """.formatted(tagTarget("dsn~coverage-target<caret>")));

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    public void testGivenCoverageTagTextWithoutPlainTextCommentMarkerWhenBasicCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("settings.cfg", "tag = \"" + tagTarget("dsn~coverage-target<caret>") + "\"");

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    public void testGivenUnsupportedFileWhenCoverageTagCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        addTargetDeclaration();
        myFixture.configureByText("notes.txt", "// " + tagTarget("dsn~coverage-target<caret>"));

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    private void addTargetDeclaration() {
        myFixture.addFileToProject("doc/spec.md", """
                dsn~coverage-target~1
                Needs: impl
                """);
    }

    private void configureJavaSource(final String text) {
        myFixture.configureByText("Main.java", text);
    }

    private static String tagTarget(final String target) {
        return "[impl" + ARROW + target + "]";
    }

    private static String spacedTagTarget(final String target) {
        return "[impl " + ARROW + " " + target + "]";
    }

    private static String incompleteTagTarget(final String target) {
        return "[impl" + ARROW + target;
    }

    private List<String> lookupStrings() {
        final List<String> lookupElementStrings = myFixture.getLookupElementStrings();
        return lookupElementStrings == null ? List.of() : lookupElementStrings;
    }
}
