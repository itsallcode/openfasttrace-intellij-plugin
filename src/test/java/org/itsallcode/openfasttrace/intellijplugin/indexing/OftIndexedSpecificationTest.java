package org.itsallcode.openfasttrace.intellijplugin.indexing;

import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftIndexedSpecificationTest {
    @Test
    void givenIndexedSpecificationWhenBuildingIdentifierThenItReturnsTheCompositeId() {
        final OftIndexedSpecification specification = new OftIndexedSpecification("req", "login.feature", 7, 42);

        assertThat(specification.id(), is("req~login.feature~7"));
    }

    @Test
    void givenMatchingSpecificationItemWhenMatchingThenItReturnsTrue() {
        final OftIndexedSpecification specification = new OftIndexedSpecification("req", "login.feature", 7, 42);

        assertThat(specification.matches(new OftSpecificationItem("req", "login.feature", 7)), is(true));
    }

    @ParameterizedTest
    @MethodSource("nonMatchingSpecificationItems")
    void givenNonMatchingSpecificationItemWhenMatchingThenItReturnsFalse(final OftSpecificationItem specificationItem) {
        final OftIndexedSpecification specification = new OftIndexedSpecification("req", "login.feature", 7, 42);

        assertThat(specification.matches(specificationItem), is(false));
    }

    private static Stream<OftSpecificationItem> nonMatchingSpecificationItems() {
        return Stream.of(
                new OftSpecificationItem("dsn", "login.feature", 7),
                new OftSpecificationItem("req", "other.feature", 7),
                new OftSpecificationItem("req", "login.feature", 8)
        );
    }
}
