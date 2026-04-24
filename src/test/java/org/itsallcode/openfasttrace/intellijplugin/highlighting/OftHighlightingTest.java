package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OftHighlightingTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~highlight-markdown-specification-item~1]
    public void testGivenMarkdownWithValidSpecificationItemWhenHighlightingRunsThenItemAndKeywordAreHighlighted() {
        myFixture.configureByText("spec.md", """
                # Login

                req~login.feature~1
                Needs: dsn, uman
                """);

        final List<HighlightInfo> infos = myFixture.doHighlighting();
        final boolean highlighted = hasHighlight(infos, "req~login.feature~1", OftHighlighterKeys.SPECIFICATION_ITEM)
                && hasHighlight(infos, "Needs", OftHighlighterKeys.KEYWORD);

        assertThat(highlighted, is(true));
    }

    // [itest->dsn~ignore-invalid-markdown-specification-item~1]
    public void testGivenMarkdownWithInvalidSpecificationItemWhenHighlightingRunsThenInvalidTextIsNotHighlighted() {
        myFixture.configureByText("spec.md", "feat~broken~I");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "feat~broken~I", OftHighlighterKeys.SPECIFICATION_ITEM), is(false));
    }

    // [itest->dsn~tolerate-incomplete-markdown-specification-item~1]
    public void testGivenMarkdownWithIncompleteSpecificationItemWhenHighlightingRunsThenIncompleteTextIsNotHighlighted() {
        myFixture.configureByText("spec.md", "feat~unfinished~");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "feat~unfinished~", OftHighlighterKeys.SPECIFICATION_ITEM), is(false));
    }

    // [itest->dsn~highlight-rst-specification-item~1]
    public void testGivenRstWithValidSpecificationItemWhenHighlightingRunsThenItemAndKeywordAreHighlighted() {
        myFixture.configureByText("spec.rst", """
                Requirement
                ===========

                dsn~rst.requirement~2
                Comment: Valid in rst as well.
                """);

        final List<HighlightInfo> infos = myFixture.doHighlighting();
        final boolean highlighted = hasHighlight(infos, "dsn~rst.requirement~2", OftHighlighterKeys.SPECIFICATION_ITEM)
                && hasHighlight(infos, "Comment", OftHighlighterKeys.KEYWORD);

        assertThat(highlighted, is(true));
    }

    // [itest->dsn~ignore-invalid-rst-specification-item~1]
    public void testGivenRstWithInvalidSpecificationItemWhenHighlightingRunsThenInvalidTextIsNotHighlighted() {
        myFixture.configureByText("spec.rst", "dsn~broken~I");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "dsn~broken~I", OftHighlighterKeys.SPECIFICATION_ITEM), is(false));
    }

    // [itest->dsn~tolerate-incomplete-rst-specification-item~1]
    public void testGivenRstWithIncompleteSpecificationItemWhenHighlightingRunsThenIncompleteTextIsNotHighlighted() {
        myFixture.configureByText("spec.rst", "dsn~unfinished~");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "dsn~unfinished~", OftHighlighterKeys.SPECIFICATION_ITEM), is(false));
    }

    // [itest->dsn~highlight-coverage-tag~1]
    public void testGivenSourceWithValidCoverageTagWhenHighlightingRunsThenCoverageTagIsHighlighted() {
        final String coverageTag = "[impl->dsn~coverage-tag-support~1]";
        myFixture.configureByText("Demo.java", "// " + coverageTag + "\n");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, coverageTag, OftHighlighterKeys.COVERAGE_TAG), is(true));
    }

    // [itest->dsn~ignore-invalid-coverage-tag~1]
    public void testGivenSourceWithInvalidCoverageTagWhenHighlightingRunsThenInvalidTextIsNotHighlighted() {
        myFixture.configureByText("Demo.java", "// [impl->feat~broken~I]\n");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "[impl->feat~broken~I]", OftHighlighterKeys.COVERAGE_TAG), is(false));
    }

    // [itest->dsn~tolerate-incomplete-coverage-tag~1]
    public void testGivenSourceWithIncompleteCoverageTagWhenHighlightingRunsThenIncompleteTextIsNotHighlighted() {
        myFixture.configureByText("Demo.java", "// [impl->feat~unfinished~\n");

        final List<HighlightInfo> infos = myFixture.doHighlighting();

        assertThat(hasHighlight(infos, "[impl->feat~unfinished~", OftHighlighterKeys.COVERAGE_TAG), is(false));
    }
}
