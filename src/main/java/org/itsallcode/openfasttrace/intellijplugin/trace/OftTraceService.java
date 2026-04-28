package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.progress.ProcessCanceledException;
import org.itsallcode.openfasttrace.api.ColorScheme;
import org.itsallcode.openfasttrace.api.DetailsSectionDisplay;
import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.itsallcode.openfasttrace.api.report.ReportConstants;
import org.itsallcode.openfasttrace.api.report.ReportVerbosity;
import org.itsallcode.openfasttrace.core.Oft;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

final class OftTraceService {
    private static final ClassLoader PLUGIN_CLASS_LOADER = OftTraceService.class.getClassLoader();

    private final Oft oft;
    private final OftTraceReportRenderer reportRenderer;

    // [impl->dsn~trace-execution-service~1]
    OftTraceService() {
        this(Oft.create(), new OftPlainTextTraceReportRenderer());
    }

    OftTraceService(final Oft oft, final OftTraceReportRenderer reportRenderer) {
        this.oft = oft;
        this.reportRenderer = reportRenderer;
    }

    // [impl->dsn~show-successful-trace-output-in-ide-output-window~1]
    // [impl->dsn~show-scanned-base-directory-in-trace-output-window~1]
    // [impl->dsn~show-failing-trace-output-in-ide-output-window~1]
    // [impl->dsn~preserve-defect-count-for-unclean-trace-chain-in-output-window~1]
    OftTraceResult traceProject(final OftTraceInputs inputs, final OftTraceProgress progress) {
        try {
            progress.phase("Importing OpenFastTrace items...", 0.15D);
            progress.checkCanceled();
            final List<SpecificationItem> items = importItems(inputs);

            progress.phase("Linking OpenFastTrace items...", 0.4D);
            progress.checkCanceled();
            final List<LinkedSpecificationItem> linkedItems = oft.link(items);

            progress.phase("Tracing OpenFastTrace items...", 0.65D);
            progress.checkCanceled();
            final Trace trace = oft.trace(linkedItems);

            progress.phase("Rendering OpenFastTrace report...", 0.9D);
            progress.checkCanceled();
            final String output = buildTraceOutput(inputs, trace);
            progress.phase("Finished OpenFastTrace trace.", 1.0D);
            return trace.hasNoDefects() ? OftTraceResult.success(output) : OftTraceResult.failure(output);
        } catch (final ProcessCanceledException exception) {
            throw exception;
        } catch (final RuntimeException exception) {
            return OftTraceResult.error(formatException(inputs, exception));
        }
    }

    private List<SpecificationItem> importItems(final OftTraceInputs inputs) {
        final ImportSettings settings = ImportSettings.builder()
                .addInputs(inputs.inputPaths())
                .build();
        return runWithPluginClassLoader(() -> oft.importItems(settings));
    }

    private String buildTraceOutput(final OftTraceInputs inputs, final Trace trace) {
        return buildInputHeader(inputs) + renderTrace(trace);
    }

    private String buildInputHeader(final OftTraceInputs inputs) {
        if (inputs.isWholeProject()) {
            return "Scanning base directory: " + inputs.inputPaths().getFirst().toAbsolutePath().normalize()
                    + System.lineSeparator()
                    + System.lineSeparator();
        }
        // [impl->dsn~show-resolved-trace-inputs-in-trace-output-window~1]
        return "Scanning configured trace inputs:"
                + System.lineSeparator()
                + System.lineSeparator()
                + inputs.inputPaths().stream()
                        .map(path -> "- " + path.toAbsolutePath().normalize())
                        .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator()
                + System.lineSeparator();
    }

    private String renderTrace(final Trace trace) {
        return runWithPluginClassLoader(() -> reportRenderer.render(trace, createReportSettings()));
    }

    private static ReportSettings createReportSettings() {
        return ReportSettings.builder()
                .outputFormat(ReportConstants.DEFAULT_REPORT_FORMAT)
                .verbosity(ReportVerbosity.FAILURE_DETAILS)
                .colorScheme(ColorScheme.COLOR)
                .detailsSectionDisplay(DetailsSectionDisplay.COLLAPSE)
                .build();
    }

    private static <T> T runWithPluginClassLoader(final Callable<T> action) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        // OFT discovers importer and reporter plugins via ServiceLoader on the thread context class loader.
        currentThread.setContextClassLoader(PLUGIN_CLASS_LOADER);
        try {
            return callUnchecked(action);
        } finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }

    private static <T> T callUnchecked(final Callable<T> action) {
        try {
            return action.call();
        } catch (final RuntimeException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new IllegalStateException(
                    "Failed to run OpenFastTrace with the plugin class loader.",
                    exception
            );
        }
    }

    private static String formatException(final OftTraceInputs inputs, final RuntimeException exception) {
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        return "OpenFastTrace trace failed for input path(s) " + inputs.inputPaths() + System.lineSeparator()
                + System.lineSeparator()
                + stackTrace;
    }
}
