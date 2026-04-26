package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OftTraceRunContentOutputPresenterTest extends AbstractOftPlatformTestCase {
    private static final Pattern ANSI_ESCAPE_SEQUENCE = Pattern.compile("\u001B\\[[;\\d]*m");
    private static final String FIRST_ITEM_ID = "req~long_requirement_00000~1";
    private static final String LAST_ITEM_ID = "req~long_requirement_01999~1";

    public void testGivenTwoThousandUncoveredRequirementsWhenPresentedThenTheIdeConsoleKeepsTheFullTraceOutput()
            throws IOException {
        final Path temporaryDirectory = Files.createTempDirectory("oft-trace-output-presenter");
        writeLongFailingTraceProject(temporaryDirectory, 2000);
        final OftTraceResult result = new OftTraceService().traceProject(temporaryDirectory, OftTraceProgress.NONE);
        final String renderedOutput = stripAnsi(result.output());
        final AtomicReference<ConsoleViewImpl> consoleRef = new AtomicReference<>();
        final String previousBufferSize = System.getProperty("idea.cycle.buffer.size");
        try {
            System.setProperty("idea.cycle.buffer.size", "104");
            final OftTraceRunContentOutputPresenter presenter = new OftTraceRunContentOutputPresenter(
                    project -> {
                        final ConsoleViewImpl console =
                                (ConsoleViewImpl) OftTraceRunContentOutputPresenter.createTraceConsole(project);
                        consoleRef.set(console);
                        return console;
                    },
                    (project, descriptor) -> {
                    }
            );

            ApplicationManager.getApplication().invokeAndWait(() -> presenter.show(
                    getProject(),
                    "OpenFastTrace Trace: truncation-test",
                    result
            ));
            ApplicationManager.getApplication().invokeAndWait(() -> consoleRef.get().waitAllRequests());

            final String consoleText = readConsoleText(consoleRef.get());

            assertThat(result.isSuccessful(), is(false));
            assertThat(consoleRef.get(), notNullValue());
            assertThat(renderedOutput, Matchers.containsString(FIRST_ITEM_ID));
            assertThat(renderedOutput, Matchers.containsString(LAST_ITEM_ID));
            assertThat(consoleText, Matchers.containsString(FIRST_ITEM_ID));
            assertThat(consoleText, Matchers.containsString(LAST_ITEM_ID));
            assertThat(consoleText, Matchers.containsString("not ok - 2000 total, 2000 defect"));
            assertThat(stripAnsi(consoleText), is(expectedConsoleText(renderedOutput)));
        } finally {
            disposeConsole(consoleRef.get());
            restoreCycleBufferSize(previousBufferSize);
        }
    }

    private String readConsoleText(final ConsoleViewImpl console) {
        final AtomicReference<String> text = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> text.set(console.getText()));
        return text.get();
    }

    private void restoreCycleBufferSize(final String previousBufferSize) {
        if (previousBufferSize == null) {
            System.clearProperty("idea.cycle.buffer.size");
        } else {
            System.setProperty("idea.cycle.buffer.size", previousBufferSize);
        }
    }

    private String expectedConsoleText(final String renderedOutput) {
        return "OpenFastTrace trace completed with defects."
                + System.lineSeparator()
                + System.lineSeparator()
                + renderedOutput
                + (renderedOutput.endsWith(System.lineSeparator()) ? "" : System.lineSeparator());
    }

    private void disposeConsole(final ConsoleViewImpl console) {
        if (console != null) {
            ApplicationManager.getApplication().invokeAndWait(() -> Disposer.dispose(console));
        }
    }

    private String stripAnsi(final String output) {
        return ANSI_ESCAPE_SEQUENCE.matcher(output).replaceAll("");
    }

    private void writeLongFailingTraceProject(final Path projectRoot, final int itemCount) throws IOException {
        final Path docDirectory = Files.createDirectories(projectRoot.resolve("doc"));
        final StringBuilder content = new StringBuilder(itemCount * 80);
        for (int index = 0; index < itemCount; index++) {
            content.append("### Requirement ").append(index).append(System.lineSeparator())
                    .append('`')
                    .append(String.format("req~long_requirement_%05d~1", index))
                    .append('`')
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("Needs: impl")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        Files.writeString(docDirectory.resolve("trace.md"), content);
    }
}
