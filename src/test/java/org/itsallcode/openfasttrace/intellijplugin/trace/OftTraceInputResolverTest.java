package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class OftTraceInputResolverTest {
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
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolveProjectRoot(temporaryDirectory.toString());

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
    void testGivenWholeProjectSettingsWhenResolvingThenItUsesTheProjectBasePath(@TempDir final Path temporaryDirectory) {
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

    @Test
    void testGivenSelectedResourceSettingsWithMissingAdditionalPathWhenResolvingThenItReturnsAnInvalidResolution(
            @TempDir final Path temporaryDirectory
    ) {
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(
                        OftTraceScopeMode.SELECTED_RESOURCES,
                        false,
                        false,
                        "missing"
                )
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("does not exist"))
        );
    }

    @Test
    void testGivenSelectedResourceSettingsWithAbsoluteAdditionalPathWhenResolvingThenItReturnsAnInvalidResolution(
            @TempDir final Path temporaryDirectory
    ) {
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(
                        OftTraceScopeMode.SELECTED_RESOURCES,
                        false,
                        false,
                        temporaryDirectory.toString()
                )
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("must be project-relative"))
        );
    }

    @Test
    void testGivenSelectedResourceSettingsWithoutAnyInputsWhenResolvingThenItReturnsAnInvalidResolution(
            @TempDir final Path temporaryDirectory
    ) {
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(
                project,
                new OftTraceSettingsSnapshot(
                        OftTraceScopeMode.SELECTED_RESOURCES,
                        false,
                        false,
                        ""
                )
        );

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(
                        resolution.errorMessage(),
                        Matchers.containsString("does not resolve to any files or directories")
                )
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
    void testGivenRootPathWhenCheckingIdeaDirectoryThenItReturnsFalse() throws ReflectiveOperationException {
        final Path rootPath = FileSystems.getDefault().getRootDirectories().iterator().next();

        assertThat(
                invokeStatic("isIdeaDirectory", new Class<?>[]{Path.class}, rootPath),
                is(false)
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

    private static Object invokeStatic(final String methodName, final Class<?>[] parameterTypes, final Object... arguments)
            throws ReflectiveOperationException {
        final Method method = OftTraceInputResolver.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
