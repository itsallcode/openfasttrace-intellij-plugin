package org.itsallcode.openfasttrace.intellijplugin.navigation;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftMarkdownLinkDestinationContextTest {
    // [utest->dsn~suppress-specification-item-id-completion-in-markdown-link-targets-inside-covers-entries~1]
    @Test
    void givenMarkdownLinkDestinationWhenFindingContextThenItReturnsTheDestinationStart() {
        final String text = """
                Covers:
                * [req~live-template-alpha.feature~1](#<caret>)
                """;

        assertThat(findContext(text).isPresent(), is(true));
    }

    @Test
    void givenMarkdownLinkTextWhenFindingContextThenItReturnsNoContext() {
        final String text = """
                Covers:
                * [req~live-template-alpha<caret>.feature~1](#feature)
                """;

        assertThat(findContext(text), is(Optional.empty()));
    }

    @Test
    void givenNonLinkCoversTextWhenFindingContextThenItReturnsNoContext() {
        final String text = """
                Covers:
                * req~live-template-alpha<caret>.feature~1
                """;

        assertThat(findContext(text), is(Optional.empty()));
    }

    private static Optional<Integer> findContext(final String text) {
        final int offset = text.indexOf("<caret>");
        if (offset < 0) {
            throw new IllegalArgumentException("Missing <caret> marker in test text: " + text);
        }
        return OftMarkdownLinkDestinationContext.findAt(text.replace("<caret>", ""), offset);
    }
}
