package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

final class OftTraceInputResolver {
    OftTraceInputResolution resolve(final Project project) {
        final OftTraceInputResolution basePathResolution = resolve(normalizeProjectPath(project.getBasePath()));
        if (basePathResolution.isValid()) {
            return basePathResolution;
        }
        final OftTraceInputResolution projectFileResolution = resolve(normalizeProjectPath(project.getProjectFilePath()));
        if (projectFileResolution.isValid()) {
            return projectFileResolution;
        }
        final VirtualFile projectDirectory = ProjectUtil.guessProjectDir(project);
        if (projectDirectory != null) {
            final OftTraceInputResolution guessedDirectoryResolution = resolve(normalizeProjectPath(projectDirectory.getPath()));
            if (guessedDirectoryResolution.isValid()) {
                return guessedDirectoryResolution;
            }
        }
        return basePathResolution;
    }

    OftTraceInputResolution resolve(final String basePath) {
        if (basePath == null || basePath.isBlank()) {
            return OftTraceInputResolution.invalid(
                    "The current project does not expose a local base path for tracing."
            );
        }
        final Path inputPath;
        try {
            inputPath = Path.of(basePath);
        } catch (final InvalidPathException exception) {
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

    private String normalizeProjectPath(final String projectPath) {
        if (projectPath == null || projectPath.isBlank()) {
            return projectPath;
        }
        final Path path;
        try {
            path = Path.of(projectPath);
        } catch (final InvalidPathException exception) {
            return projectPath;
        }
        final Path fileName = path.getFileName();
        if (fileName != null && ".idea".equals(fileName.toString()) && path.getParent() != null) {
            return path.getParent().toString();
        }
        final Path parent = path.getParent();
        if (parent != null) {
            final Path parentFileName = parent.getFileName();
            if (parentFileName != null && ".idea".equals(parentFileName.toString()) && parent.getParent() != null) {
                return parent.getParent().toString();
            }
        }
        if (Files.isRegularFile(path) && parent != null) {
            return parent.toString();
        }
        return projectPath;
    }
}
