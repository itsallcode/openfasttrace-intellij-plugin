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
}
