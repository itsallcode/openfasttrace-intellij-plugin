package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeInsight.daemon.impl.BackgroundUpdateHighlightersUtil;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiFile;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftCoverageTagMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftKeywordMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

// [impl->dsn~markdown-specification-support~1]
// [impl->dsn~rst-specification-support~1]
// [impl->dsn~coverage-tag-support~1]
public final class OftHighlightingPass extends TextEditorHighlightingPass implements DumbAware {
    private final PsiFile file;
    public OftHighlightingPass(final PsiFile file) {
        super(file.getProject(), file.getViewProvider().getDocument(), true);
        this.file = file;
    }

    @Override
    // [impl->dsn~highlight-markdown-specification-item~1]
    // [impl->dsn~ignore-invalid-markdown-specification-item~1]
    // [impl->dsn~tolerate-incomplete-markdown-specification-item~1]
    // [impl->dsn~highlight-rst-specification-item~1]
    // [impl->dsn~ignore-invalid-rst-specification-item~1]
    // [impl->dsn~tolerate-incomplete-rst-specification-item~1]
    // [impl->dsn~highlight-coverage-tag~1]
    // [impl->dsn~ignore-invalid-coverage-tag~1]
    // [impl->dsn~tolerate-incomplete-coverage-tag~1]
    public void doCollectInformation(final com.intellij.openapi.progress.@NonNull ProgressIndicator progress) {
        if (file.getVirtualFile() == null) {
            return;
        }
        final CharSequence text = myDocument.getImmutableCharSequence();
        final List<HighlightInfo> collected = new ArrayList<>();
        if (OftSupportedFiles.isSpecificationFile(file.getVirtualFile())) {
            for (OftSpecificationItemMatch match : OftSyntaxCore.findSpecificationItems(text)) {
                collected.add(info(match.span(), OftHighlighterKeys.SPECIFICATION_ITEM));
            }
            for (OftKeywordMatch match : OftSyntaxCore.findKeywords(text)) {
                collected.add(info(match.span(), OftHighlighterKeys.KEYWORD));
            }
        }
        if (OftSupportedFiles.isCoverageTagFile(file.getVirtualFile())) {
            for (OftCoverageTagMatch match : OftSyntaxCore.findCoverageTags(text)) {
                collected.add(info(match.span(), OftHighlighterKeys.COVERAGE_TAG));
            }
        }
        BackgroundUpdateHighlightersUtil.setHighlightersToEditor(
                myProject,
                file,
                myDocument,
                0,
                myDocument.getTextLength(),
                List.copyOf(collected),
                getId()
        );
    }

    @Override
    public void doApplyInformationToEditor() {
        // BackgroundUpdateHighlightersUtil applies the collected infos during the background phase.
    }

    private static HighlightInfo info(final OftTextSpan span, final TextAttributesKey key) {
        return HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION)
                .range(span.startOffset(), span.endOffset())
                .textAttributes(key)
                .createUnconditionally();
    }
}
