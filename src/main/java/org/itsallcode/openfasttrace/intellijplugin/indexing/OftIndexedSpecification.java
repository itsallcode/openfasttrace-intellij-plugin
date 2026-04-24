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

    public boolean matches(final OftSpecificationItem item) {
        return artifactType.equals(item.artifactType())
                && name.equals(item.name())
                && revision == item.revision();
    }
}
