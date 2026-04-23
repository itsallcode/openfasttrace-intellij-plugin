package org.itsallcode.openfasttrace.intellijplugin.indexing;

public record OftIndexedSpecification(String artifactType, String name, int revision, int offset) {
    public String id() {
        return artifactType + "~" + name + "~" + revision;
    }
}
