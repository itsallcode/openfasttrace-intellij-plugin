package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftTraceServiceTest {
    @TempDir
    Path temporaryDirectory;

    // [itest->dsn~show-successful-trace-output-in-ide-output-window~1]
    @Test
    void testGivenCleanTraceInputWhenTracingThenItReturnsSuccessfulPlainTextOutput() throws IOException {
        writeSuccessfulTraceProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(temporaryDirectory, OftTraceProgress.NONE);

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(true)),
                () -> assertThat(
                        result.output(),
                        Matchers.startsWith(
                                "Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize()
                        )
                ),
                () -> assertThat(result.output(), Matchers.containsString("ok -")),
                () -> assertThat(result.output(), Matchers.not(Matchers.containsString("not ok")))
        );
    }

    // [itest->dsn~show-failing-trace-output-in-ide-output-window~1]
    @Test
    void testGivenDefectiveTraceInputWhenTracingThenItReturnsFailingPlainTextOutput() throws IOException {
        writeFailingTraceProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(temporaryDirectory, OftTraceProgress.NONE);

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(false)),
                () -> assertThat(
                        result.output(),
                        Matchers.startsWith(
                                "Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize()
                        )
                ),
                () -> assertThat(result.output(), Matchers.containsString("not ok")),
                () -> assertThat(result.output(), Matchers.containsString("req~trace_output_requirement~1"))
        );
    }

    // [itest->dsn~show-scanned-base-directory-in-trace-output-window~1]
    @Test
    void testGivenTraceInputWhenTracingThenItWritesTheScannedBaseDirectoryIntoTheTextOutput()
            throws IOException {
        writeSuccessfulTraceProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(temporaryDirectory, OftTraceProgress.NONE);

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(true)),
                () -> assertThat(
                        result.output(),
                        Matchers.startsWith(
                                "Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize()
                                        + System.lineSeparator() + System.lineSeparator()
                        )
                ),
                () -> assertThat(result.output(), Matchers.containsString("ok -"))
        );
    }

    // [itest->dsn~preserve-defect-count-for-unclean-trace-chain-in-output-window~1]
    @Test
    void testGivenUncleanTraceChainWhenTracingThenItReportsTheExpectedDefectCountInTheTextOutput()
            throws IOException {
        writeUncleanTraceChainProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(temporaryDirectory, OftTraceProgress.NONE);

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(false)),
                () -> assertThat(result.output(), Matchers.containsString("not ok - 3 total, 3 defect")),
                () -> assertThat(result.output(), Matchers.containsString("dsn~chain_design~1")),
                () -> assertThat(result.output(), Matchers.containsString("feat~chain_feature~1")),
                () -> assertThat(result.output(), Matchers.containsString("req~chain_requirement~1"))
        );
    }

    private void writeSuccessfulTraceProject(final Path projectRoot) throws IOException {
        writeSpecification(projectRoot);
        final Path sourceDirectory = Files.createDirectories(projectRoot.resolve("src"));
        final String coverageTag = "[impl" + "->req~trace_output_requirement~1]";
        Files.writeString(
                sourceDirectory.resolve("Main.java"),
                "// " + coverageTag + System.lineSeparator()
                        + "class Main {" + System.lineSeparator()
                        + "}" + System.lineSeparator()
        );
    }

    private void writeFailingTraceProject(final Path projectRoot) throws IOException {
        writeSpecification(projectRoot);
    }

    private void writeUncleanTraceChainProject(final Path projectRoot) throws IOException {
        final Path docDirectory = Files.createDirectories(projectRoot.resolve("doc"));
        Files.writeString(
                docDirectory.resolve("trace.md"),
                """
                ### Feature
                `feat~chain_feature~1`

                Needs: req

                ### Requirement
                `req~chain_requirement~1`

                Covers:
                - `feat~chain_feature~1`

                Needs: dsn

                ### Design
                `dsn~chain_design~1`

                Covers:
                - `req~chain_requirement~1`

                Needs: impl
                """
        );
    }

    private void writeSpecification(final Path projectRoot) throws IOException {
        final Path docDirectory = Files.createDirectories(projectRoot.resolve("doc"));
        Files.writeString(
                docDirectory.resolve("trace.md"),
                """
                ### Feature
                `feat~trace_output_feature~1`

                Needs: req

                ### Requirement
                `req~trace_output_requirement~1`

                Covers:
                - `feat~trace_output_feature~1`

                Needs: impl
                """
        );
    }
}
