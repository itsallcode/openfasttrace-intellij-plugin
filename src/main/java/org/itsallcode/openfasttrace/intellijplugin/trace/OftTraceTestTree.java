package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.jetbrains.annotations.Nullable;

import java.util.List;

record OftTraceTestTree(List<OftTraceSuiteNode> suites) {
    int testCount() {
        return suites.stream()
                .mapToInt(suite -> suite.items().size())
                .sum();
    }

    boolean failed() {
        return suites.stream().anyMatch(OftTraceSuiteNode::failed);
    }

    record OftTraceSuiteNode(String name, @Nullable String sourcePath, List<OftTraceItemNode> items) {
        int testCount() {
            return items.size();
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
}
