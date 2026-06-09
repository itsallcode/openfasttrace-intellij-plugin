package org.itsallcode.openfasttrace.intellijplugin.trace;

import java.util.List;

record OftTraceTestTree(List<OftTraceSuiteNode> suites) {
    int testCount() {
        return suites.stream()
                .mapToInt(OftTraceSuiteNode::testCount)
                .sum();
    }

    boolean failed() {
        return suites.stream().anyMatch(OftTraceSuiteNode::failed);
    }
}

record OftTraceSuiteNode(String name, String sourcePath, List<OftTraceItemNode> items) {
    int testCount() {
        return items.stream()
                .mapToInt(OftTraceItemNode::testCount)
                .sum();
    }

    boolean failed() {
        return items.stream().anyMatch(OftTraceItemNode::failed);
    }

    OftTraceTestNodeDetails failureDetails() {
        return OftTraceTestNodeDetails.sourceSuiteFailure(name);
    }
}

record OftTraceItemNode(
        String name,
        String navigationId,
        boolean defective,
        OftTraceTestNodeDetails details,
        List<OftTraceLinkNode> links
) {
    int testCount() {
        return 1 + links.size();
    }

    boolean failed() {
        return defective || links.stream().anyMatch(OftTraceLinkNode::failed);
    }

    OftTraceTestNodeDetails failureDetails() {
        return defective
                ? details
                : OftTraceTestNodeDetails.specificationItemLinkFailure(navigationId);
    }
}

record OftTraceLinkNode(String name, String navigationId, boolean failed, OftTraceTestNodeDetails details) {
}
