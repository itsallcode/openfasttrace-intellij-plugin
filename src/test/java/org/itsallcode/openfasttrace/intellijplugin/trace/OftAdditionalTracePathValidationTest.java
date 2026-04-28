package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class OftAdditionalTracePathValidationTest {
    @Test
    void testGivenExistingRelativePathsWhenValidatingThenItReturnsNoMessages(@TempDir final Path temporaryDirectory)
            throws IOException {
        Files.createDirectories(temporaryDirectory.resolve("doc"));
        Files.writeString(temporaryDirectory.resolve("trace.conf"), "value");

        final OftAdditionalTracePathValidation validation =
                OftAdditionalTracePathValidation.validate(temporaryDirectory, "doc/\ntrace.conf");

        assertThat(
                validation.resolvedRelativeToText(),
                is("Resolved relative to: " + temporaryDirectory.toAbsolutePath().normalize())
        );
        assertThat(validation.messages(), is(empty()));
    }

    @Test
    void testGivenInvalidLinesWhenValidatingThenItReturnsPerLineMessages(@TempDir final Path temporaryDirectory) {
        final OftAdditionalTracePathValidation validation =
                OftAdditionalTracePathValidation.validate(temporaryDirectory, "missing\n/absolute/path");

        assertThat(
                validation.messages(),
                contains(
                        "Line 1: 'missing' not found",
                        "Line 2: '/absolute/path' must be project-relative"
                )
        );
    }
}
