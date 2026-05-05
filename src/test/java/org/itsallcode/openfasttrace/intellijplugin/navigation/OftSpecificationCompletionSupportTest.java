package org.itsallcode.openfasttrace.intellijplugin.navigation;

import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftSpecificationCompletionSupportTest {
    // [utest->dsn~specification-item-completion~1]
    @ParameterizedTest
    @CsvSource({
            "req~login.feature~1, req~log, FULL_ID_PREFIX",
            "req~login.feature~1, login, NAME_PREFIX",
            "req~user-login.feature~1, login, NAME_SUBSTRING",
            "req~login.feature~1, rq, NONE",
            "dsn~login.feature~1, ds, FULL_ID_PREFIX",
            "req~login.feature~1, trace, NONE"
    })
    void givenSpecificationAndQueryWhenMatchingThenItReturnsTheExpectedMatchKind(
            final String specificationId,
            final String query,
            final OftSpecificationCompletionSupport.MatchKind expectedMatchKind
    ) {
        final OftIndexedSpecification specification = OftIndexedSpecification.fromId(specificationId);

        assertThat(OftSpecificationCompletionSupport.matchKind(specification, query), is(expectedMatchKind));
    }

    @Test
    void givenUppercaseQueryWhenMatchingThenItMatchesCaseInsensitively() {
        final OftIndexedSpecification specification = OftIndexedSpecification.fromId("req~login.feature~1");

        assertThat(
                OftSpecificationCompletionSupport.matchKind(specification, "LOGIN"),
                is(OftSpecificationCompletionSupport.MatchKind.NAME_PREFIX)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "FULL_ID_PREFIX, 400.0",
            "NAME_PREFIX, 300.0",
            "NAME_SUBSTRING, 200.0",
            "ARTIFACT_TYPE_PREFIX, 100.0",
            "NONE, 0.0"
    })
    void givenMatchKindWhenGettingPriorityThenItReturnsExpectedPriority(
            final OftSpecificationCompletionSupport.MatchKind matchKind,
            final double expectedPriority
    ) {
        assertThat(OftSpecificationCompletionSupport.priorityOf(matchKind), is(expectedPriority));
    }

    @ParameterizedTest
    @CsvSource({
            "Covers:| - req~login.feature, 28, req~login.feature",
            "Covers:| - req~login.feature!, 29, ''",
            "req~bounded-offset~1, 999, req~bounded-offset~1",
            "req~bounded-offset~1, -1, ''",
            "prefix req~middle, 6, prefix"
    })
    void givenTextAndOffsetWhenExtractingPrefixThenItReturnsSpecificationPrefix(
            final String text,
            final int offset,
            final String expectedPrefix
    ) {
        assertThat(
                OftSpecificationCompletionSupport.specificationPrefixAt(text.replace('|', '\n'), offset),
                is(expectedPrefix)
        );
    }
}
