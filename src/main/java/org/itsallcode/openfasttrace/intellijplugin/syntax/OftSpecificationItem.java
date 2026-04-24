package org.itsallcode.openfasttrace.intellijplugin.syntax;

public record OftSpecificationItem(String artifactType, String name, int revision) {
    public String id() {
        return artifactType + "~" + name + "~" + revision;
    }
}
