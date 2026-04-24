package org.itsallcode.openfasttrace.intellijplugin.syntax;

public record OftCoverageTagMatch(
        OftCoverageTag tag,
        OftTextSpan span,
        OftTextSpan sourceSpan,
        OftTextSpan targetSpan
) {
}
