package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jspecify.annotations.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
final class OftTraceBackgroundRunner implements OftTraceRunner {
    private final OftTraceService traceService;
    private final OftTraceOutputPresenter outputPresenter;

    OftTraceBackgroundRunner(final OftTraceService traceService, final OftTraceOutputPresenter outputPresenter) {
        this.traceService = traceService;
        this.outputPresenter = outputPresenter;
    }

    // [impl->dsn~run-trace-project-in-background~1]
    @Override
    public void run(final Project project, final OftTraceInputs inputs, final String contentTitle) {
        ProgressManager.getInstance().run(new TraceTask(project, inputs, contentTitle));
    }

    private static String formatThrowable(final Throwable error) {
        final StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        return "OpenFastTrace trace failed unexpectedly."
                + System.lineSeparator()
                + System.lineSeparator()
                + stackTrace;
    }

    private final class TraceTask extends Task.Backgroundable {
        private final OftTraceInputs inputs;
        private final String contentTitle;
        private OftTraceResult result;

        private TraceTask(final Project project, final OftTraceInputs inputs, final String contentTitle) {
            super(project, "OpenFastTrace Trace Project", true);
            this.inputs = inputs;
            this.contentTitle = contentTitle;
        }

        @Override
        public void run(final ProgressIndicator indicator) {
            indicator.setIndeterminate(false);
            indicator.setText2(inputs.progressText());
            result = traceService.traceProject(inputs, new IndicatorProgress(indicator));
        }

        @Override
        public void onSuccess() {
            outputPresenter.show(getProject(), contentTitle, result);
        }

        @Override
        public void onCancel() {
            outputPresenter.show(getProject(), contentTitle, OftTraceResult.cancelled());
        }

        @Override
        public void onThrowable(final @NonNull Throwable error) {
            if (error instanceof ProcessCanceledException) {
                outputPresenter.show(getProject(), contentTitle, OftTraceResult.cancelled());
                return;
            }
            outputPresenter.show(getProject(), contentTitle, OftTraceResult.error(formatThrowable(error)));
        }
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
