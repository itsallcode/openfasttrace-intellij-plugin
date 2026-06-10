package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class OftTraceInputResolverPlatformTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~include-intellij-source-directories-in-selected-resource-trace~1]
    // [itest->dsn~include-intellij-test-directories-in-selected-resource-trace~1]
    public void testGivenSelectedResourceSettingsWhenResolvingThenItUsesIntellijSourceAndTestRoots() throws Exception {
        final Path contentRoot = createManagedTestArtifactDirectory("trace-input-resolver-content-root");
        final Path sourceDirectory = Files.createDirectories(contentRoot.resolve("src/main/java"));
        final Path testDirectory = Files.createDirectories(contentRoot.resolve("src/test/java"));
        final Path docDirectory = Files.createDirectories(contentRoot.resolve("doc"));
        final VirtualFile[] originalContentRoots = ModuleRootManager.getInstance(getModule()).getContentRoots();
        try {
            configureModuleRoots(contentRoot, sourceDirectory, testDirectory);

            final OftTraceInputResolution resolution = resolveFromProjectRoot(
                    getProject(),
                    contentRoot,
                    new OftTraceSettingsSnapshot(
                        OftTraceScopeMode.SELECTED_RESOURCES,
                        true,
                            true,
                            "doc/"
                    )
            );

            assertThat(resolution.isValid(), is(true));
            assertThat(
                    resolution.inputs().inputPaths(),
                    contains(sourceDirectory, testDirectory, docDirectory)
            );
        } finally {
            restoreContentRoots(originalContentRoots);
        }
    }

    private void configureModuleRoots(final Path contentRoot, final Path sourceDirectory, final Path testDirectory) {
        final VirtualFile contentRootFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(contentRoot);
        final VirtualFile sourceDirectoryFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(sourceDirectory);
        final VirtualFile testDirectoryFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(testDirectory);
        assertNotNull(contentRootFile);
        assertNotNull(sourceDirectoryFile);
        assertNotNull(testDirectoryFile);
        WriteAction.runAndWait(() -> {
            final ModifiableRootModel model = ModuleRootManager.getInstance(getModule()).getModifiableModel();
            for (final ContentEntry contentEntry : model.getContentEntries()) {
                model.removeContentEntry(contentEntry);
            }
            final ContentEntry contentEntry = model.addContentEntry(contentRootFile);
            contentEntry.addSourceFolder(sourceDirectoryFile, false);
            contentEntry.addSourceFolder(testDirectoryFile, true);
            model.commit();
        });
    }

    private void restoreContentRoots(final VirtualFile[] contentRoots) {
        WriteAction.runAndWait(() -> {
            final ModifiableRootModel model = ModuleRootManager.getInstance(getModule()).getModifiableModel();
            for (final ContentEntry contentEntry : model.getContentEntries()) {
                model.removeContentEntry(contentEntry);
            }
            for (final VirtualFile contentRoot : contentRoots) {
                model.addContentEntry(contentRoot);
            }
            model.commit();
        });
    }

    private static OftTraceInputResolution resolveFromProjectRoot(
            final com.intellij.openapi.project.Project project,
            final Path projectRoot,
            final OftTraceSettingsSnapshot settings
    ) throws ReflectiveOperationException {
        final Method method = OftTraceInputResolver.class.getDeclaredMethod(
                "resolveFromProjectRoot",
                com.intellij.openapi.project.Project.class,
                Path.class,
                OftTraceSettingsSnapshot.class
        );
        method.setAccessible(true);
        return (OftTraceInputResolution) method.invoke(null, project, projectRoot, settings);
    }
}
