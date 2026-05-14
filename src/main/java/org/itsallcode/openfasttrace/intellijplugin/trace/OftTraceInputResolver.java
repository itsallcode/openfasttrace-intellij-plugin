package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public final class OftTraceInputResolver {
    private static final Logger LOG = Logger.getInstance(OftTraceInputResolver.class);

    private OftTraceInputResolver() {
        // Prevent constructor invocation.
    }

    public static OftTraceInputResolution resolve(final Project project) {
        return resolve(project, OftTraceProjectSettings.getInstance(project).snapshot());
    }

    public static OftTraceInputResolution resolve(final Project project, final OftTraceSettingsSnapshot settings) {
        final OftTraceInputResolution projectRootResolution = resolveProjectRoot(
                normalizeProjectPath(project.getBasePath()),
                settings
        );
        if (projectRootResolution.isValid()) {
            return resolveFromProjectRoot(
                    project,
                    projectRootResolution.inputs().inputPaths().getFirst(),
                    settings
            );
        }
        final OftTraceInputResolution projectFileResolution = resolveProjectRoot(
                normalizeProjectPath(project.getProjectFilePath()),
                settings
        );
        if (projectFileResolution.isValid()) {
            return resolveFromProjectRoot(
                    project,
                    projectFileResolution.inputs().inputPaths().getFirst(),
                    settings
            );
        }
        return resolveFromGuessedProjectDirectory(project, settings).orElse(projectRootResolution);
    }

    private static Optional<OftTraceInputResolution> resolveFromGuessedProjectDirectory(
            final Project project,
            final OftTraceSettingsSnapshot settings
    ) {
        return resolveFromGuessedProjectDirectory(project, ProjectUtil.guessProjectDir(project), settings);
    }

    private static Optional<OftTraceInputResolution> resolveFromGuessedProjectDirectory(
            final Project project,
            final VirtualFile guessedProjectDirectory,
            final OftTraceSettingsSnapshot settings
    ) {
        return Optional.ofNullable(guessedProjectDirectory)
                .map(VirtualFile::getPath)
                .map(OftTraceInputResolver::normalizeProjectPath)
                .map(basePath -> resolveProjectRoot(basePath, settings))
                .filter(OftTraceInputResolution::isValid)
                .map(resolution -> resolveFromProjectRoot(
                        project,
                        resolution.inputs().inputPaths().getFirst(),
                        settings
                ));
    }

    static OftTraceInputResolution resolveProjectRoot(final String basePath, final OftTraceSettingsSnapshot settings) {
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
        return OftTraceInputResolution.valid(OftTraceInputs.wholeProject(
                inputPath,
                settings.artifactTypes(),
                settings.tags()
        ));
    }

    // [impl->dsn~trace-selected-project-resources~1]
    // [impl->dsn~include-intellij-source-directories-in-selected-resource-trace~1]
    // [impl->dsn~include-intellij-test-directories-in-selected-resource-trace~1]
    // [impl->dsn~add-project-relative-paths-to-selected-resource-trace~1]
    private static OftTraceInputResolution resolveFromProjectRoot(
            final Project project,
            final Path projectRoot,
            final OftTraceSettingsSnapshot settings
    ) {
        if (settings.scopeMode() == OftTraceScopeMode.WHOLE_PROJECT) {
            return OftTraceInputResolution.valid(OftTraceInputs.wholeProject(
                    projectRoot,
                    settings.artifactTypes(),
                    settings.tags()
            ));
        }
        final LinkedHashSet<Path> inputs = new LinkedHashSet<>();
        if (settings.includeSourceRoots()) {
            inputs.addAll(resolveModuleSourceFolders(project, false));
        }
        if (settings.includeTestRoots()) {
            inputs.addAll(resolveModuleSourceFolders(project, true));
        }
        for (final String additionalPath : settings.additionalPaths()) {
            final OftTraceInputResolution additionalPathResolution = resolveConfiguredAdditionalPath(
                    projectRoot,
                    additionalPath,
                    settings
            );
            if (!additionalPathResolution.isValid()) {
                return additionalPathResolution;
            }
            inputs.addAll(additionalPathResolution.inputs().inputPaths());
        }
        if (inputs.isEmpty()) {
            return OftTraceInputResolution.invalid(
                    "The current trace configuration does not resolve to any files or directories."
            );
        }
        return OftTraceInputResolution.valid(OftTraceInputs.selectedResources(
                List.copyOf(inputs),
                settings.artifactTypes(),
                settings.tags()
        ));
    }

    private static List<Path> resolveModuleSourceFolders(final Project project, final boolean testSource) {
        final LinkedHashSet<Path> paths = new LinkedHashSet<>();
        for (final Module module : ModuleManager.getInstance(project).getModules()) {
            for (final ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
                for (final SourceFolder sourceFolder : contentEntry.getSourceFolders()) {
                    addMatchingSourceFolderPath(paths, sourceFolder, testSource);
                }
            }
        }
        return List.copyOf(paths);
    }

    private static void addMatchingSourceFolderPath(
            final LinkedHashSet<Path> paths,
            final SourceFolder sourceFolder,
            final boolean testSource
    ) {
        if (sourceFolder.isTestSource() == testSource) {
            sourceFolderPath(sourceFolder).ifPresent(paths::add);
        }
    }

    private static Optional<Path> sourceFolderPath(final SourceFolder sourceFolder) {
        return Optional.ofNullable(sourceFolder.getFile())
                .map(VirtualFile::getPath)
                .map(Path::of);
    }

    // [impl->dsn~add-project-relative-paths-to-selected-resource-trace~1]
    private static OftTraceInputResolution resolveConfiguredAdditionalPath(
            final Path projectRoot,
            final String additionalPath,
            final OftTraceSettingsSnapshot settings
    ) {
        final Path relativePath;
        try {
            relativePath = Path.of(additionalPath);
        } catch (final InvalidPathException exception) {
            LOG.debug("Ignoring invalid configured OpenFastTrace input path: " + additionalPath, exception);
            return OftTraceInputResolution.invalid(
                    "The configured additional trace path is invalid: " + exception.getInput()
            );
        }
        if (relativePath.isAbsolute()) {
            return OftTraceInputResolution.invalid(
                    "The configured additional trace path must be project-relative: " + additionalPath
            );
        }
        final Path resolvedPath = projectRoot.resolve(relativePath).normalize();
        if (!Files.exists(resolvedPath)) {
            return OftTraceInputResolution.invalid(
                    "The configured additional trace path does not exist: " + resolvedPath
            );
        }
        if (!Files.isRegularFile(resolvedPath) && !Files.isDirectory(resolvedPath)) {
            return OftTraceInputResolution.invalid(
                    "The configured additional trace path is neither a file nor a directory: " + resolvedPath
            );
        }
        return OftTraceInputResolution.valid(OftTraceInputs.selectedResources(
                List.of(resolvedPath),
                settings.artifactTypes(),
                settings.tags()
        ));
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

    private static Optional<Path> parentDirectoryIfRegularFile(final Path path) {
        final Path parent = path.getParent();
        if (parent != null && Files.isRegularFile(path)) {
            return Optional.of(parent);
        }
        return Optional.empty();
    }
}
