package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.Trace;

interface OftTraceReportRenderer {
    String render(Trace trace, ReportSettings settings);
}
