package org.itsallcode.openfasttrace.intellijplugin;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

// [tst->dsn~intellij-light-tests-keep-junit4-compatibility-dependency~1]
public abstract class AbstractOftPlatformTestCase extends BasePlatformTestCase {
    protected boolean hasHighlight(final List<HighlightInfo> infos, final String fragment, final TextAttributesKey key) {
        final String text = myFixture.getEditor().getDocument().getText();
        final int startOffset = text.indexOf(fragment);
        assertThat("Missing fragment in test text: " + fragment, startOffset, greaterThanOrEqualTo(0));
        final int endOffset = startOffset + fragment.length();
        return infos.stream().anyMatch(info ->
                info.getStartOffset() == startOffset
                        && info.getEndOffset() == endOffset
                        && key.equals(info.forcedTextAttributesKey)
        );
    }
}
