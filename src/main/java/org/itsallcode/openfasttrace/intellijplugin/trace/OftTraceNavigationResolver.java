package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

final class OftTraceNavigationResolver {
    private static final Logger LOG = Logger.getInstance(OftTraceNavigationResolver.class);

    private final Project project;
    private final OftCoverageTagTraceItemNavigator coverageTagTraceItemNavigator;

    OftTraceNavigationResolver(final Project project) {
        this.project = project;
        this.coverageTagTraceItemNavigator = new OftCoverageTagTraceItemNavigator(project);
    }

    Optional<OftTraceNavigationTarget> resolve(final String specificationId) {
        return findDeclaredSpecificationTarget(specificationId)
                .or(() -> coverageTagTraceItemNavigator.resolve(specificationId));
    }

    // [impl->dsn~navigate-from-test-runner-source-files~1]
    Optional<OftTraceNavigationTarget> resolveSourceFile(final String sourcePath) {
        if (sourcePath == null || sourcePath.isBlank()) {
            return Optional.empty();
        }
        final Path path = sourcePath(sourcePath);
        if (path != null) {
            final VirtualFile file = LocalFileSystem.getInstance().findFileByNioFile(path);
            if (file != null) {
                return Optional.of(new OftTraceNavigationTarget(file, 0));
            }
        }
        return findIndexedSourceFile(sourcePath, path)
                .or(() -> findProjectSourceFile(sourcePath, path));
    }

    private Path sourcePath(final String sourcePath) {
        if (sourcePath == null || sourcePath.isBlank()) {
            return null;
        }
        try {
            final Path path = Path.of(sourcePath);
            if (path.isAbsolute()) {
                return path.normalize();
            }
            final String projectBasePath = project.getBasePath();
            return projectBasePath == null || projectBasePath.isBlank()
                    ? null
                    : Path.of(projectBasePath).resolve(path).normalize();
        } catch (final InvalidPathException exception) {
            LOG.debug("Ignoring invalid OFT trace source path: " + sourcePath, exception);
            return null;
        }
    }

    private Optional<OftTraceNavigationTarget> findIndexedSourceFile(
            final String sourcePath,
            final @Nullable Path path
    ) {
        final String fileName = sourceFileName(sourcePath, path);
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }
        final String requestedPath = displayPath(sourcePath);
        final String resolvedPath = path == null ? null : displayPath(path.toString());
        final String projectRelativeSourcePath = projectRelativeSourcePath(path).orElse(requestedPath);
        return FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project))
                .stream()
                .filter(file -> matchesSourcePath(file, requestedPath, resolvedPath, projectRelativeSourcePath))
                .findFirst()
                .map(file -> new OftTraceNavigationTarget(file, 0));
    }

    private static @Nullable String sourceFileName(final String sourcePath, final @Nullable Path path) {
        if (path != null) {
            final Path fileName = path.getFileName();
            if (fileName != null) {
                return fileName.toString();
            }
        }
        if (sourcePath == null || sourcePath.isBlank()) {
            return null;
        }
        final String displayPath = displayPath(sourcePath);
        final int lastSeparator = displayPath.lastIndexOf('/');
        if (lastSeparator < 0) {
            return displayPath;
        }
        return displayPath.substring(lastSeparator + 1);
    }

    private Optional<String> projectRelativeSourcePath(final @Nullable Path path) {
        if (path == null || !path.isAbsolute()) {
            return Optional.empty();
        }
        final String projectBasePath = project.getBasePath();
        if (projectBasePath == null || projectBasePath.isBlank()) {
            return Optional.empty();
        }
        final Path basePath = Path.of(projectBasePath).normalize();
        if (!path.startsWith(basePath)) {
            return Optional.empty();
        }
        return Optional.of(displayPath(basePath.relativize(path).toString()));
    }

    private Optional<OftTraceNavigationTarget> findProjectSourceFile(
            final String sourcePath,
            final @Nullable Path path
    ) {
        final String requestedPath = displayPath(sourcePath);
        final String resolvedPath = path == null ? null : displayPath(path.toString());
        final String projectRelativeSourcePath = projectRelativeSourcePath(path).orElse(requestedPath);
        final OftTraceNavigationTarget[] target = new OftTraceNavigationTarget[1];
        ProjectFileIndex.getInstance(project)
                .iterateContent(file -> processProjectSourceFile(
                        requestedPath,
                        resolvedPath,
                        projectRelativeSourcePath,
                        target,
                        file
                ));
        return Optional.ofNullable(target[0]);
    }

    private boolean processProjectSourceFile(
            final String requestedPath,
            final @Nullable String resolvedPath,
            final String projectRelativeSourcePath,
            final OftTraceNavigationTarget[] target,
            final VirtualFile file
    ) {
        if (matchesSourcePath(file, requestedPath, resolvedPath, projectRelativeSourcePath)) {
            target[0] = new OftTraceNavigationTarget(file, 0);
            return false;
        }
        return true;
    }

    private boolean matchesSourcePath(
            final VirtualFile file,
            final String requestedPath,
            final @Nullable String resolvedPath,
            final String projectRelativeSourcePath
    ) {
        final String filePath = displayPath(file.getPath());
        return matchesAbsoluteSourcePath(filePath, requestedPath, resolvedPath)
                || matchesProjectRelativeSourcePath(file, requestedPath, projectRelativeSourcePath)
                || matchesRelativeSourcePath(filePath, projectRelativeSourcePath);
    }

    private static boolean matchesAbsoluteSourcePath(
            final String filePath,
            final String requestedPath,
            final @Nullable String resolvedPath
    ) {
        return filePath.equals(requestedPath) || filePath.equals(resolvedPath);
    }

    private boolean matchesProjectRelativeSourcePath(
            final VirtualFile file,
            final String requestedPath,
            final String projectRelativeSourcePath
    ) {
        return projectRelativePath(file)
                .filter(path -> requestedPath.equals(path) || projectRelativeSourcePath.equals(path))
                .isPresent();
    }

    private static boolean matchesRelativeSourcePath(final String filePath, final String requestedPath) {
        return !requestedPath.startsWith("/")
                && requestedPath.contains("/")
                && filePath.endsWith("/" + requestedPath);
    }

    private Optional<String> projectRelativePath(final VirtualFile file) {
        final String projectBasePath = project.getBasePath();
        if (projectBasePath == null || projectBasePath.isBlank()) {
            return Optional.empty();
        }
        final String basePath = displayPath(projectBasePath);
        final String filePath = displayPath(file.getPath());
        if (!filePath.startsWith(basePath + "/")) {
            return Optional.empty();
        }
        return Optional.of(filePath.substring(basePath.length() + 1));
    }

    private static String displayPath(final String path) {
        return path.replace('\\', '/');
    }

    private Optional<OftTraceNavigationTarget> findDeclaredSpecificationTarget(final String specificationId) {
        final OftTraceNavigationTarget[] target = new OftTraceNavigationTarget[1];
        FileBasedIndex.getInstance().processValues(
                OftSpecificationIndex.SPECIFICATION_ID,
                specificationId,
                null,
                (file, values) -> {
                    if (!values.isEmpty()) {
                        target[0] = new OftTraceNavigationTarget(file, values.getFirst().offset());
                        return false;
                    }
                    return true;
                },
                GlobalSearchScope.projectScope(project)
        );
        return Optional.ofNullable(target[0]);
    }

}
