package org.itsallcode.openfasttrace.intellijplugin.indexing;

import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItem;

import java.io.Serial;
import java.io.Serializable;

public record OftIndexedSpecification(String artifactType, String name, int revision, int offset)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String id() {
        return artifactType + "~" + name + "~" + revision;
    }

    public static OftIndexedSpecification fromId(final String id) {
        final String[] parts = id.split("~", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid specification ID: " + id);
        }
        return new OftIndexedSpecification(parts[0], parts[1], Integer.parseInt(parts[2]), 0);
    }

    public boolean matches(final OftSpecificationItem item) {
        return artifactType.equals(item.artifactType())
                && name.equals(item.name())
                && revision == item.revision();
    }
}
