package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

final class OftTraceBackgroundRunner implements OftTraceRunner {
    private final OftTraceService traceService;
    private final OftTraceOutputPresenter outputPresenter;

    OftTraceBackgroundRunner(final OftTraceService traceService, final OftTraceOutputPresenter outputPresenter) {
        this.traceService = traceService;
        this.outputPresenter = outputPresenter;
    }

    // [impl->dsn~run-trace-project-in-background~1]
    @Override
    public void run(final Project project, final Path inputPath, final String contentTitle) {
        ProgressManager.getInstance().run(new Task.Backgroundable(
                project,
                "OpenFastTrace Trace Project",
                true
        ) {
            private OftTraceResult result;

            @Override
            public void run(final ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText2(inputPath.toString());
                result = traceService.traceProject(inputPath, new IndicatorProgress(indicator));
            }

            @Override
            public void onSuccess() {
                outputPresenter.show(project, contentTitle, result);
            }

            @Override
            public void onCancel() {
                outputPresenter.show(project, contentTitle, OftTraceResult.cancelled());
            }

            @Override
            public void onThrowable(final Throwable error) {
                if (error instanceof ProcessCanceledException) {
                    outputPresenter.show(project, contentTitle, OftTraceResult.cancelled());
                    return;
                }
                outputPresenter.show(project, contentTitle, OftTraceResult.error(formatThrowable(error)));
            }
        });
    }

    private String formatThrowable(final Throwable error) {
        final StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        return "OpenFastTrace trace failed unexpectedly."
                + System.lineSeparator()
                + System.lineSeparator()
                + stackTrace;
    }

    private static final class IndicatorProgress implements OftTraceProgress {
        private final ProgressIndicator indicator;

        private IndicatorProgress(final ProgressIndicator indicator) {
            this.indicator = indicator;
        }

        @Override
        public void phase(final String text, final double fraction) {
            indicator.setText(text);
            indicator.setFraction(fraction);
        }

        @Override
        public void checkCanceled() {
            indicator.checkCanceled();
        }
    }
}
