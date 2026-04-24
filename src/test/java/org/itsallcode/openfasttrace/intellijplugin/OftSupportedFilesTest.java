package org.itsallcode.openfasttrace.intellijplugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OftSupportedFilesTest {
    @ParameterizedTest
    @ValueSource(strings = {"requirements.MD", "architecture.rst"})
    void givenValidSpecificationFileNameWhenCheckingThenItReturnsTrue(final String fileName) {
        assertThat(OftSupportedFiles.isSpecificationFileName(fileName), is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"notes.txt", "README"})
    void givenInvalidSpecificationFileNameWhenCheckingThenItReturnsFalse(final String fileName) {
        assertThat(OftSupportedFiles.isSpecificationFileName(fileName), is(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Main.JAVA", "diagram.puml"})
    void givenValidCoverageTagFileNameWhenCheckingThenItReturnsTrue(final String fileName) {
        assertThat(OftSupportedFiles.isCoverageTagFileName(fileName), is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"requirements.md", "README"})
    void givenInvalidCoverageTagFileNameWhenCheckingThenItReturnsFalse(final String fileName) {
        assertThat(OftSupportedFiles.isCoverageTagFileName(fileName), is(false));
    }

    @Test
    void givenNullVirtualFilesWhenCheckingThenItReturnsFalse() {
        assertThat(
                java.util.List.of(OftSupportedFiles.isSpecificationFile(null), OftSupportedFiles.isCoverageTagFile(null)),
                is(java.util.List.of(false, false))
        );
    }
}
