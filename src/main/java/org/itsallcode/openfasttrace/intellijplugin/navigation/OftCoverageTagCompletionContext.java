package org.itsallcode.openfasttrace.intellijplugin.navigation;

import java.util.Optional;

// [impl->dsn~complete-specification-item-id-in-coverage-tag-target~1]
// [impl->dsn~complete-specification-item-id-in-spaced-coverage-tag-target~1]
// [impl->dsn~complete-specification-item-id-in-incomplete-coverage-tag-target~1]
// [impl->dsn~suppress-coverage-tag-target-completion-outside-target-context~1]
record OftCoverageTagCompletionContext(String prefix, int bracketStart) {
    private static final int NOT_FOUND = -1;

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
        final String token = text.subSequence(startOffset, endOffset).toString().trim();
        if (token.isEmpty()) {
            return false;
        }
        final int separatorIndex = token.indexOf('~');
        if (separatorIndex == NOT_FOUND) {
            return isArtifactType(token);
        }
        if (!isArtifactType(token.substring(0, separatorIndex))) {
            return false;
        }
        final String remainder = token.substring(separatorIndex);
        return isRevisionOnlyArtifact(remainder) || isFullyQualifiedArtifact(remainder);
    }

    private static boolean isArtifactType(final String token) {
        return token.chars().allMatch(Character::isLetter);
    }

    private static boolean isRevisionOnlyArtifact(final String token) {
        return token.startsWith("~~")
                && token.length() > 2
                && token.substring(2).chars().allMatch(Character::isDigit);
    }

    private static boolean isFullyQualifiedArtifact(final String token) {
        if (!token.startsWith("~")) {
            return false;
        }
        final int revisionSeparator = token.lastIndexOf('~');
        if (revisionSeparator <= 1 || revisionSeparator == token.length() - 1) {
            return false;
        }
        return isArtifactName(token.substring(1, revisionSeparator))
                && token.substring(revisionSeparator + 1).chars().allMatch(Character::isDigit);
    }

    private static boolean isArtifactName(final String token) {
        if (token.isEmpty() || !Character.isLetter(token.charAt(0))) {
            return false;
        }
        boolean previousWasSeparator = false;
        for (int index = 0; index < token.length(); index++) {
            final char character = token.charAt(index);
            if (Character.isLetterOrDigit(character)) {
                previousWasSeparator = false;
                continue;
            }
            if (!isNameSeparator(character) || previousWasSeparator || index == token.length() - 1) {
                return false;
            }
            previousWasSeparator = true;
        }
        return true;
    }

    private static boolean isNameSeparator(final char character) {
        return character == '.' || character == '_' || character == '-';
    }
}
