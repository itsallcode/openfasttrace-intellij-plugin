package org.itsallcode.openfasttrace.intellijplugin.navigation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftMarkdownSpecificationCompletionContextTest {
    // [utest->dsn~specification-item-completion~1]
    @ParameterizedTest
    @MethodSource("markdownDeclarationIdContexts")
    void givenMarkdownDeclarationIdWhenFindingContextThenItReturnsThePrefix(
            final String text,
            final String expectedPrefix
    ) {
        final MarkedText markedText = markedText(text);

        final Optional<String> context = OftMarkdownSpecificationCompletionContext.findAt(
                markedText.text(),
                markedText.offset()
        );

        assertThat(context, is(Optional.of(expectedPrefix)));
    }

    // [utest->dsn~specification-item-completion~1]
    @ParameterizedTest
    @MethodSource("nonMarkdownDeclarationIdContexts")
    void givenNonMarkdownDeclarationIdWhenFindingContextThenItReturnsNoContext(final String text) {
        final MarkedText markedText = markedText(text);

        final Optional<String> context = OftMarkdownSpecificationCompletionContext.findAt(
                markedText.text(),
                markedText.offset()
        );

        assertThat(context, is(Optional.empty()));
    }

    private static Stream<Arguments> markdownDeclarationIdContexts() {
        return Stream.of(
                Arguments.of("""
                        ### Title
                        req~markdown-completion.fea<caret>ture~1
                        """, "req~markdown-completion.fea"),
                Arguments.of("""
                        ### Title
                        `req~markdown-completion.fea<caret>ture~1`
                        """, "req~markdown-completion.fea"),
                Arguments.of("""
                        ### Title
                        req~markdown-completion<caret>.feature~1
                        """, "req~markdown-completion")
        );
    }

    private static Stream<String> nonMarkdownDeclarationIdContexts() {
        return Stream.of(
                """
                        ### <caret>Title
                        req~markdown-completion.feature~1
                        """,
                """
                        ### Title
                        Body text <caret>outside the ID field.
                        req~markdown-completion.feature~1
                        """,
                """
                        ### Title
                        req~markdown-completion.feature~1

                        More body text <caret>below the declaration.
                        """
        );
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
