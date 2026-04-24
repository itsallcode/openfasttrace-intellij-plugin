package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

// [impl->dsn~specification-item-navigation-runtime~1]
public final class OftNavigationItem implements NavigationItem {
    private final Project project;
    private final VirtualFile file;
    private final OftIndexedSpecification specification;

    OftNavigationItem(final Project project, final VirtualFile file, final OftIndexedSpecification specification) {
        this.project = project;
        this.file = file;
        this.specification = specification;
    }

    @Override
    public String getName() {
        return specification.id();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @Nullable String getPresentableText() {
                return specification.id();
            }

            @Override
            public @Nullable String getLocationString() {
                final String basePath = project.getBasePath();
                return basePath == null ? file.getPresentableUrl() : FileUtil.getRelativePath(basePath, file.getPath(), '/');
            }

            @Override
            public @Nullable Icon getIcon(final boolean unused) {
                return file.getFileType().getIcon();
            }
        };
    }

    @Override
    public void navigate(final boolean requestFocus) {
        new OpenFileDescriptor(project, file, specification.offset()).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return file.isValid();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    VirtualFile getFile() {
        return file;
    }

    OftIndexedSpecification getSpecification() {
        return specification;
    }
}
