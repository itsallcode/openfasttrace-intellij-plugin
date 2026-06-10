package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.core.Oft;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLClassLoader;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftTraceServiceTest {
    private static final Pattern ANSI_ESCAPE_SEQUENCE = Pattern.compile("\u001B\\[[;\\d]*m");

    // [itest->dsn~show-successful-trace-output-in-ide-output-window~1]
    @Test
    void testGivenCleanTraceInputWhenTracingThenItReturnsSuccessfulPlainTextOutput(@TempDir final Path temporaryDirectory)
            throws IOException {
        writeSuccessfulTraceProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(
                OftTraceInputs.wholeProject(temporaryDirectory),
                OftTraceProgress.NONE
        );
        final String renderedOutput = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(true)),
                () -> assertThat(
                        renderedOutput,
                        Matchers.startsWith(
                                "Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize()
                        )
                ),
                () -> assertThat(result.output(), Matchers.containsString("\u001B[")),
                () -> assertThat(renderedOutput, Matchers.containsString("ok -")),
                () -> assertThat(renderedOutput, Matchers.not(Matchers.containsString("not ok")))
        );
    }

    @Test
    void testGivenForeignThreadContextClassLoaderWhenTracingThenItStillImportsProjectItems(
            @TempDir final Path temporaryDirectory
    )
            throws IOException {
        writeSuccessfulTraceProject(temporaryDirectory);
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();

        try (URLClassLoader foreignClassLoader = new URLClassLoader(new java.net.URL[0], null)) {
            currentThread.setContextClassLoader(foreignClassLoader);

            final OftTraceResult result = new OftTraceService().traceProject(
                    OftTraceInputs.wholeProject(temporaryDirectory),
                    OftTraceProgress.NONE
            );
            final String renderedOutput = stripAnsi(result.output());

            Assertions.assertAll(
                    () -> assertThat(result.isSuccessful(), is(true)),
                    () -> assertThat(renderedOutput, Matchers.containsString("ok - 3 total")),
                    () -> assertThat(renderedOutput, Matchers.not(Matchers.containsString("ok - 0 total"))),
                    () -> assertThat(currentThread.getContextClassLoader(), is(foreignClassLoader))
            );
        } finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }

    // [itest->dsn~show-failing-trace-output-in-ide-output-window~1]
    @Test
    void testGivenDefectiveTraceInputWhenTracingThenItReturnsFailingPlainTextOutput(@TempDir final Path temporaryDirectory)
            throws IOException {
        writeFailingTraceProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(
                OftTraceInputs.wholeProject(temporaryDirectory),
                OftTraceProgress.NONE
        );
        final String renderedOutput = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(false)),
                () -> assertThat(
                        renderedOutput,
                        Matchers.startsWith(
                                "Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize()
                        )
                ),
                () -> assertThat(renderedOutput, Matchers.containsString("not ok")),
                () -> assertThat(renderedOutput, Matchers.containsString("req~trace_output_requirement~1")),
                () -> assertThat(result.output(), Matchers.containsString("\u001B["))
        );
    }

    // [itest->dsn~show-scanned-base-directory-in-trace-output-window~1]
    @Test
    void testGivenTraceInputWhenTracingThenItWritesTheScannedBaseDirectoryIntoTheTextOutput(
            @TempDir final Path temporaryDirectory
    )
            throws IOException {
        writeSuccessfulTraceProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(
                OftTraceInputs.wholeProject(temporaryDirectory),
                OftTraceProgress.NONE
        );
        final String renderedOutput = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(true)),
                () -> assertThat(
                        renderedOutput,
                        Matchers.startsWith(
                                "Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize()
                                        + System.lineSeparator() + System.lineSeparator()
                        )
                ),
                () -> assertThat(renderedOutput, Matchers.containsString("ok -"))
        );
    }

    // [itest->dsn~preserve-defect-count-for-unclean-trace-chain-in-output-window~1]
    @Test
    void testGivenUncleanTraceChainWhenTracingThenItReportsTheExpectedDefectCountInTheTextOutput(
            @TempDir final Path temporaryDirectory
    )
            throws IOException {
        writeUncleanTraceChainProject(temporaryDirectory);

        final OftTraceResult result = new OftTraceService().traceProject(
                OftTraceInputs.wholeProject(temporaryDirectory),
                OftTraceProgress.NONE
        );
        final String renderedOutput = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(false)),
                () -> assertThat(result.output(), Matchers.containsString("\u001B[")),
                () -> assertThat(renderedOutput, Matchers.containsString("not ok - 3 total, 3 defect")),
                () -> assertThat(renderedOutput, Matchers.containsString("dsn~chain_design~1")),
                () -> assertThat(renderedOutput, Matchers.containsString("feat~chain_feature~1")),
                () -> assertThat(renderedOutput, Matchers.containsString("req~chain_requirement~1"))
        );
    }

    @Test
    void testGivenRuntimeExceptionWhenTracingThenItReturnsAnErrorResult(@TempDir final Path temporaryDirectory) {
        final Oft oft = (Oft) Proxy.newProxyInstance(
                Oft.class.getClassLoader(),
                new Class<?>[]{Oft.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "importItems" -> throw new IllegalStateException("boom");
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "OftProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
        final OftTraceReportRenderer renderer = (trace, settings) -> {
            throw new AssertionError("renderer must not be called");
        };

        final OftTraceResult result = new OftTraceService(oft, renderer).traceProject(
                OftTraceInputs.wholeProject(temporaryDirectory),
                OftTraceProgress.NONE
        );

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(false)),
                () -> assertThat(result.statusMessage(), is("OpenFastTrace trace failed unexpectedly.")),
                () -> assertThat(result.output(), Matchers.containsString("OpenFastTrace trace failed for input path")),
                () -> assertThat(result.output(), Matchers.containsString("IllegalStateException: boom"))
        );
    }

    private String stripAnsi(final String output) {
        return ANSI_ESCAPE_SEQUENCE.matcher(output).replaceAll("");
    }

    // [itest->dsn~trace-selected-project-resources~1]
    // [itest->dsn~show-resolved-trace-inputs-in-trace-output-window~1]
    @Test
    void testGivenSelectedResourceInputsWhenTracingThenItListsConfiguredInputsAndIgnoresOutOfScopeArtifacts(
            @TempDir final Path temporaryDirectory
    ) throws IOException {
        writeSuccessfulTraceProject(temporaryDirectory);
        writeOutOfScopeDefect(temporaryDirectory);

        final Path docDirectory = temporaryDirectory.resolve("doc");
        final Path sourceDirectory = temporaryDirectory.resolve("src");
        final OftTraceResult result = new OftTraceService().traceProject(
                OftTraceInputs.selectedResources(java.util.List.of(docDirectory, sourceDirectory)),
                OftTraceProgress.NONE
        );
        final String renderedOutput = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat(result.isSuccessful(), is(true)),
                () -> assertThat(renderedOutput, Matchers.startsWith("Scanning configured trace inputs:")),
                () -> assertThat(renderedOutput, Matchers.containsString("- " + docDirectory.toAbsolutePath().normalize())),
                () -> assertThat(renderedOutput, Matchers.containsString("- " + sourceDirectory.toAbsolutePath().normalize())),
                () -> assertThat(renderedOutput, Matchers.not(Matchers.containsString("unwanted_requirement"))),
                () -> assertThat(renderedOutput, Matchers.containsString("ok - 3 total"))
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

    private void writeOutOfScopeDefect(final Path projectRoot) throws IOException {
        final Path examplesDirectory = Files.createDirectories(projectRoot.resolve("examples"));
        Files.writeString(
                examplesDirectory.resolve("ignored.md"),
                """
                ### Requirement
                `req~unwanted_requirement~1`

                Needs: impl
                """
        );
    }
}
