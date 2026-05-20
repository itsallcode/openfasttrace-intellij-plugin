package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftArtifactFilterTest {
    private static final Pattern ANSI_ESCAPE_SEQUENCE = Pattern.compile("\\x1B\\[[0-?]*[ -/]*[@-~]");

    @Test
    void testGivenReqTracingToMissingDsnWhenFilteringForReqOnlyThenItShouldNotComplainAboutMissingDsn(@TempDir Path tempDir) throws IOException {
        Path specFile = tempDir.resolve("spec.md");
        Files.writeString(specFile, """
                ### Requirement
                `req~test_req~1`
                
                Needs: dsn
                """);

        OftTraceInputs inputs = OftTraceInputs.selectedResources(
                List.of(specFile),
                List.of("req"), // Filter for req only
                List.of()
        );

        OftTraceResult result = new OftTraceService().traceProject(inputs, OftTraceProgress.NONE);
        String output = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat("Trace should be successful. Output:\n" + output, result.isSuccessful(), is(true)),
                () -> assertThat("Output should show 1 total ok item. Output:\n" + output, output, Matchers.containsString("ok - 1 total")),
                () -> assertThat("Output should not contain missing dsn error (-dsn). Output:\n" + output, output, Matchers.not(Matchers.containsString("(-dsn)")))
        );
    }

    @Test
    void testGivenReqTracingToExistingDsnWhenFilteringForReqOnlyThenItShouldNotCheckDsnCoverage(@TempDir Path tempDir) throws IOException {
        Path specFile = tempDir.resolve("spec.md");
        Files.writeString(specFile, """
                ### Requirement
                `req~test_req~1`
                
                Needs: dsn
                
                ### Design
                `dsn~test_dsn~1`
                
                Covers:
                - `req~test_req~1`
                
                Needs: impl
                """);

        OftTraceInputs inputs = OftTraceInputs.selectedResources(
                List.of(specFile),
                List.of("req"), // Filter for req only
                List.of()
        );

        OftTraceResult result = new OftTraceService().traceProject(inputs, OftTraceProgress.NONE);
        String output = stripAnsi(result.output());

        Assertions.assertAll(
                () -> assertThat("Trace should be successful. Output:\n" + output, result.isSuccessful(), is(true)),
                () -> assertThat("Output should show 1 total ok item. Output:\n" + output, output, Matchers.containsString("ok - 1 total")),
                () -> assertThat("Output should not show dsn details. Output:\n" + output, output, Matchers.not(Matchers.containsString("dsn~test_dsn~1")))
        );
    }

    private String stripAnsi(String output) {
        return ANSI_ESCAPE_SEQUENCE.matcher(output).replaceAll("");
    }
}
