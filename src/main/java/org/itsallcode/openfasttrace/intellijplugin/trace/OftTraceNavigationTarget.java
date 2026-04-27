package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.vfs.VirtualFile;

record OftTraceNavigationTarget(VirtualFile file, int offset) {
}
