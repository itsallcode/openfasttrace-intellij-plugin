package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.project.Project;

import java.nio.file.Path;

interface OftTraceRunner {
    void run(Project project, Path inputPath, String contentTitle);
}
