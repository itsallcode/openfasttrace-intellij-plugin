package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.codeInsight.lookup.LookupElement;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class OftSpecificationCompletionTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~complete-specification-item-id-in-covers-section~1]
    public void testGivenCoversSectionWhenBasicCompletionInvokesThenItSuggestsDeclaredSpecificationIds() {
        myFixture.addFileToProject("doc/spec.md", """
                req~covers-completion-login.feature~1
                Needs: scn
                """);
        myFixture.addFileToProject("doc/design.md", """
                dsn~covers-completion-design.feature~1
                Needs: impl
                """);
        myFixture.configureByText("impl.md", """
                impl~implementation.feature~1
                Covers:
                - req~covers-completion-lo<caret>
                """);

        final LookupElement[] elements = myFixture.completeBasic();

        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(elements.length, is(1)),
                () -> assertThat(myFixture.getLookupElementStrings(), contains("req~covers-completion-login.feature~1"))
        );
    }

    public void testGivenNamePrefixAndSubstringMatchesWhenBasicCompletionInvokesThenItRanksPrefixBeforeSubstring() {
        myFixture.addFileToProject("doc/spec.md", """
                req~ranking-priority.feature~1
                Needs: scn

                req~catalog-ranking-priority.feature~1
                Needs: scn

                req~unrelated-priority.feature~1
                Needs: scn

                dsn~other-priority.feature~1
                Needs: impl
                """);
        myFixture.configureByText("impl.md", """
                impl~implementation.feature~1
                Covers:
                - ranking-priority<caret>
                """);

        myFixture.completeBasic();

        assertThat(
                myFixture.getLookupElementStrings(),
                contains("req~ranking-priority.feature~1", "req~catalog-ranking-priority.feature~1")
        );
    }

    public void testGivenArtifactTypePrefixWhenBasicCompletionInvokesThenItSuggestsMatchingArtifactTypeIds() {
        myFixture.addFileToProject("doc/spec.md", """
                constr~artifact-type-match.feature~1
                Needs: impl

                req~artifact-type-other.feature~1
                Needs: scn
                """);
        myFixture.configureByText("impl.md", """
                impl~implementation.feature~1
                Covers:
                - constr~artifact-type<caret>
                """);

        myFixture.completeBasic();

        assertThat(lookupStrings().getFirst(), is("constr~artifact-type-match.feature~1"));
    }

    public void testGivenNonCoversSectionWhenBasicCompletionInvokesThenItDoesNotSuggestSpecificationIds() {
        myFixture.addFileToProject("doc/spec.md", """
                req~login.feature~1
                Needs: scn
                """);
        myFixture.configureByText("impl.md", """
                impl~implementation.feature~1
                Needs: req~lo<caret>
                """);

        myFixture.completeBasic();

        assertThat(lookupStrings(), is(empty()));
    }

    private List<String> lookupStrings() {
        final List<String> lookupElementStrings = myFixture.getLookupElementStrings();
        return lookupElementStrings == null ? List.of() : lookupElementStrings;
    }
}
