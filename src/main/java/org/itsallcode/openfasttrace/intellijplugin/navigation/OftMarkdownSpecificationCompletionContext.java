package org.itsallcode.openfasttrace.intellijplugin.navigation;

import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;

import java.util.Optional;

// [impl->dsn~complete-markdown-specification-item-id-in-declaration-id-field~1]
final class OftMarkdownSpecificationCompletionContext {
    private OftMarkdownSpecificationCompletionContext() {
    }

    static Optional<String> findAt(final CharSequence text, final int offset) {
        return OftSyntaxCore.findDefinitionSpecificationItems(text).stream()
                .filter(match -> contains(match, offset))
                .map(match -> OftSpecificationCompletionSupport.specificationPrefixAt(text, offset))
                .findFirst();
    }

    private static boolean contains(final OftSpecificationItemMatch match, final int offset) {
        return match.span().startOffset() <= offset && offset < match.span().endOffset();
    }
}
