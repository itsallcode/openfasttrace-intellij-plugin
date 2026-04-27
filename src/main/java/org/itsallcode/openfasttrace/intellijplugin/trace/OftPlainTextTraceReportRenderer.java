package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.report.plaintext.PlainTextReport;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

// Facade around the current plaintext reporter so the service does not depend directly on its internal API.
final class OftPlainTextTraceReportRenderer implements OftTraceReportRenderer {
    @Override
    public String render(final Trace trace, final ReportSettings settings) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new PlainTextReport(trace, settings).renderToStream(outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
