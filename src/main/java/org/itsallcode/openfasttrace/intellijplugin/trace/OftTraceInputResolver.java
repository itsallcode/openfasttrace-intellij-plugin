package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

final class OftTraceInputResolver {
    private static final Logger LOG = Logger.getInstance(OftTraceInputResolver.class);

    private OftTraceInputResolver() {
        // Prevent constructor invocation.
    }

    static OftTraceInputResolution resolve(final Project project) {
        final OftTraceInputResolution basePathResolution = resolve(
                normalizeProjectPath(project.getBasePath())
        );
        if (basePathResolution.isValid()) {
            return basePathResolution;
        }
        final OftTraceInputResolution projectFileResolution = resolve(
                normalizeProjectPath(project.getProjectFilePath())
        );
        if (projectFileResolution.isValid()) {
            return projectFileResolution;
        }
        final VirtualFile projectDirectory = ProjectUtil.guessProjectDir(project);
        if (projectDirectory != null) {
            final OftTraceInputResolution guessedDirectoryResolution = resolve(
                    normalizeProjectPath(projectDirectory.getPath())
            );
            if (guessedDirectoryResolution.isValid()) {
                return guessedDirectoryResolution;
            }
        }
        return basePathResolution;
    }

    static OftTraceInputResolution resolve(final String basePath) {
        if (basePath == null || basePath.isBlank()) {
            return OftTraceInputResolution.invalid(
                    "The current project does not expose a local base path for tracing."
            );
        }
        final Path inputPath;
        try {
            inputPath = Path.of(basePath);
        } catch (final InvalidPathException exception) {
            LOG.debug("Ignoring invalid OpenFastTrace input path: " + basePath, exception);
            return OftTraceInputResolution.invalid(
                    "The current project base path is invalid: " + exception.getInput()
            );
        }
        if (!Files.exists(inputPath)) {
            return OftTraceInputResolution.invalid(
                    "The current project base path does not exist: " + inputPath
            );
        }
        if (!Files.isDirectory(inputPath)) {
            return OftTraceInputResolution.invalid(
                    "The current project base path is not a directory: " + inputPath
            );
        }
        return OftTraceInputResolution.valid(inputPath);
    }

    private static String normalizeProjectPath(final String projectPath) {
        if (projectPath == null || projectPath.isBlank()) {
            return projectPath;
        }
        final Path path;
        try {
            path = Path.of(projectPath);
        } catch (final InvalidPathException exception) {
            LOG.debug("Ignoring invalid OpenFastTrace project path: " + projectPath, exception);
            return projectPath;
        }
        final Path ideaDirectoryParent = ideaDirectoryParent(path);
        if (ideaDirectoryParent != null) {
            return ideaDirectoryParent.toString();
        }
        return parentDirectoryIfRegularFile(path)
                .map(Path::toString)
                .orElse(projectPath);
    }

    private static Path ideaDirectoryParent(final Path path) {
        if (isIdeaDirectory(path)) {
            return path.getParent();
        }
        final Path parent = path.getParent();
        if (parent != null && isIdeaDirectory(parent)) {
            return parent.getParent();
        }
        return null;
    }

    private static boolean isIdeaDirectory(final Path path) {
        final Path fileName = path.getFileName();
        return fileName != null && ".idea".equals(fileName.toString());
    }

    private static java.util.Optional<Path> parentDirectoryIfRegularFile(final Path path) {
        final Path parent = path.getParent();
        if (parent != null && Files.isRegularFile(path)) {
            return java.util.Optional.of(parent);
        }
        return java.util.Optional.empty();
    }
}
