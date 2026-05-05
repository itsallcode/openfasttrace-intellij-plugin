package org.itsallcode.openfasttrace.intellijplugin.navigation;

import java.util.Optional;
import java.util.regex.Pattern;

// [impl->dsn~complete-specification-item-id-in-coverage-tag-target~1]
// [impl->dsn~complete-specification-item-id-in-spaced-coverage-tag-target~1]
// [impl->dsn~complete-specification-item-id-in-incomplete-coverage-tag-target~1]
// [impl->dsn~suppress-coverage-tag-target-completion-outside-target-context~1]
record OftCoverageTagCompletionContext(String prefix, int bracketStart) {
    private static final int NOT_FOUND = -1;
    private static final Pattern SOURCE_SIDE_PATTERN = Pattern.compile(
            "^\\s*\\p{L}++(?:~~\\d++|~[\\p{L}][\\p{L}\\p{N}]*+(?:[._-][\\p{L}\\p{N}]++)*+~\\d++)?\\s*$",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    static Optional<OftCoverageTagCompletionContext> findAt(final CharSequence text, final int offset) {
        final int boundedOffset = Math.clamp(offset, 0, text.length());
        final int lineStart = findLineStart(text, boundedOffset);
        final int bracketStart = findLastOpeningBracketBefore(text, lineStart, boundedOffset);
        if (bracketStart == NOT_FOUND || closesBeforeCaret(text, bracketStart, boundedOffset)) {
            return Optional.empty();
        }
        final int arrowStart = findArrowBeforeCaret(text, bracketStart + 1, boundedOffset);
        if (arrowStart == NOT_FOUND || !hasSourceArtifact(text, bracketStart + 1, arrowStart)) {
            return Optional.empty();
        }
        final int targetStart = arrowStart + 2;
        final String prefix = OftSpecificationCompletionSupport.specificationPrefixAt(text, boundedOffset);
        if (boundedOffset - prefix.length() < targetStart) {
            return Optional.empty();
        }
        return Optional.of(new OftCoverageTagCompletionContext(prefix, bracketStart));
    }

    private static int findLineStart(final CharSequence text, final int offset) {
        int lineStart = offset;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        return lineStart;
    }

    private static int findLastOpeningBracketBefore(
            final CharSequence text,
            final int startOffset,
            final int endOffset
    ) {
        for (int index = endOffset - 1; index >= startOffset; index--) {
            if (text.charAt(index) == '[') {
                return index;
            }
        }
        return NOT_FOUND;
    }

    private static boolean closesBeforeCaret(
            final CharSequence text,
            final int startOffset,
            final int endOffset
    ) {
        for (int index = startOffset + 1; index < endOffset; index++) {
            if (text.charAt(index) == ']') {
                return true;
            }
        }
        return false;
    }

    private static int findArrowBeforeCaret(
            final CharSequence text,
            final int startOffset,
            final int endOffset
    ) {
        for (int index = endOffset - 2; index >= startOffset; index--) {
            if (text.charAt(index) == '-' && text.charAt(index + 1) == '>') {
                return index;
            }
        }
        return NOT_FOUND;
    }

    private static boolean hasSourceArtifact(
            final CharSequence text,
            final int startOffset,
            final int endOffset
    ) {
        return SOURCE_SIDE_PATTERN.matcher(text.subSequence(startOffset, endOffset)).matches();
    }
}
