package org.itsallcode.openfasttrace.intellijplugin.navigation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftCoverageTagCompletionContextTest {
    private static final String ARROW = "-" + ">";

    // [utest->dsn~specification-item-completion~1]
    @ParameterizedTest
    @MethodSource("coverageTagTargetContexts")
    void givenCoverageTagTargetWhenFindingContextThenItReturnsTheTargetPrefix(
            final String text,
            final String expectedPrefix
    ) {
        final MarkedText markedText = markedText(text);

        final Optional<OftCoverageTagCompletionContext> context =
                OftCoverageTagCompletionContext.findAt(markedText.text(), markedText.offset());

        assertThat(context.map(OftCoverageTagCompletionContext::prefix), is(Optional.of(expectedPrefix)));
    }

    // [utest->dsn~specification-item-completion~1]
    @ParameterizedTest
    @MethodSource("nonCoverageTagTargetContexts")
    void givenTextOutsideCoverageTagTargetWhenFindingContextThenItReturnsNoContext(final String text) {
        final MarkedText markedText = markedText(text);

        final Optional<OftCoverageTagCompletionContext> context =
                OftCoverageTagCompletionContext.findAt(markedText.text(), markedText.offset());

        assertThat(context, is(Optional.empty()));
    }

    private static Stream<Arguments> coverageTagTargetContexts() {
        return Stream.of(
                Arguments.of(tag("impl", "dsn~partial<caret>"), "dsn~partial"),
                Arguments.of(spacedTag("impl", "dsn~partial<caret>"), "dsn~partial"),
                Arguments.of(spacedTag("impl", "<caret>"), ""),
                Arguments.of(tag("impl~target~1", "dsn~partial<caret>"), "dsn~partial"),
                Arguments.of(incompleteTag("impl~~1", "dsn~partial<caret>"), "dsn~partial")
        );
    }

    private static Stream<String> nonCoverageTagTargetContexts() {
        return Stream.of(
                "// [impl<caret>" + ARROW + "dsn~partial]",
                tag("impl", "dsn~partial~1") + " <caret>",
                "// impl" + ARROW + "dsn~partial<caret>",
                "// [" + ARROW + "dsn~partial<caret>]",
                "// [impl dsn~partial<caret>]"
        );
    }

    private static String tag(final String source, final String target) {
        return "// [" + source + ARROW + target + "]";
    }

    private static String spacedTag(final String source, final String target) {
        return "// [" + source + " " + ARROW + " " + target + "]";
    }

    private static String incompleteTag(final String source, final String target) {
        return "// [" + source + ARROW + target;
    }

    private static MarkedText markedText(final String text) {
        final int offset = text.indexOf("<caret>");
        if (offset < 0) {
            throw new IllegalArgumentException("Missing <caret> marker in test text: " + text);
        }
        return new MarkedText(text.replace("<caret>", ""), offset);
    }

    private record MarkedText(String text, int offset) {
    }
}
