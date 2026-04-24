package org.itsallcode.openfasttrace.intellijplugin.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// [impl->dsn~oft-syntax-core~1]
public final class OftSyntaxCore {
    private static final String GROUP_KEYWORD = "keyword";
    private static final String NAME_PART = "\\p{L}[\\p{L}\\p{N}]*+(?:[._-][\\p{L}\\p{N}]++)*+";
    private static final String SPECIFICATION_ITEM_BODY = "[A-Za-z]+~" + NAME_PART + "~\\d+";
    private static final String NAMED_SPECIFICATION_ITEM_BODY =
            "(?<artifactType>[A-Za-z]+)~(?<name>" + NAME_PART + ")~(?<revision>\\d+)";
    private static final Pattern SPECIFICATION_ITEM_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\p{N}~._-])" + NAMED_SPECIFICATION_ITEM_BODY + "(?![\\p{L}\\p{N}~._-])"
    );
    private static final Pattern SPECIFICATION_ITEM_DEFINITION_PATTERN = Pattern.compile(
            "(?m)^\\s*(?<id>" + NAMED_SPECIFICATION_ITEM_BODY + ")\\s*$"
    );
    private static final Pattern SPECIFICATION_ITEM_EXACT_PATTERN = Pattern.compile("^" + SPECIFICATION_ITEM_BODY + "$");
    private static final Pattern INCOMPLETE_SPECIFICATION_ITEM_PATTERN = Pattern.compile(
            "^[A-Za-z]+(?:~" + NAME_PART + ")?~?$"
    );
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "(?m)^\\s*(?<" + GROUP_KEYWORD + ">Needs|Covers|Depends|Status|Description|Rationale|Comment|Tags):"
    );
    private static final Pattern COVERAGE_TAG_EXACT_PATTERN = Pattern.compile(
            "^\\[\\s*[A-Za-z]+(?:~~\\d+|~" + NAME_PART + "~\\d+)?\\s*->\\s*" + SPECIFICATION_ITEM_BODY + "\\s*]$"
    );
    private static final Pattern COVERAGE_TAG_PATTERN = Pattern.compile(
            "\\[\\s*(?<sourceId>(?<sourceArtifact>[A-Za-z]+)"
                    + "(?:~~(?<sourceRevision>\\d+)|~(?<sourceName>" + NAME_PART + ")~(?<sourceNamedRevision>\\d+))?)"
                    + "\\s*->\\s*(?<targetId>(?<targetArtifactType>[A-Za-z]+)"
                    + "~(?<targetName>" + NAME_PART + ")~(?<targetRevision>\\d+))\\s*]"
    );

    private OftSyntaxCore() {
    }

    public static OftFragmentStatus classifySpecificationItem(final String text) {
        if (SPECIFICATION_ITEM_EXACT_PATTERN.matcher(text).matches()) {
            return OftFragmentStatus.VALID;
        }
        if (text.contains("~") && INCOMPLETE_SPECIFICATION_ITEM_PATTERN.matcher(text).matches()) {
            return OftFragmentStatus.INCOMPLETE;
        }
        return OftFragmentStatus.INVALID;
    }

    public static OftFragmentStatus classifyCoverageTag(final String text) {
        if (COVERAGE_TAG_EXACT_PATTERN.matcher(text).matches()) {
            return OftFragmentStatus.VALID;
        }
        if (text.startsWith("[") && text.contains("->")) {
            if (!text.endsWith("]")) {
                return OftFragmentStatus.INCOMPLETE;
            }
            final String body = text.substring(1, text.length() - 1).trim();
            final int arrow = body.indexOf("->");
            if (arrow >= 0) {
                final String target = body.substring(arrow + 2).trim();
                if (target.isEmpty() || classifySpecificationItem(target) == OftFragmentStatus.INCOMPLETE) {
                    return OftFragmentStatus.INCOMPLETE;
                }
            }
        }
        return OftFragmentStatus.INVALID;
    }

    public static List<OftSpecificationItemMatch> findSpecificationItems(final CharSequence text) {
        return collectSpecificationItemMatches(text, SPECIFICATION_ITEM_PATTERN, false);
    }

    public static List<OftSpecificationItemMatch> findDefinitionSpecificationItems(final CharSequence text) {
        return collectSpecificationItemMatches(text, SPECIFICATION_ITEM_DEFINITION_PATTERN, true);
    }

    private static List<OftSpecificationItemMatch> collectSpecificationItemMatches(
            final CharSequence text, final Pattern pattern, final boolean useNamedIdGroup
    ) {
        final List<OftSpecificationItemMatch> matches = new ArrayList<>();
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(new OftSpecificationItemMatch(
                    specificationItemFrom(matcher),
                    specificationItemSpan(matcher, useNamedIdGroup)
            ));
        }
        return List.copyOf(matches);
    }

    public static List<OftCoverageTagMatch> findCoverageTags(final CharSequence text) {
        final List<OftCoverageTagMatch> matches = new ArrayList<>();
        final Matcher matcher = COVERAGE_TAG_PATTERN.matcher(text);
        while (matcher.find()) {
            final Integer sourceRevision = parseOptionalInt(matcher.group("sourceRevision"));
            final Integer sourceNamedRevision = parseOptionalInt(matcher.group("sourceNamedRevision"));
            final OftCoverageTag tag = new OftCoverageTag(
                    matcher.group("sourceArtifact"),
                    matcher.group("sourceName"),
                    sourceRevision != null ? sourceRevision : sourceNamedRevision,
                    targetSpecificationItemFrom(matcher)
            );
            matches.add(new OftCoverageTagMatch(
                    tag,
                    new OftTextSpan(matcher.start(), matcher.end()),
                    new OftTextSpan(matcher.start("sourceId"), matcher.end("sourceId")),
                    new OftTextSpan(matcher.start("targetId"), matcher.end("targetId"))
            ));
        }
        return List.copyOf(matches);
    }

    public static List<OftKeywordMatch> findKeywords(final CharSequence text) {
        final List<OftKeywordMatch> matches = new ArrayList<>();
        final Matcher matcher = KEYWORD_PATTERN.matcher(text);
        while (matcher.find()) {
            matches.add(new OftKeywordMatch(
                    matcher.group(GROUP_KEYWORD),
                    new OftTextSpan(matcher.start(GROUP_KEYWORD), matcher.end(GROUP_KEYWORD))
            ));
        }
        return List.copyOf(matches);
    }

    private static OftSpecificationItem specificationItemFrom(final Matcher matcher) {
        return new OftSpecificationItem(
                matcher.group("artifactType"),
                matcher.group("name"),
                Integer.parseInt(matcher.group("revision"))
        );
    }

    private static OftSpecificationItem targetSpecificationItemFrom(final Matcher matcher) {
        return new OftSpecificationItem(
                matcher.group("targetArtifactType"),
                matcher.group("targetName"),
                Integer.parseInt(matcher.group("targetRevision"))
        );
    }

    private static Integer parseOptionalInt(final String value) {
        return value == null ? null : Integer.parseInt(value);
    }

    private static OftTextSpan specificationItemSpan(final Matcher matcher, final boolean useNamedIdGroup) {
        if (useNamedIdGroup) {
            return new OftTextSpan(matcher.start("id"), matcher.end("id"));
        }
        return new OftTextSpan(matcher.start(), matcher.end());
    }
}
