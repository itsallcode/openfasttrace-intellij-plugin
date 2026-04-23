package org.itsallcode.openfasttrace.intellijplugin.syntax;

public record OftCoverageTag(
        String sourceArtifactType,
        String sourceName,
        Integer sourceRevision,
        OftSpecificationItem target
) {
}
