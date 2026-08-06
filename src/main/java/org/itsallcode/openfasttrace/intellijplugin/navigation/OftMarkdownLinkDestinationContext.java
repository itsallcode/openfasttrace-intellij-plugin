package org.itsallcode.openfasttrace.intellijplugin.navigation;

import java.util.Optional;

final class OftMarkdownLinkDestinationContext {
    private static final int NOT_FOUND = -1;

    private OftMarkdownLinkDestinationContext() {
    }

    static Optional<Integer> findAt(final CharSequence text, final int offset) {
        final int boundedOffset = Math.clamp(offset, 0, text.length());
        final int lineStart = findLineStart(text, boundedOffset);
        final int linkDestinationStart = findLastLinkDestinationStartBefore(text, lineStart, boundedOffset);
        if (linkDestinationStart == NOT_FOUND || closesBeforeCaret(text, linkDestinationStart, boundedOffset)) {
            return Optional.empty();
        }
        return Optional.of(linkDestinationStart);
    }

    private static int findLineStart(final CharSequence text, final int offset) {
        int lineStart = offset;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        return lineStart;
    }

    private static int findLastLinkDestinationStartBefore(
            final CharSequence text,
            final int startOffset,
            final int endOffset
    ) {
        for (int index = endOffset - 2; index >= startOffset; index--) {
            if (text.charAt(index) == ']' && text.charAt(index + 1) == '(') {
                return index + 2;
            }
        }
        return NOT_FOUND;
    }

    private static boolean closesBeforeCaret(
            final CharSequence text,
            final int startOffset,
            final int endOffset
    ) {
        for (int index = startOffset; index < endOffset; index++) {
            if (text.charAt(index) == ')') {
                return true;
            }
        }
        return false;
    }
}
