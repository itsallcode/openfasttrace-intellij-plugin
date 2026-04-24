package org.itsallcode.openfasttrace.intellijplugin.navigation;

import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OftDeclarationResolverTest {
    @ParameterizedTest
    @MethodSource("coverageTagReferences")
    void givenCoverageTagWhenFindingReferenceAtOffsetThenItReturnsTheReferencedSpecificationItem(
            final int offset,
            final String expectedReferenceId
    ) {
        final String text = "// [impl" + "->req~openfasttrace_navigation_target~1]";

        assertThat(
                OftDeclarationResolver.findReferenceAt(text, offset)
                        .orElseThrow()
                        .id(),
                is(expectedReferenceId)
        );
    }

    @Test
    void givenPlainSpecificationReferenceWhenFindingReferenceAtOffsetThenItReturnsTheSpecificationItem() {
        final String text = """
                Covers:
                - req~openfasttrace_navigation_target~1
                """;

        assertThat(
                OftDeclarationResolver.findReferenceAt(text, text.indexOf("req~openfasttrace_navigation_target~1"))
                        .orElseThrow()
                        .id(),
                is("req~openfasttrace_navigation_target~1")
        );
        assertThat(OftDeclarationResolver.findReferenceAt(text, 0).isEmpty(), is(true));
    }

    @Test
    void givenOffsetOutsideAnyReferenceWhenFindingReferenceThenItReturnsEmpty() {
        final String text = """
                Covers:
                - req~openfasttrace_navigation_target~1
                """;

        assertThat(OftDeclarationResolver.findReferenceAt(text, 0).isEmpty(), is(true));
    }

    @Test
    void givenMixedSectionsWhenFindingCoveredSpecificationItemsThenItReturnsOnlyItemsFromCoversSections() {
        final List<OftSpecificationItemMatch> matches = OftDeclarationResolver.findCoveredSpecificationItems("""
                req~openfasttrace_navigation_target~1
                Needs: dsn

                dsn~openfasttrace_navigation_design~1
                Covers:
                - req~openfasttrace_navigation_target~1
                - `impl~openfasttrace_navigation_target~1`
                Depends: req~ignored_outside_covers~1

                feat~another_declaration~1
                Covers:
                - tst~second_target~1
                """);

        assertThat(
                matches.stream().map(match -> match.item().id()).toList(),
                contains(
                        "req~openfasttrace_navigation_target~1",
                        "impl~openfasttrace_navigation_target~1",
                        "tst~second_target~1"
                )
        );
    }

    private static Stream<Arguments> coverageTagReferences() {
        final String targetId = "req~openfasttrace_navigation_target~1";
        final String coverageTag = "// [impl->" + targetId + "]";
        return Stream.of(
                arguments(coverageTag.indexOf("impl"), "impl~openfasttrace_navigation_target~1"),
                arguments(coverageTag.indexOf(targetId), targetId)
        );
    }
}
