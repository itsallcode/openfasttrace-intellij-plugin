package org.itsallcode.openfasttrace.intellijplugin.syntax;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
    void extractsOnlyStandaloneDefinitionItemsForIndexing() {
        final String text = """
                req~hello.world~1
                Covers:
                - req~hello.world~1
                """;

        assertThat(OftSyntaxCore.findDefinitionSpecificationItems(text), hasSize(1));
    }

    @Test
    void extractsCoverageTagsWithOptionalSourceDetails() {
        final String revisionOnlyTag = "[impl~~2->" + "dsn~oft-syntax-core~1]";
        final String namedTag = "[impl~validate-password~2->" + "dsn~oft-syntax-core~1]";
        final String text = "// " + revisionOnlyTag + "\n"
                + "// " + namedTag + "\n";

        assertThat(OftSyntaxCore.findCoverageTags(text), hasSize(2));
    }
}
