package org.itsallcode.openfasttrace.intellijplugin.syntax;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class OftSyntaxCoreTest {
    @Test
    void classifiesSpecificationItems() {
        assertThat(OftSyntaxCore.classifySpecificationItem("req~hello.world~1"), is(OftFragmentStatus.VALID));
        assertThat(OftSyntaxCore.classifySpecificationItem("feat~broken~I"), is(OftFragmentStatus.INVALID));
        assertThat(OftSyntaxCore.classifySpecificationItem("feat~broken~"), is(OftFragmentStatus.INCOMPLETE));
    }

    @Test
    void classifiesCoverageTags() {
        final String validCoverageTag = "[impl->" + "dsn~coverage-tag-support~1]";
        assertThat(
                OftSyntaxCore.classifyCoverageTag(validCoverageTag),
                is(OftFragmentStatus.VALID)
        );
        assertThat(
                OftSyntaxCore.classifyCoverageTag("[impl->feat~foobar~I]"),
                is(OftFragmentStatus.INVALID)
        );
        assertThat(
                OftSyntaxCore.classifyCoverageTag("[impl->feat~foobar~"),
                is(OftFragmentStatus.INCOMPLETE)
        );
    }

    @Test
    void extractsSpecificationItemsAndKeywords() {
        final String text = """
                req~hello.world~1
                Needs: dsn, uman
                """;

        assertThat(OftSyntaxCore.findSpecificationItems(text), hasSize(1));
        assertThat(OftSyntaxCore.findKeywords(text), hasSize(1));
    }

    @Test
    void extractsSpecificationItemsFromMarkdownCodeSpans() {
        final String text = """
                Covers:
                - `req~hello.world~1`
                """;

        assertThat(OftSyntaxCore.findSpecificationItems(text), hasSize(1));
    }

    @Test
    void extractsOnlyStandaloneDefinitionItemsForIndexing() {
        final String text = """
                req~hello.world~1
                Covers:
                - req~hello.world~1
                """;

        assertThat(OftSyntaxCore.findDefinitionSpecificationItems(text), hasSize(1));
    }

    @Test
    void extractsMarkdownDeclarationVariantsForIndexing() {
        final String text = """
                req~plain_markdown~1
                Needs: dsn

                `req~quoted_markdown~1`
                Needs: dsn
                """;

        assertThat(
                OftSyntaxCore.findDefinitionSpecificationItems(text).stream()
                        .map(match -> match.item().id())
                        .toList(),
                contains("req~plain_markdown~1", "req~quoted_markdown~1")
        );
    }

    @Test
    void extractsCoverageTagsWithOptionalSourceDetails() {
        final String revisionOnlyTag = "[impl~~2->" + "dsn~oft-syntax-core~1]";
        final String namedTag = "[impl~validate-password~2->" + "dsn~oft-syntax-core~1]";
        final String text = "// " + revisionOnlyTag + "\n"
                + "// " + namedTag + "\n";

        assertThat(OftSyntaxCore.findCoverageTags(text), hasSize(2));
    }

    @Test
    void extractsCoverageTagSourceAndTargetSpansAndEffectiveSource() {
        final String text = "// [impl->" + "req~openfasttrace_navigation_target~1]";
        final OftCoverageTagMatch match = OftSyntaxCore.findCoverageTags(text).get(0);

        assertThat(text.substring(match.sourceSpan().startOffset(), match.sourceSpan().endOffset()), is("impl"));
        assertThat(
                text.substring(match.targetSpan().startOffset(), match.targetSpan().endOffset()),
                is("req~openfasttrace_navigation_target~1")
        );
        assertThat(match.tag().effectiveSource().id(), is("impl~openfasttrace_navigation_target~1"));
    }
}
