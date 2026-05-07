package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.hamcrest.Matchers;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class OftTraceInputResolverTest {
    private static final String PROJECT_ROOT_ADDITIONAL_PATH = "<project-root>";

    @Test
    void testGivenNullBasePathWhenResolvingProjectRootThenItReturnsAnInvalidResolution() {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolveProjectRoot(null);

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(
                        resolution.errorMessage(),
                        Matchers.containsString("does not expose a local base path")
                )
        );
    }

    @Test
    void testGivenBlankBasePathWhenResolvingProjectRootThenItReturnsAnInvalidResolution() {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolveProjectRoot("   ");

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(
                        resolution.errorMessage(),
                        Matchers.containsString("does not expose a local base path")
                )
        );
    }

    @Test
    void testGivenMissingDirectoryWhenResolvingProjectRootThenItReturnsAnInvalidResolution(
            @TempDir final Path temporaryDirectory
    ) {
        final OftTraceInputResolution resolution =
                OftTraceInputResolver.resolveProjectRoot(temporaryDirectory.resolve("missing").toString());

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("does not exist"))
        );
    }

    @Test
    void testGivenFilePathWhenResolvingProjectRootThenItReturnsAnInvalidResolution(
            @TempDir final Path temporaryDirectory
    ) throws IOException {
        final Path file = Files.writeString(temporaryDirectory.resolve("build.gradle.kts"), "plugins {}");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolveProjectRoot(file.toString());

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("not a directory"))
        );
    }

    @Test
    void testGivenDirectoryWhenResolvingProjectRootThenItReturnsAWholeProjectResolution(
            @TempDir final Path temporaryDirectory
    ) {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolveProjectRoot(
                temporaryDirectory.toString()
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputs().isWholeProject(), is(true)),
                () -> assertThat(resolution.inputs().inputPaths(), contains(temporaryDirectory))
        );
    }

    @Test
    void testGivenInvalidBasePathStringWhenResolvingProjectRootThenItReturnsAnInvalidResolution() {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolveProjectRoot("\0");

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("base path is invalid"))
        );
    }

    @Test
    void testGivenWholeProjectSettingsWhenResolvingThenItUsesTheProjectBasePath(
            @TempDir final Path temporaryDirectory
    ) {
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(OftTraceScopeMode.WHOLE_PROJECT, true, true, "doc/")
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputs().isWholeProject(), is(true)),
                () -> assertThat(resolution.inputs().inputPaths(), contains(temporaryDirectory))
        );
    }

    @Test
    void testGivenInvalidBasePathAndValidProjectFilePathWhenResolvingThenItUsesTheProjectFileParent(
            @TempDir final Path temporaryDirectory
    ) throws IOException {
        final Path projectFile = Files.writeString(temporaryDirectory.resolve("trace-project.ipr"), "<project/>");
        final Project project = projectProxy("\0", projectFile.toString(), "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(OftTraceScopeMode.WHOLE_PROJECT, true, true, "doc/")
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputs().inputPaths(), contains(temporaryDirectory))
        );
    }

    @Test
    // [itest->dsn~add-project-relative-paths-to-selected-resource-trace~1]
    void testGivenSelectedResourceSettingsWhenResolvingThenItUsesAdditionalProjectRelativePaths(
            @TempDir final Path temporaryDirectory
    ) throws IOException {
        final Path docDirectory = Files.createDirectories(temporaryDirectory.resolve("doc"));
        final Path configFile = Files.writeString(temporaryDirectory.resolve("trace.conf"), "value");
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(
                        OftTraceScopeMode.SELECTED_RESOURCES,
                        false,
                        false,
                        "doc/\ntrace.conf"
                )
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputs().isWholeProject(), is(false)),
                () -> assertThat(resolution.inputs().inputPaths(), contains(docDirectory, configFile))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSelectedResourceSettings")
    void testGivenSelectedResourceSettingsWithInvalidInputsWhenResolvingThenItReturnsAnInvalidResolution(
            final String configuredAdditionalPaths,
            final String expectedErrorMessageFragment,
            @TempDir final Path temporaryDirectory
    ) {
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");
        final String additionalPathsText = PROJECT_ROOT_ADDITIONAL_PATH.equals(configuredAdditionalPaths)
                ? temporaryDirectory.toString()
                : configuredAdditionalPaths;

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(
                        OftTraceScopeMode.SELECTED_RESOURCES,
                        false,
                        false,
                        additionalPathsText
                )
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString(expectedErrorMessageFragment))
        );
    }

    @Test
    void testGivenIdeaDirectoryBasePathWhenResolvingThenItUsesTheProjectRoot(@TempDir final Path temporaryDirectory)
            throws IOException {
        final Path ideaDirectory = Files.createDirectories(temporaryDirectory.resolve(".idea"));
        final Path projectFile = Files.writeString(ideaDirectory.resolve("misc.xml"), "<project/>");
        final Project project = projectProxy(ideaDirectory.toString(), projectFile.toString(), "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(OftTraceScopeMode.WHOLE_PROJECT, true, true, "doc/")
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputs().inputPaths(), contains(temporaryDirectory))
        );
    }

    @Test
    void testGivenInvalidProjectPathWhenNormalizingThenItReturnsTheOriginalValue()
            throws ReflectiveOperationException {
        assertThat(
                invokeStatic("normalizeProjectPath", new Class<?>[]{String.class}, "\0"),
                is("\0")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGivenGuessedProjectDirectoryWhenResolvingFromGuessedDirectoryThenItUsesTheGuessedDirectory(
            @TempDir final Path temporaryDirectory
    ) throws ReflectiveOperationException {
        final Optional<OftTraceInputResolution> resolution = (Optional<OftTraceInputResolution>) invokeStatic(
                "resolveFromGuessedProjectDirectory",
                new Class<?>[]{Project.class, VirtualFile.class, OftTraceSettingsSnapshot.class},
                projectProxy(null, null, "trace-project"),
                virtualFileAt(temporaryDirectory),
                new OftTraceSettingsSnapshot(OftTraceScopeMode.WHOLE_PROJECT, true, true, "doc/")
        );

        assertThat(resolution.orElseThrow().inputs().inputPaths(), contains(temporaryDirectory));
    }

    @Test
    void testGivenBlankProjectPathWhenNormalizingThenItReturnsItUnchanged() throws ReflectiveOperationException {
        assertThat(
                invokeStatic("normalizeProjectPath", new Class<?>[]{String.class}, "  "),
                is("  ")
        );
    }

    @Test
    void testGivenIdeaDirectoryPathWhenLookingUpItsParentThenItReturnsTheProjectRoot(
            @TempDir final Path temporaryDirectory
    ) throws ReflectiveOperationException, IOException {
        final Path ideaDirectory = Files.createDirectories(temporaryDirectory.resolve(".idea"));

        assertThat(
                invokeStatic("ideaDirectoryParent", new Class<?>[]{Path.class}, ideaDirectory),
                is(temporaryDirectory)
        );
    }

    @Test
    void testGivenProjectFileInsideIdeaDirectoryWhenLookingUpItsParentThenItReturnsTheProjectRoot(
            @TempDir final Path temporaryDirectory
    ) throws ReflectiveOperationException, IOException {
        final Path projectFile = Files.writeString(
                Files.createDirectories(temporaryDirectory.resolve(".idea")).resolve("misc.xml"),
                "<project/>"
        );

        assertThat(
                invokeStatic("ideaDirectoryParent", new Class<?>[]{Path.class}, projectFile),
                is(temporaryDirectory)
        );
    }

    @Test
    void testGivenRegularProjectFileWhenNormalizingThenItReturnsTheParentDirectory(
            @TempDir final Path temporaryDirectory
    ) throws ReflectiveOperationException, IOException {
        final Path projectFile = Files.writeString(temporaryDirectory.resolve("project.ipr"), "<project/>");

        assertThat(
                invokeStatic("normalizeProjectPath", new Class<?>[]{String.class}, projectFile.toString()),
                is(temporaryDirectory.toString())
        );
    }

    @Test
    void testGivenSourceFolderWithoutVirtualFileWhenResolvingSourceFolderPathThenItReturnsEmpty()
            throws ReflectiveOperationException {
        final SourceFolder sourceFolder = sourceFolderProxy(null);

        assertThat(
                invokeStatic("sourceFolderPath", new Class<?>[]{SourceFolder.class}, sourceFolder),
                is(Optional.empty())
        );
    }

    @Test
    void testGivenRootPathWhenCheckingIdeaDirectoryThenItReturnsFalse() throws ReflectiveOperationException {
        final Path rootPath = FileSystems.getDefault().getRootDirectories().iterator().next();

        assertThat(
                invokeStatic("isIdeaDirectory", new Class<?>[]{Path.class}, rootPath),
                is(false)
        );
    }

    private static Stream<Arguments> invalidSelectedResourceSettings() {
        return Stream.of(
                Arguments.of("missing", "does not exist"),
                Arguments.of(PROJECT_ROOT_ADDITIONAL_PATH, "must be project-relative"),
                Arguments.of("bad\0path", "path is invalid"),
                Arguments.of("", "does not resolve to any files or directories")
        );
    }

    private Project projectProxy(final String basePath, final String projectFilePath, final String name) {
        return (Project) Proxy.newProxyInstance(
                Project.class.getClassLoader(),
                new Class<?>[]{Project.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBasePath" -> basePath;
                    case "getProjectFilePath" -> projectFilePath;
                    case "getName" -> name;
                    case "isDefault" -> false;
                    case "isDisposed" -> false;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "Project[" + name + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static SourceFolder sourceFolderProxy(final VirtualFile file) {
        return (SourceFolder) Proxy.newProxyInstance(
                SourceFolder.class.getClassLoader(),
                new Class<?>[]{SourceFolder.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getFile" -> file;
                    case "isTestSource" -> false;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "SourceFolder[" + file + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static VirtualFile virtualFileAt(final Path path) {
        return new LightVirtualFile(path.getFileName().toString()) {
            @Override
            public @NonNull String getPath() {
                return path.toString();
            }
        };
    }

    private static Object invokeStatic(
            final String methodName,
            final Class<?>[] parameterTypes,
            final Object... arguments
    )
            throws ReflectiveOperationException {
        final Method method = OftTraceInputResolver.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
