package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiFile;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;

public final class OftHighlightingPassFactory implements TextEditorHighlightingPassFactory, DumbAware {
    @Override
    public TextEditorHighlightingPass createHighlightingPass(PsiFile file, Editor editor) {
        if (file.getVirtualFile() == null) {
            return null;
        }
        if (!OftSupportedFiles.isSpecificationFile(file.getVirtualFile())
                && !OftSupportedFiles.isCoverageTagFile(file.getVirtualFile())) {
            return null;
        }
        return new OftHighlightingPass(file);
    }
}
