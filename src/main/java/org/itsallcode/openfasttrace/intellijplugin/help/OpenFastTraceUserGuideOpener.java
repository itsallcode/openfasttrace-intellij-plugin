package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.project.Project;

@FunctionalInterface
interface OpenFastTraceUserGuideOpener {
    void open(final Project project, final String title, final String url);
}
