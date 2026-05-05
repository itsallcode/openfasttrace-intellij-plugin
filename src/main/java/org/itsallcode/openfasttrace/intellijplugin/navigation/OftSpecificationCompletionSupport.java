package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class OftSpecificationCompletionSupport {
    private OftSpecificationCompletionSupport() {
    }

    static List<OftIndexedSpecification> findMatchingSpecifications(final Project project, final String query) {
        final List<OftIndexedSpecification> matchingSpecifications = new ArrayList<>();
        FileBasedIndex.getInstance().processAllKeys(OftSpecificationIndex.SPECIFICATION_ID, key -> {
            final OftIndexedSpecification specification = OftIndexedSpecification.fromId(key);
            if (matchKind(specification, query) != MatchKind.NONE) {
                matchingSpecifications.add(specification);
            }
            return true;
        }, project);
        matchingSpecifications.sort(Comparator
                .comparing((OftIndexedSpecification specification) -> matchKind(specification, query))
                .thenComparing(OftIndexedSpecification::id));
        return List.copyOf(matchingSpecifications);
    }

    static MatchKind matchKind(final OftIndexedSpecification specification, final String query) {
        final String normalizedQuery = query.toLowerCase(Locale.ROOT);
        final String normalizedId = specification.id().toLowerCase(Locale.ROOT);
        if (normalizedId.startsWith(normalizedQuery)) {
            return MatchKind.FULL_ID_PREFIX;
        }
        final String normalizedName = specification.name().toLowerCase(Locale.ROOT);
        if (normalizedName.startsWith(normalizedQuery)) {
            return MatchKind.NAME_PREFIX;
        }
        if (normalizedName.contains(normalizedQuery)) {
            return MatchKind.NAME_SUBSTRING;
        }
        final String normalizedArtifactType = specification.artifactType().toLowerCase(Locale.ROOT);
        if (normalizedArtifactType.startsWith(normalizedQuery)) {
            return MatchKind.ARTIFACT_TYPE_PREFIX;
        }
        return MatchKind.NONE;
    }

    static double priorityOf(final MatchKind matchKind) {
        return switch (matchKind) {
            case FULL_ID_PREFIX -> 400;
            case NAME_PREFIX -> 300;
            case NAME_SUBSTRING -> 200;
            case ARTIFACT_TYPE_PREFIX -> 100;
            case NONE -> 0;
        };
    }

    static String specificationPrefixAt(final CharSequence text, final int offset) {
        final int boundedOffset = Math.clamp(offset, 0, text.length());
        int start = boundedOffset;
        while (start > 0 && isSpecificationCharacter(text.charAt(start - 1))) {
            start--;
        }
        return text.subSequence(start, boundedOffset).toString();
    }

    private static boolean isSpecificationCharacter(final char character) {
        return Character.isLetterOrDigit(character)
                || isSpecificationSeparator(character);
    }

    private static boolean isSpecificationSeparator(final char character) {
        return switch (character) {
            case '~', '.', '_', '-' -> true;
            default -> false;
        };
    }

    enum MatchKind {
        FULL_ID_PREFIX,
        NAME_PREFIX,
        NAME_SUBSTRING,
        ARTIFACT_TYPE_PREFIX,
        NONE
    }
}
