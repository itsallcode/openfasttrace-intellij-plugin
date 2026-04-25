package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.progress.ProcessCanceledException;
import org.itsallcode.openfasttrace.api.ColorScheme;
import org.itsallcode.openfasttrace.api.DetailsSectionDisplay;
import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.api.importer.ImporterContext;
import org.itsallcode.openfasttrace.api.importer.ImporterService;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.itsallcode.openfasttrace.api.report.ReportConstants;
import org.itsallcode.openfasttrace.api.report.ReportVerbosity;
import org.itsallcode.openfasttrace.core.importer.ImporterFactoryLoader;
import org.itsallcode.openfasttrace.core.importer.ImporterServiceImpl;
import org.itsallcode.openfasttrace.core.Oft;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

final class OftTraceService {
    private final Oft oft;

    // [impl->dsn~trace-execution-service~1]
    OftTraceService() {
        this(Oft.create());
    }

    OftTraceService(final Oft oft) {
        this.oft = oft;
    }

    // [impl->dsn~show-successful-trace-output-in-ide-output-window~1]
    // [impl->dsn~show-scanned-base-directory-in-trace-output-window~1]
    // [impl->dsn~show-failing-trace-output-in-ide-output-window~1]
    // [impl->dsn~preserve-defect-count-for-unclean-trace-chain-in-output-window~1]
    OftTraceResult traceProject(final Path inputPath, final OftTraceProgress progress) {
        try {
            progress.phase("Importing OpenFastTrace items...", 0.15d);
            progress.checkCanceled();
            final List<SpecificationItem> items = importItems(inputPath);

            progress.phase("Linking OpenFastTrace items...", 0.4d);
            progress.checkCanceled();
            final List<LinkedSpecificationItem> linkedItems = oft.link(items);

            progress.phase("Tracing OpenFastTrace items...", 0.65d);
            progress.checkCanceled();
            final Trace trace = oft.trace(linkedItems);

            progress.phase("Rendering OpenFastTrace report...", 0.9d);
            progress.checkCanceled();
            final String output = buildTraceOutput(inputPath, trace);
            progress.phase("Finished OpenFastTrace trace.", 1.0d);
            return trace.hasNoDefects() ? OftTraceResult.success(output) : OftTraceResult.failure(output);
        } catch (final ProcessCanceledException exception) {
            throw exception;
        } catch (final RuntimeException exception) {
            return OftTraceResult.error(formatException(inputPath, exception));
        }
    }

    private List<SpecificationItem> importItems(final Path inputPath) {
        return runWithPluginClassLoader(() -> {
            final ImportSettings settings = ImportSettings.builder()
                    .addInputs(inputPath)
                    .build();
            final ImporterContext context = new ImporterContext(settings);
            final ImporterService importerService =
                    new ImporterServiceImpl(new ImporterFactoryLoader(context), settings);
            context.setImporterService(importerService);
            return importerService.createImporter()
                    .importAny(List.of(inputPath))
                    .getImportedItems();
        });
    }

    private String buildTraceOutput(final Path inputPath, final Trace trace) {
        return "Scanning base directory: " + inputPath.toAbsolutePath().normalize()
                + System.lineSeparator()
                + System.lineSeparator()
                + renderTrace(trace);
    }

    private String renderTrace(final Trace trace) {
        final Path reportFile = createTemporaryReportPath();
        try {
            runWithPluginClassLoader(() -> {
                oft.reportToPath(trace, reportFile, createReportSettings());
                return null;
            });
            return Files.readString(reportFile);
        } catch (final IOException exception) {
            throw new IllegalStateException("Failed to read OpenFastTrace report from " + reportFile, exception);
        } finally {
            deleteTemporaryReportPath(reportFile);
        }
    }

    private ReportSettings createReportSettings() {
        return ReportSettings.builder()
                .outputFormat(ReportConstants.DEFAULT_REPORT_FORMAT)
                .verbosity(ReportVerbosity.FAILURE_DETAILS)
                .colorScheme(ColorScheme.BLACK_AND_WHITE)
                .detailsSectionDisplay(DetailsSectionDisplay.COLLAPSE)
                .build();
    }

    private Path createTemporaryReportPath() {
        try {
            return Files.createTempFile("openfasttrace-intellij-trace-", ".txt");
        } catch (final IOException exception) {
            throw new IllegalStateException("Failed to create a temporary OpenFastTrace report file.", exception);
        }
    }

    private void deleteTemporaryReportPath(final Path reportFile) {
        try {
            Files.deleteIfExists(reportFile);
        } catch (final IOException exception) {
            // Temporary report clean-up is best-effort only.
        }
    }

    private <T> T runWithPluginClassLoader(final Callable<T> action) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(OftTraceService.class.getClassLoader());
        try {
            return action.call();
        } catch (final RuntimeException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new IllegalStateException("Failed to run OpenFastTrace with the plugin class loader.", exception);
        } finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }

    private String formatException(final Path inputPath, final RuntimeException exception) {
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        return "OpenFastTrace trace failed for input path " + inputPath + System.lineSeparator()
                + System.lineSeparator()
                + stackTrace;
    }
}
