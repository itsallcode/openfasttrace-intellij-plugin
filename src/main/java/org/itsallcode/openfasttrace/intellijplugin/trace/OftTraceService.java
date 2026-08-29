package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.progress.ProcessCanceledException;
import org.itsallcode.openfasttrace.api.ColorScheme;
import org.itsallcode.openfasttrace.api.DetailsSectionDisplay;
import org.itsallcode.openfasttrace.api.FilterSettings;
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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public final class OftTraceService {
    private static final String UNTAGGED_ITEMS_FILTER_MARKER = "__oft-include-untagged__!";
    @SuppressWarnings("java:S3032")
    // OFT ServiceLoader discovery must use the plugin class loader, not an arbitrary caller context loader.
    private static final ClassLoader PLUGIN_CLASS_LOADER = OftTraceService.class.getClassLoader();

    private final Oft oft;
    private final OftTraceReportRenderer reportRenderer;
    private final boolean showTransitiveDefects;

    // [impl->dsn~trace-execution-service~1]
    public OftTraceService() {
        this(Oft.create(), new OftPlainTextTraceReportRenderer(), true);
    }

    public OftTraceService(final boolean showTransitiveDefects) {
        this(Oft.create(), new OftPlainTextTraceReportRenderer(), showTransitiveDefects);
    }

    OftTraceService(final Oft oft, final OftTraceReportRenderer reportRenderer) {
        this(oft, reportRenderer, true);
    }

    OftTraceService(
            final Oft oft,
            final OftTraceReportRenderer reportRenderer,
            final boolean showTransitiveDefects
    ) {
        this.oft = oft;
        this.reportRenderer = reportRenderer;
        this.showTransitiveDefects = showTransitiveDefects;
    }

    // [impl->dsn~show-successful-trace-output-in-ide-output-window~2]
    // [impl->dsn~show-scanned-base-directory-in-trace-output-window~1]
    // [impl->dsn~show-failing-trace-output-in-ide-output-window~1]
    // [impl->dsn~preserve-defect-count-for-unclean-trace-chain-in-output-window~1]
    // [impl->dsn~filter-trace-by-artifact-types-and-tags~1]
    // [impl->dsn~filter-trace-by-item-statuses~1]
    public OftTraceResult traceProject(final OftTraceInputs inputs, final OftTraceProgress progress) {
        try {
            progress.phase("Importing OpenFastTrace items...", 0.15D);
            progress.checkCanceled();
            final FilterSettings.Builder filterSettings = FilterSettings.builder();
            if (!inputs.artifactTypes().isEmpty()) {
                filterSettings.artifactTypes(Set.copyOf(inputs.artifactTypes()));
            }
            filterSettings.wantedStatuses(inputs.selectedStatuses());
            final Set<String> tags = createTagFilter(inputs);
            if (!tags.isEmpty()) {
                filterSettings.tags(tags);
                // OFT defaults to "without tags" mode unless this is explicitly disabled.
                filterSettings.withoutTags(inputs.includeUntagged());
            }
            final List<SpecificationItem> items = importItems(inputs.inputPaths(), filterSettings);

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
            return trace.hasNoDefects() ? OftTraceResult.success(output, trace) : OftTraceResult.failure(output, trace);
        } catch (final ProcessCanceledException exception) {
            throw exception;
        } catch (final RuntimeException exception) {
            return OftTraceResult.error(formatException(inputs, exception));
        }
    }


    private List<SpecificationItem> importItems(final List<Path> inputs, final FilterSettings.Builder filterSettings) {
        final ImportSettings settings = ImportSettings.builder()
                .addInputs(inputs)
                .filter(filterSettings.build())
                .build();
        return runWithPluginClassLoader(() -> oft.importItems(settings));
    }

    private String buildTraceOutput(final OftTraceInputs inputs, final Trace trace) {
        return buildInputHeader(inputs) + renderTrace(trace);
    }

    private static String buildInputHeader(final OftTraceInputs inputs) {
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

    // [impl->dsn~hide-transitive-defects-in-plain-text-output~1]
    // [impl->dsn~transitive-defect-visibility-is-controlled-by-the-run-configuration~1]
    private ReportSettings createReportSettings() {
        return ReportSettings.builder()
                .outputFormat(ReportConstants.DEFAULT_REPORT_FORMAT)
                .verbosity(showTransitiveDefects
                        ? ReportVerbosity.FAILURE_DETAILS
                        : ReportVerbosity.DIRECT_FAILURE_DETAILS)
                .colorScheme(ColorScheme.COLOR)
                .detailsSectionDisplay(DetailsSectionDisplay.COLLAPSE)
                .build();
    }

    private static Set<String> createTagFilter(final OftTraceInputs inputs) {
        if (!inputs.includeUntagged() && inputs.tags().isEmpty()) {
            return Set.of();
        }
        final Set<String> tags = new java.util.LinkedHashSet<>(inputs.tags());
        if (inputs.includeUntagged()) {
            tags.add(UNTAGGED_ITEMS_FILTER_MARKER);
        }
        return tags;
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
