package org.itsallcode.openfasttrace.intellijplugin.syntax;

public record OftCoverageTag(
        String sourceArtifactType,
        String sourceName,
        Integer sourceRevision,
        OftSpecificationItem target
) {
    public OftSpecificationItem effectiveSource() {
        return new OftSpecificationItem(
                sourceArtifactType,
                sourceName != null ? sourceName : target.name(),
                sourceRevision != null ? sourceRevision : target.revision()
        );
    }
}
