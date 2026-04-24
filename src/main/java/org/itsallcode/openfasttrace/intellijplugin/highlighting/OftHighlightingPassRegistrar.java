package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.codeHighlighting.TextEditorHighlightingPassFactoryRegistrar;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar.Anchor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

public final class OftHighlightingPassRegistrar implements TextEditorHighlightingPassFactoryRegistrar, DumbAware {
    @Override
    public void registerHighlightingPassFactory(
            final TextEditorHighlightingPassRegistrar registrar,
            final Project project
    ) {
        registrar.registerTextEditorHighlightingPass(
                new OftHighlightingPassFactory(),
                Anchor.LAST,
                -1,
                false,
                false
        );
    }
}
