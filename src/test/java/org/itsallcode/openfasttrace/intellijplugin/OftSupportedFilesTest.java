package org.itsallcode.openfasttrace.intellijplugin;

import org.itsallcode.openfasttrace.importer.tag.TagImporterFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
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
    @ValueSource(strings = {
            "source.ads", "source.adb",
            "build.bat",
            "source.c", "source.C", "source.cc", "source.cpp", "source.c++", "source.h", "source.H", "source.h++",
            "source.hh", "source.hpp",
            "source.c#", "source.cs",
            "settings.cfg", "settings.conf", "settings.ini",
            "scenario.feature",
            "source.go", "source.groovy",
            "data.json", "layout.fxml", "project.xml", "page.htm", "page.html", "page.xhtml",
            "pipeline.yaml", "pipeline.yml",
            "Main.JAVA",
            "source.clj", "source.kt", "source.scala",
            "source.js", "source.mjs", "source.cjs", "template.ejs",
            "source.ts", "source.lua", "source.m", "source.mm",
            "source.php", "source.pl", "source.pm", "source.py",
            "suite.robot",
            "diagram.pu", "diagram.puml", "diagram.plantuml",
            "source.r", "source.rs",
            "source.sh", "source.bash", "source.zsh",
            "source.sv", "source.v", "defs.inc",
            "source.swift",
            "settings.toml",
            "main.tf", "variables.tfvars",
            "query.sql", "package.pls"
    })
    void givenValidCoverageTagFileNameWhenCheckingThenItReturnsTrue(final String fileName) {
        assertThat(OftSupportedFiles.isCoverageTagFileName(fileName), is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"requirements.md", "README"})
    void givenInvalidCoverageTagFileNameWhenCheckingThenItReturnsFalse(final String fileName) {
        assertThat(OftSupportedFiles.isCoverageTagFileName(fileName), is(false));
    }

    @Test
    void givenUpstreamTagImporterDefaultExtensionsWhenCheckingCoverageTagFileNameThenAllAreSupported()
            throws ReflectiveOperationException {
        final List<String> unsupportedExtensions = tagImporterDefaultExtensions().stream()
                .filter(extension -> !OftSupportedFiles.isCoverageTagFileName("coverage-tag." + extension))
                .toList();

        assertThat(unsupportedExtensions, is(empty()));
    }

    @Test
    void givenNullVirtualFilesWhenCheckingThenItReturnsFalse() {
        assertThat(
                java.util.List.of(OftSupportedFiles.isSpecificationFile(null), OftSupportedFiles.isCoverageTagFile(null)),
                is(java.util.List.of(false, false))
        );
    }

    @SuppressWarnings("unchecked")
    private static List<String> tagImporterDefaultExtensions() throws ReflectiveOperationException {
        final Field field = TagImporterFactory.class.getDeclaredField("SUPPORTED_DEFAULT_EXTENSIONS");
        field.setAccessible(true);
        return (List<String>) field.get(null);
    }
}
