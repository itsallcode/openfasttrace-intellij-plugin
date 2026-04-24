package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public final class OftHighlighterKeys {
    public static final TextAttributesKey SPECIFICATION_ITEM = TextAttributesKey.createTextAttributesKey(
            "OPENFASTTRACE_SPECIFICATION_ITEM",
            DefaultLanguageHighlighterColors.METADATA
    );
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey(
            "OPENFASTTRACE_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
    );
    public static final TextAttributesKey COVERAGE_TAG = TextAttributesKey.createTextAttributesKey(
            "OPENFASTTRACE_COVERAGE_TAG",
            DefaultLanguageHighlighterColors.DOC_COMMENT_TAG
    );

    private OftHighlighterKeys() {
    }
}
