package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

final class OftTraceNavigationResolver {
    private static final String OPTIONAL_WHITESPACE = "\\s*";
    private static final String ARTIFACT_TYPE = "\\p{Alpha}+";
    private static final String NAME_PART = "\\p{L}[\\p{L}\\p{N}]*(?:[._-][\\p{L}\\p{N}]+)*";
    private static final String COVERAGE_TAG_PATTERN = "\\[" + OPTIONAL_WHITESPACE
            + "(?<sourceId>(?<sourceArtifact>" + ARTIFACT_TYPE + ")"
            + "(?:~(?:(?<sourceName>" + NAME_PART + ")?~(?<sourceRevision>\\d+)))?)"
            + OPTIONAL_WHITESPACE + "->" + OPTIONAL_WHITESPACE
            + "(?<targetId>(?<targetArtifact>" + ARTIFACT_TYPE + ")~(?<targetName>" + NAME_PART
            + ")~(?<targetRevision>\\d+))"
            + OPTIONAL_WHITESPACE + "(?:>>" + OPTIONAL_WHITESPACE
            + "(?<needsArtifactTypes>\\p{Alpha}+(?:" + OPTIONAL_WHITESPACE + "," + OPTIONAL_WHITESPACE
            + "\\p{Alpha}+)*)" + OPTIONAL_WHITESPACE + ")?"
            + "]";
    private static final Pattern LINE_COVERAGE_TAG_PATTERN = Pattern.compile(COVERAGE_TAG_PATTERN);

    private final Project project;

    OftTraceNavigationResolver(final Project project) {
        this.project = project;
    }

    Optional<OftTraceNavigationTarget> resolve(final String specificationId) {
        return findDeclaredSpecificationTarget(specificationId)
                .or(() -> findCoverageTagSourceTarget(specificationId));
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

    private Optional<OftTraceNavigationTarget> findCoverageTagSourceTarget(final String specificationId) {
        final OftTraceNavigationTarget[] target = new OftTraceNavigationTarget[1];
        ProjectFileIndex.getInstance(project).iterateContent(file -> {
            if (!OftSupportedFiles.isCoverageTagFile(file)) {
                return true;
            }
            final OftTraceNavigationTarget match = findCoverageTagTargetInFile(file, specificationId);
            if (match != null) {
                target[0] = match;
                return false;
            }
            return true;
        });
        return Optional.ofNullable(target[0]);
    }

    private @Nullable OftTraceNavigationTarget findCoverageTagTargetInFile(
            final VirtualFile file,
            final String specificationId
    ) {
        try {
            final String text = currentFileText(file);
            int lineNumber = 1;
            int lineStartOffset = 0;
            int cursor = 0;
            while (cursor < text.length()) {
                final int lineEnd = findLineEnd(text, cursor);
                final String line = text.substring(cursor, lineEnd);
                final OftTraceNavigationTarget target = findCoverageTagTargetInLine(
                        file,
                        line,
                        lineNumber,
                        lineStartOffset,
                        specificationId
                );
                if (target != null) {
                    return target;
                }
                final int nextCursor = skipLineSeparator(text, lineEnd);
                lineStartOffset = nextCursor;
                cursor = nextCursor;
                lineNumber++;
            }
        } catch (final IOException exception) {
            return null;
        }
        return null;
    }

    private String currentFileText(final VirtualFile file) throws IOException {
        final Document document = FileDocumentManager.getInstance().getCachedDocument(file);
        if (document != null) {
            return document.getText();
        }
        return VfsUtilCore.loadText(file);
    }

    private @Nullable OftTraceNavigationTarget findCoverageTagTargetInLine(
            final VirtualFile file,
            final String line,
            final int lineNumber,
            final int lineStartOffset,
            final String specificationId
    ) {
        final Matcher matcher = LINE_COVERAGE_TAG_PATTERN.matcher(line);
        int lineMatchCount = 0;
        while (matcher.find()) {
            if (createCoverageTagSourceId(file, lineNumber, lineMatchCount, matcher).equals(specificationId)) {
                return new OftTraceNavigationTarget(file, lineStartOffset + matcher.start("sourceId"));
            }
            lineMatchCount++;
        }
        return null;
    }

    private String createCoverageTagSourceId(
            final VirtualFile file,
            final int lineNumber,
            final int lineMatchCount,
            final Matcher matcher
    ) {
        final String sourceName = matcher.group("sourceName");
        final int revision = parseRevision(matcher.group("sourceRevision"));
        final String name = sourceName != null
                ? sourceName
                : generateCoverageTagName(file, lineNumber, lineMatchCount, matcher);
        return matcher.group("sourceArtifact") + "~" + name + "~" + revision;
    }

    private String generateCoverageTagName(
            final VirtualFile file,
            final int lineNumber,
            final int lineMatchCount,
            final Matcher matcher
    ) {
        final String targetName = matcher.group("targetName");
        if (matcher.group("needsArtifactTypes") != null) {
            return targetName;
        }
        final String uniqueName = file.getPath() + lineNumber + lineMatchCount + matcher.group("targetId");
        return targetName + "-" + calculateCrc32(uniqueName);
    }

    private int parseRevision(final @Nullable String revisionText) {
        return revisionText == null ? 0 : Integer.parseInt(revisionText);
    }

    private long calculateCrc32(final String value) {
        final CRC32 checksum = new CRC32();
        checksum.update(value.getBytes(StandardCharsets.UTF_8));
        return checksum.getValue();
    }

    private int findLineEnd(final String text, final int cursor) {
        int index = cursor;
        while (index < text.length()) {
            final char current = text.charAt(index);
            if (current == '\n' || current == '\r') {
                return index;
            }
            index++;
        }
        return text.length();
    }

    private int skipLineSeparator(final String text, final int cursor) {
        if (cursor >= text.length()) {
            return cursor;
        }
        if (text.charAt(cursor) == '\r' && cursor + 1 < text.length() && text.charAt(cursor + 1) == '\n') {
            return cursor + 2;
        }
        return cursor + 1;
    }
}
