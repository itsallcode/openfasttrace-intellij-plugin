package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.EdtTestUtil;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class OftTraceRunContentOutputPresenterTest extends AbstractOftPlatformTestCase {
    private static final Pattern ANSI_ESCAPE_SEQUENCE = Pattern.compile("\u001B\\[[;\\d]*m");
    private static final String FIRST_ITEM_ID = "req~long_requirement_00000~1";
    private static final String LAST_ITEM_ID = "req~long_requirement_01999~1";

    public void testGivenTwoThousandUncoveredRequirementsWhenPresentedThenTheIdeConsoleKeepsTheFullTraceOutput()
            throws IOException {
        final Path temporaryDirectory = createManagedTempDirectory("oft-trace-output-presenter");
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

    // [itest->dsn~open-specification-item-from-trace-output-window~1]
    public void testGivenTraceOutputContainsDeclaredSpecificationItemWhenPresentedThenItCreatesNavigableConsoleHyperlink() {
        myFixture.addFileToProject("doc/spec.md", """
                req~trace_output_navigation_target~1
                Needs: dsn
                """);
        final AtomicReference<ConsoleViewImpl> consoleRef = new AtomicReference<>();
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
        try {
            EdtTestUtil.runInEdtAndWait(() -> presenter.show(
                    getProject(),
                    "OpenFastTrace Trace: hyperlinks",
                    OftTraceResult.failure("not ok req~trace_output_navigation_target~1" + System.lineSeparator())
            ));
            ApplicationManager.getApplication().invokeAndWait(() -> consoleRef.get().waitAllRequests());
            ApplicationManager.getApplication().invokeAndWait(() -> Objects.requireNonNull(consoleRef.get().getHyperlinks()).waitForPendingFilters(5000));

            final String consoleText = readConsoleText(consoleRef.get());
            final HyperlinkInfo hyperlink = hyperlinkAt(
                    consoleRef.get(),
                    consoleText.indexOf("req~trace_output_navigation_target~1")
            );

            assertThat(hyperlink, notNullValue());

            EdtTestUtil.runInEdtAndWait(() -> hyperlink.navigate(getProject()));

            assertThat(selectedEditorFileName(), is("spec.md"));
        } finally {
            disposeConsole(consoleRef.get());
        }
    }

    // [itest->dsn~open-specification-item-from-trace-output-window~1]
    public void testGivenTraceOutputContainsAutoGeneratedCoverageTagItemWhenPresentedThenItCreatesNavigableConsoleHyperlink() {
        myFixture.addFileToProject("doc/design.md", """
                dsn~intellij-light-tests-keep-junit4-compatibility-dependency~1
                Needs: tst
                """);
        final var coverageTagFile = myFixture.addFileToProject(
                "src/AbstractOftPlatformTestCase.java",
                """
                        // [tst""" + "->dsn~intellij-light-tests-keep-junit4-compatibility-dependency~1]\n" + """
                        abstract class AbstractOftPlatformTestCase {
                        }
                        """
        );
        final AtomicReference<ConsoleViewImpl> consoleRef = new AtomicReference<>();
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
        try {
            final String generatedCoverageItemId = generatedCoverageItemId(
                    coverageTagFile.getVirtualFile().getPath(),
                    1,
                    0,
                    "tst",
                    "dsn~intellij-light-tests-keep-junit4-compatibility-dependency~1"
            );
            EdtTestUtil.runInEdtAndWait(() -> presenter.show(
                    getProject(),
                    "OpenFastTrace Trace: generated-hyperlinks",
                    OftTraceResult.failure("not ok " + generatedCoverageItemId + System.lineSeparator())
            ));
            ApplicationManager.getApplication().invokeAndWait(() -> consoleRef.get().waitAllRequests());
            ApplicationManager.getApplication().invokeAndWait(() -> Objects.requireNonNull(consoleRef.get().getHyperlinks()).waitForPendingFilters(5000));

            final String consoleText = readConsoleText(consoleRef.get());
            final HyperlinkInfo hyperlink = hyperlinkAt(
                    consoleRef.get(),
                    consoleText.indexOf(generatedCoverageItemId)
            );

            assertThat(hyperlink, notNullValue());

            EdtTestUtil.runInEdtAndWait(() -> hyperlink.navigate(getProject()));

            assertThat(selectedEditorFileName(), is("AbstractOftPlatformTestCase.java"));
        } finally {
            disposeConsole(consoleRef.get());
        }
    }

    private String readConsoleText(final ConsoleViewImpl console) {
        final AtomicReference<String> text = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> text.set(console.getText()));
        return text.get();
    }

    private HyperlinkInfo hyperlinkAt(final ConsoleViewImpl console, final int offset) {
        final AtomicReference<HyperlinkInfo> hyperlink = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() ->
                hyperlink.set(Objects.requireNonNull(console.getHyperlinks()).getHyperlinkAt(offset))
        );
        return hyperlink.get();
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

    private String generatedCoverageItemId(
            final String filePath,
            final int lineNumber,
            final int lineMatchCount,
            final String sourceArtifactType,
            final String coveredId
    ) {
        final String coveredName = coveredId.split("~")[1];
        return sourceArtifactType + "~" + coveredName + "-" + crc32(filePath + lineNumber + lineMatchCount + coveredId) + "~0";
    }

    private long crc32(final String value) {
        final CRC32 checksum = new CRC32();
        checksum.update(value.getBytes(StandardCharsets.UTF_8));
        return checksum.getValue();
    }

    private void disposeConsole(final ConsoleViewImpl console) {
        if (console != null) {
            ApplicationManager.getApplication().invokeAndWait(() -> Disposer.dispose(console));
        }
    }

    private String selectedEditorFileName() {
        final TextEditor selectedEditor = (TextEditor) FileEditorManager.getInstance(getProject()).getSelectedEditor();
        assertThat(selectedEditor, notNullValue());
        assertThat(selectedEditor.getFile(), Matchers.not(nullValue()));
        return selectedEditor.getFile().getName();
    }

    private String stripAnsi(final String output) {
        return ANSI_ESCAPE_SEQUENCE.matcher(output).replaceAll("");
    }

    private Path createManagedTempDirectory(final String directoryName) throws IOException {
        return Files.createDirectories(Path.of(myFixture.getTempDirFixture().getTempDirPath()).resolve(directoryName));
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
