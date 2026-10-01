package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.DialogMessage;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidatorEx;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftFragmentStatus;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// [impl->dsn~specification-item-rename~1]
public final class OftRenameInputValidator implements RenameInputValidatorEx {
    @Override
    public @NotNull ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement().with(new PatternCondition<>("oftSpecificationDeclaration") {
            @Override
            public boolean accepts(final @NotNull PsiElement element, final ProcessingContext context) {
                return element instanceof OftDeclarationNavigationElement
                        || OftDeclarationResolver.findDeclaredItem(element).isPresent();
            }
        });
    }

    @Override
    public boolean isInputValid(
            final @NotNull String newName,
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        return OftSyntaxCore.classifySpecificationItem(newName) == OftFragmentStatus.VALID;
    }

    @Override
    public @DialogMessage @Nullable String getErrorMessage(final @NotNull String newName, final @NotNull Project project) {
        if (OftSyntaxCore.classifySpecificationItem(newName) == OftFragmentStatus.VALID) {
            return null;
        }
        return "Specification item ID must have the form '<type>~<name>~<revision>', e.g. 'req~item-name~1'.";
    }
}
