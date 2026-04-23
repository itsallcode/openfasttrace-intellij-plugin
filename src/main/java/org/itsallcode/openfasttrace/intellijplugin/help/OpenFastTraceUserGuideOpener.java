package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.project.Project;

@FunctionalInterface
interface OpenFastTraceUserGuideOpener {
    void open(Project project, String title, String url);
}
