package org.itsallcode.openfasttrace.intellijplugin;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

// [tst->dsn~intellij-light-tests-keep-junit4-compatibility-dependency~1]
public abstract class AbstractOftPlatformTestCase extends BasePlatformTestCase {
    private final List<Path> managedTestArtifactDirectories = new ArrayList<>();

    protected boolean hasHighlight(final List<HighlightInfo> infos, final String fragment, final TextAttributesKey key) {
        final String text = myFixture.getEditor().getDocument().getText();
        final int startOffset = text.indexOf(fragment);
        assertThat("Missing fragment in test text: " + fragment, startOffset, greaterThanOrEqualTo(0));
        final int endOffset = startOffset + fragment.length();
        return infos.stream().anyMatch(info ->
                info.getStartOffset() == startOffset
                        && info.getEndOffset() == endOffset
                        && key.equals(info.forcedTextAttributesKey)
        );
    }

    protected Path createManagedTestArtifactDirectory(final String directoryName) throws IOException {
        final Path directory = Path.of(
                "build",
                "test-artifacts",
                "intellij-platform-tests",
                getClass().getSimpleName(),
                directoryName
        ).toAbsolutePath().normalize();
        deleteRecursively(directory);
        Files.createDirectories(directory);
        managedTestArtifactDirectories.add(directory);
        return directory;
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            deleteManagedTestArtifactDirectories();
        } catch (final Throwable throwable) {
            addSuppressedException(throwable);
        } finally {
            super.tearDown();
        }
    }

    private void deleteManagedTestArtifactDirectories() throws IOException {
        for (int index = managedTestArtifactDirectories.size() - 1; index >= 0; index--) {
            deleteRecursively(managedTestArtifactDirectories.get(index));
        }
        managedTestArtifactDirectories.clear();
    }

    private static void deleteRecursively(final Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEachOrdered(currentPath -> {
                try {
                    Files.deleteIfExists(currentPath);
                } catch (final IOException exception) {
                    throw new IllegalStateException("Failed to delete test artifact path " + currentPath, exception);
                }
            });
        }
    }
}
