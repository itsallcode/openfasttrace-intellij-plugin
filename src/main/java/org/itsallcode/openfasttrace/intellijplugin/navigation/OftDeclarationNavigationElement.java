package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import javax.swing.Icon;
import java.util.Objects;

final class OftDeclarationNavigationElement extends FakePsiElement {
    private final transient PsiElement delegate;
    private final OftIndexedSpecification specification;

    OftDeclarationNavigationElement(final PsiElement delegate, final OftIndexedSpecification specification) {
        this.delegate = delegate;
        this.specification = specification;
    }

    @Override
    public PsiElement getParent() {
        return delegate == null ? null : delegate.getParent();
    }

    @Override
    public PsiFile getContainingFile() {
        return delegate == null ? null : delegate.getContainingFile();
    }

    @Override
    public @NonNull PsiElement getNavigationElement() {
        return requireDelegate();
    }

    @Override
    public int getTextOffset() {
        return specification.offset();
    }

    @Override
    public String getName() {
        return specification.id();
    }

    @Override
    public @NonNull String getPresentableText() {
        return specification.id();
    }

    @Override
    public @Nullable String getLocationString() {
        final PsiFile file = getContainingFile();
        if (file == null || file.getVirtualFile() == null) {
            return null;
        }
        final Project project = file.getProject();
        final String basePath = project.getBasePath();
        return basePath == null
                ? file.getVirtualFile().getPresentableUrl()
                : FileUtil.getRelativePath(basePath, file.getVirtualFile().getPath(), '/');
    }

    @Override
    public @Nullable Icon getIcon(final boolean unused) {
        final PsiFile file = getContainingFile();
        return file == null ? null : file.getFileType().getIcon();
    }

    @Override
    public boolean isValid() {
        return delegate != null && delegate.isValid();
    }

    @Override
    public PsiManager getManager() {
        return requireDelegate().getManager();
    }

    @Override
    public void navigate(final boolean requestFocus) {
        final PsiFile file = getContainingFile();
        if (file == null || file.getVirtualFile() == null) {
            return;
        }
        new OpenFileDescriptor(file.getProject(), file.getVirtualFile(), specification.offset()).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        final PsiFile file = getContainingFile();
        final VirtualFile virtualFile = file == null ? null : file.getVirtualFile();
        return virtualFile != null && virtualFile.isValid();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    private @NonNull PsiElement requireDelegate() {
        return Objects.requireNonNull(delegate, "navigation delegate is unavailable");
    }
}
