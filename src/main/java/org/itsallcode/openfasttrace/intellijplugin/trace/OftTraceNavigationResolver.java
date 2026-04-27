package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.diagnostic.Logger;
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
import java.util.zip.CRC32;

final class OftTraceNavigationResolver {
    private static final Logger LOG = Logger.getInstance(OftTraceNavigationResolver.class);

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
        ProjectFileIndex.getInstance(project)
                .iterateContent(file -> processCoverageTagFile(specificationId, target, file));
        return Optional.ofNullable(target[0]);
    }

    private boolean processCoverageTagFile(
            final String specificationId,
            final OftTraceNavigationTarget[] target,
            final VirtualFile file
    ) {
        if (!OftSupportedFiles.isCoverageTagFile(file)) {
            return true;
        }
        final OftTraceNavigationTarget match = findCoverageTagTargetInFile(file, specificationId);
        if (match != null) {
            target[0] = match;
            return false;
        }
        return true;
    }

    private @Nullable OftTraceNavigationTarget findCoverageTagTargetInFile(
            final VirtualFile file,
            final String specificationId
    ) {
        final String text;
        try {
            text = currentFileText(file);
        } catch (final IllegalStateException exception) {
            LOG.debug("Failed to read OFT trace navigation source file: " + file.getPath(), exception);
            return null;
        }
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
        return null;
    }

    private static String currentFileText(final VirtualFile file) {
        final Document document = FileDocumentManager.getInstance().getCachedDocument(file);
        if (document != null) {
            return document.getText();
        }
        try {
            return VfsUtilCore.loadText(file);
        } catch (final IOException exception) {
            throw new IllegalStateException(
                    "Failed to load OFT trace navigation source file " + file.getPath(),
                    exception
            );
        }
    }

    private static @Nullable OftTraceNavigationTarget findCoverageTagTargetInLine(
            final VirtualFile file,
            final String line,
            final int lineNumber,
            final int lineStartOffset,
            final String specificationId
    ) {
        int searchStart = 0;
        int lineMatchCount = 0;
        while (searchStart < line.length()) {
            final int openingBracket = line.indexOf('[', searchStart);
            if (openingBracket < 0) {
                return null;
            }
            final int closingBracket = line.indexOf(']', openingBracket + 1);
            if (closingBracket < 0) {
                return null;
            }
            final ParsedCoverageTag tag = parseCoverageTag(line, openingBracket, closingBracket);
            if (tag != null) {
                if (createCoverageTagSourceId(file, lineNumber, lineMatchCount, tag).equals(specificationId)) {
                    return new OftTraceNavigationTarget(file, lineStartOffset + tag.sourceIdStartOffset());
                }
                lineMatchCount++;
            }
            searchStart = closingBracket + 1;
        }
        return null;
    }

    private static String createCoverageTagSourceId(
            final VirtualFile file,
            final int lineNumber,
            final int lineMatchCount,
            final ParsedCoverageTag tag
    ) {
        final String sourceName = tag.sourceName();
        final int revision = tag.sourceRevision();
        final String name = sourceName != null
                ? sourceName
                : generateCoverageTagName(file, lineNumber, lineMatchCount, tag);
        return tag.sourceArtifact() + "~" + name + "~" + revision;
    }

    private static String generateCoverageTagName(
            final VirtualFile file,
            final int lineNumber,
            final int lineMatchCount,
            final ParsedCoverageTag tag
    ) {
        final String targetName = tag.targetName();
        if (tag.hasNeedsArtifactTypes()) {
            return targetName;
        }
        final String uniqueName = file.getPath() + lineNumber + lineMatchCount + tag.targetId();
        return targetName + "-" + calculateCrc32(uniqueName);
    }

    private static long calculateCrc32(final String value) {
        final CRC32 checksum = new CRC32();
        checksum.update(value.getBytes(StandardCharsets.UTF_8));
        return checksum.getValue();
    }

    private static int findLineEnd(final String text, final int cursor) {
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

    private static int skipLineSeparator(final String text, final int cursor) {
        if (cursor >= text.length()) {
            return cursor;
        }
        if (text.charAt(cursor) == '\r' && cursor + 1 < text.length() && text.charAt(cursor + 1) == '\n') {
            return cursor + 2;
        }
        return cursor + 1;
    }

    private static @Nullable ParsedCoverageTag parseCoverageTag(
            final String line,
            final int openingBracket,
            final int closingBracket
    ) {
        int cursor = skipWhitespace(line, openingBracket + 1, closingBracket);
        final ParsedSource source = parseSource(line, cursor, closingBracket);
        if (source == null) {
            return null;
        }
        cursor = skipWhitespace(line, source.endOffset(), closingBracket);
        if (!startsWith(line, cursor, "->") || cursor + 2 > closingBracket) {
            return null;
        }
        cursor = skipWhitespace(line, cursor + 2, closingBracket);
        final ParsedTarget target = parseTarget(line, cursor, closingBracket);
        if (target == null) {
            return null;
        }
        cursor = skipWhitespace(line, target.endOffset(), closingBracket);
        boolean hasNeedsArtifactTypes = false;
        if (startsWith(line, cursor, ">>")) {
            cursor = skipWhitespace(line, cursor + 2, closingBracket);
            final int needsEndOffset = parseNeedsArtifactTypes(line, cursor, closingBracket);
            if (needsEndOffset < 0) {
                return null;
            }
            cursor = skipWhitespace(line, needsEndOffset, closingBracket);
            hasNeedsArtifactTypes = true;
        }
        if (cursor != closingBracket) {
            return null;
        }
        return new ParsedCoverageTag(
                skipWhitespace(line, openingBracket + 1, closingBracket),
                source.artifactType(),
                source.name(),
                source.revision(),
                target.id(),
                target.name(),
                hasNeedsArtifactTypes
        );
    }

    private static @Nullable ParsedSource parseSource(final String line, final int startOffset, final int limit) {
        final ParsedToken artifact = parseArtifactType(line, startOffset, limit);
        if (artifact == null) {
            return null;
        }
        return parseSourceBody(line, limit, artifact);
    }

    private static @Nullable ParsedTarget parseTarget(final String line, final int startOffset, final int limit) {
        final ParsedToken artifact = parseArtifactType(line, startOffset, limit);
        if (artifact == null || artifact.endOffset() >= limit || line.charAt(artifact.endOffset()) != '~') {
            return null;
        }
        final ParsedToken name = parseName(line, artifact.endOffset() + 1, limit);
        if (name == null || name.endOffset() >= limit || line.charAt(name.endOffset()) != '~') {
            return null;
        }
        final ParsedNumber revision = parseUnsignedInteger(line, name.endOffset() + 1, limit);
        if (revision == null) {
            return null;
        }
        return new ParsedTarget(
                artifact.value() + "~" + name.value() + "~" + revision.value(),
                name.value(),
                revision.endOffset()
        );
    }

    private static int parseNeedsArtifactTypes(final String line, final int startOffset, final int limit) {
        final ParsedToken firstArtifact = parseArtifactType(line, startOffset, limit);
        if (firstArtifact == null) {
            return -1;
        }
        int cursor = firstArtifact.endOffset();
        while (true) {
            final int commaOffset = skipWhitespace(line, cursor, limit);
            if (commaOffset >= limit || line.charAt(commaOffset) != ',') {
                return cursor;
            }
            final int nextArtifactOffset = skipWhitespace(line, commaOffset + 1, limit);
            final ParsedToken artifact = parseArtifactType(line, nextArtifactOffset, limit);
            if (artifact == null) {
                return -1;
            }
            cursor = artifact.endOffset();
        }
    }

    private static @Nullable ParsedToken parseArtifactType(final String line, final int startOffset, final int limit) {
        return parseToken(line, startOffset, limit, Character::isLetter);
    }

    private static @Nullable ParsedToken parseName(final String line, final int startOffset, final int limit) {
        if (startOffset >= limit || !Character.isLetter(line.charAt(startOffset))) {
            return null;
        }
        int cursor = advanceWhileLetterOrDigit(line, startOffset + 1, limit);
        while (cursor < limit) {
            final int nextCursor = advanceAfterNameSeparator(line, cursor, limit);
            if (nextCursor < 0) {
                break;
            }
            cursor = nextCursor;
        }
        return new ParsedToken(line.substring(startOffset, cursor), cursor);
    }

    private static @Nullable ParsedNumber parseUnsignedInteger(
            final String line,
            final int startOffset,
            final int limit
    ) {
        if (startOffset >= limit || !Character.isDigit(line.charAt(startOffset))) {
            return null;
        }
        int cursor = startOffset + 1;
        while (cursor < limit && Character.isDigit(line.charAt(cursor))) {
            cursor++;
        }
        return new ParsedNumber(Integer.parseInt(line.substring(startOffset, cursor)), cursor);
    }

    private static @Nullable ParsedToken parseToken(
            final String line,
            final int startOffset,
            final int limit,
            final CharacterPredicate predicate
    ) {
        if (startOffset >= limit || !predicate.test(line.charAt(startOffset))) {
            return null;
        }
        int cursor = startOffset + 1;
        while (cursor < limit && predicate.test(line.charAt(cursor))) {
            cursor++;
        }
        return new ParsedToken(line.substring(startOffset, cursor), cursor);
    }

    private static int skipWhitespace(final String value, final int startOffset, final int limit) {
        int cursor = startOffset;
        while (cursor < limit && Character.isWhitespace(value.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private static @Nullable ParsedSource parseSourceBody(
            final String line,
            final int limit,
            final ParsedToken artifact
    ) {
        final int cursor = artifact.endOffset();
        if (!isTildeAt(line, cursor, limit)) {
            return new ParsedSource(artifact.value(), null, 0, cursor);
        }
        final int detailsStartOffset = cursor + 1;
        if (detailsStartOffset >= limit) {
            return null;
        }
        if (isTildeAt(line, detailsStartOffset, limit)) {
            return parseUnnamedSource(line, limit, artifact.value(), detailsStartOffset);
        }
        return parseNamedSource(line, limit, artifact.value(), detailsStartOffset);
    }

    private static @Nullable ParsedSource parseUnnamedSource(
            final String line,
            final int limit,
            final String artifactType,
            final int revisionMarkerOffset
    ) {
        final ParsedNumber revision = parseUnsignedInteger(line, revisionMarkerOffset + 1, limit);
        if (revision == null) {
            return null;
        }
        return new ParsedSource(artifactType, null, revision.value(), revision.endOffset());
    }

    private static @Nullable ParsedSource parseNamedSource(
            final String line,
            final int limit,
            final String artifactType,
            final int nameStartOffset
    ) {
        final ParsedToken sourceName = parseName(line, nameStartOffset, limit);
        if (!isTokenFollowedByTilde(sourceName, line, limit)) {
            return null;
        }
        final ParsedNumber revision = parseUnsignedInteger(line, sourceName.endOffset() + 1, limit);
        if (revision == null) {
            return null;
        }
        return new ParsedSource(artifactType, sourceName.value(), revision.value(), revision.endOffset());
    }

    private static boolean isTildeAt(final String line, final int offset, final int limit) {
        return offset < limit && line.charAt(offset) == '~';
    }

    private static boolean isTokenFollowedByTilde(
            final @Nullable ParsedToken token,
            final String line,
            final int limit
    ) {
        return token != null && token.endOffset() < limit && line.charAt(token.endOffset()) == '~';
    }

    private static int advanceAfterNameSeparator(final String line, final int cursor, final int limit) {
        if (!isNameSeparatorAt(line, cursor, limit) || !hasNameCharacterAfterSeparator(line, cursor, limit)) {
            return -1;
        }
        return advanceWhileLetterOrDigit(line, cursor + 2, limit);
    }

    private static boolean isNameSeparatorAt(final String line, final int cursor, final int limit) {
        return cursor < limit && isNameSeparator(line.charAt(cursor));
    }

    private static boolean hasNameCharacterAfterSeparator(final String line, final int cursor, final int limit) {
        return cursor + 1 < limit && Character.isLetterOrDigit(line.charAt(cursor + 1));
    }

    private static boolean isNameSeparator(final char current) {
        return current == '.' || current == '_' || current == '-';
    }

    private static int advanceWhileLetterOrDigit(final String line, final int startOffset, final int limit) {
        int cursor = startOffset;
        while (cursor < limit && Character.isLetterOrDigit(line.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private static boolean startsWith(final String value, final int offset, final String prefix) {
        return offset >= 0 && offset + prefix.length() <= value.length() && value.startsWith(prefix, offset);
    }

    @FunctionalInterface
    private interface CharacterPredicate {
        boolean test(char value);
    }

    private record ParsedCoverageTag(
            int sourceIdStartOffset,
            String sourceArtifact,
            @Nullable String sourceName,
            int sourceRevision,
            String targetId,
            String targetName,
            boolean hasNeedsArtifactTypes
    ) {
    }

    private record ParsedSource(String artifactType, @Nullable String name, int revision, int endOffset) {
    }

    private record ParsedTarget(String id, String name, int endOffset) {
    }

    private record ParsedToken(String value, int endOffset) {
    }

    private record ParsedNumber(int value, int endOffset) {
    }
}
