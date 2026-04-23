package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// [itest->dsn~markdown-highlighting-runtime~1]
// [itest->dsn~rst-highlighting-runtime~1]
// [itest->dsn~coverage-tag-highlighting-runtime~1]
public class OftHighlightingTest extends AbstractOftPlatformTestCase {
    public void testMarkdownHighlightingRecognizesValidItemsAndKeywords() {
        myFixture.configureByText("spec.md", """
                # Login

                req~login.feature~1
                This item is valid.
                Needs: dsn, uman

                feat~broken~I
                feat~unfinished~
                """);

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "req~login.feature~1", OftHighlighterKeys.SPECIFICATION_ITEM), is(true));
        assertThat(hasHighlight(infos, "Needs", OftHighlighterKeys.KEYWORD), is(true));
        assertThat(hasHighlight(infos, "feat~broken~I", OftHighlighterKeys.SPECIFICATION_ITEM), is(false));
    }

    public void testRstHighlightingRecognizesValidItems() {
        myFixture.configureByText("spec.rst", """
                Requirement
                ===========

                dsn~rst.requirement~2
                Comment: Valid in rst as well.

                dsn~broken~I
                dsn~unfinished~
                """);

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "dsn~rst.requirement~2", OftHighlighterKeys.SPECIFICATION_ITEM), is(true));
        assertThat(hasHighlight(infos, "Comment", OftHighlighterKeys.KEYWORD), is(true));
        assertThat(hasHighlight(infos, "dsn~broken~I", OftHighlighterKeys.SPECIFICATION_ITEM), is(false));
    }

    public void testCoverageTagHighlightingRecognizesValidTags() {
        final String validCoverageTag = "[impl->" + "dsn~coverage-tag-support~1]";
        myFixture.configureByText(
                "Demo.java",
                "class Demo {\n"
                        + "    // " + validCoverageTag + "\n"
                        + "    // [impl->feat~broken~I]\n"
                        + "    // [impl->feat~unfinished~\n"
                        + "}\n"
        );

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, validCoverageTag, OftHighlighterKeys.COVERAGE_TAG), is(true));
        assertThat(hasHighlight(infos, "[impl->feat~broken~I]", OftHighlighterKeys.COVERAGE_TAG), is(false));
    }
}
