package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftTraceInputResolverTest {
    @Test
    void testGivenNullBasePathWhenResolvingThenItReturnsAnInvalidResolution() {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve((String) null);

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(
                        resolution.errorMessage(),
                        Matchers.containsString("does not expose a local base path")
                )
        );
    }

    @Test
    void testGivenBlankBasePathWhenResolvingThenItReturnsAnInvalidResolution() {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve("   ");

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(
                        resolution.errorMessage(),
                        Matchers.containsString("does not expose a local base path")
                )
        );
    }

    @Test
    void testGivenMissingDirectoryWhenResolvingThenItReturnsAnInvalidResolution(@TempDir final Path temporaryDirectory) {
        final OftTraceInputResolution resolution =
                OftTraceInputResolver.resolve(temporaryDirectory.resolve("missing").toString());

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("does not exist"))
        );
    }

    @Test
    void testGivenFilePathWhenResolvingThenItReturnsAnInvalidResolution(@TempDir final Path temporaryDirectory)
            throws IOException {
        final Path file = Files.writeString(temporaryDirectory.resolve("build.gradle.kts"), "plugins {}");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(file.toString());

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("not a directory"))
        );
    }

    @Test
    void testGivenDirectoryWhenResolvingThenItReturnsAValidResolution(@TempDir final Path temporaryDirectory) {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(temporaryDirectory.toString());

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputPath(), is(temporaryDirectory))
        );
    }

    @Test
    void testGivenInvalidBasePathStringWhenResolvingThenItReturnsAnInvalidResolution() {
        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve("\0");

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(false)),
                () -> assertThat(resolution.errorMessage(), Matchers.containsString("base path is invalid"))
        );
    }

    @Test
    void testGivenProjectWithValidBasePathWhenResolvingThenItUsesTheProjectBasePath(@TempDir final Path temporaryDirectory) {
        final Project project = projectProxy(temporaryDirectory.toString(), null, "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(project);

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputPath(), is(temporaryDirectory))
        );
    }

    @Test
    void testGivenIdeaDirectoryBasePathWhenResolvingThenItUsesTheProjectRoot(@TempDir final Path temporaryDirectory)
            throws IOException {
        final Path ideaDirectory = Files.createDirectories(temporaryDirectory.resolve(".idea"));
        final Path projectFile = Files.writeString(ideaDirectory.resolve("misc.xml"), "<project/>");
        final Project project = projectProxy(ideaDirectory.toString(), projectFile.toString(), "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(project);

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputPath(), is(temporaryDirectory))
        );
    }

    @Test
    void testGivenInvalidBasePathAndRegularProjectFileWhenResolvingThenItUsesTheProjectFileParent(
            @TempDir final Path temporaryDirectory
    )
            throws IOException {
        final Path projectFile = Files.writeString(temporaryDirectory.resolve("build.gradle.kts"), "plugins {}");
        final Project project = projectProxy("missing", projectFile.toString(), "trace-project");

        final OftTraceInputResolution resolution = OftTraceInputResolver.resolve(project);

        Assertions.assertAll(
                () -> assertThat(resolution.isValid(), is(true)),
                () -> assertThat(resolution.inputPath(), is(temporaryDirectory))
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
                    case "isDisposed" -> false;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "Project[" + name + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
