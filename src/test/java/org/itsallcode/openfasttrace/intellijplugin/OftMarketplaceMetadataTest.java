package org.itsallcode.openfasttrace.intellijplugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class OftMarketplaceMetadataTest {
    private static final String PROJECT_URL = "https://github.com/itsallcode/openfasttrace-intellij-plugin";
    private static final String FIRST_DESCRIPTION_SENTENCE =
            "Author requirements directly in your project and trace them all the way down to your implementation "
                    + "and tests without leaving your IDE.";

    // [itest->dsn~marketplace-plugin-metadata~1]
    @Test
    void givenMarketplaceDescriptionWhenReadingThenItContainsConfirmedMetadata() throws Exception {
        final String description = Files.readString(Path.of("doc/marketplace/description.html"));

        Assertions.assertAll(
                () -> assertThat(description, containsString(FIRST_DESCRIPTION_SENTENCE)),
                () -> assertThat(description, containsString("developers")),
                () -> assertThat(description, containsString("requirement engineers")),
                () -> assertThat(description, containsString("quality engineers")),
                () -> assertThat(description, containsString("technical writers")),
                () -> assertThat(description, containsString("broken requirement chains"))
        );
    }

    // [itest->dsn~marketplace-plugin-metadata~1]
    @Test
    void givenActiveReleaseNotesWhenReadingThenItContainsMarketplaceMetadataEntry() throws Exception {
        final String version = projectVersion();
        final String bundledOpenFastTraceVersion = bundledOpenFastTraceVersion();
        final String releaseNotes = Files.readString(Path.of("doc/changes/changes_" + version + ".md"));

        Assertions.assertAll(
                () -> assertThat(releaseNotes, containsString("# OpenFastTrace IntelliJ Plugin " + version)),
                () -> assertThat(releaseNotes, containsString("#51: Complete Marketplace-facing plugin metadata")),
                () -> assertThat(releaseNotes, containsString("clearer overview")),
                () -> assertThat(releaseNotes, containsString("installing or updating")),
                () -> assertThat(releaseNotes, containsString("## Bundled OpenFastTrace")),
                () -> assertThat(releaseNotes, containsString("OpenFastTrace " + bundledOpenFastTraceVersion))
        );
    }

    // [itest->dsn~marketplace-plugin-metadata~1]
    @Test
    void givenSourcePluginDescriptorWhenReadingThenItContainsProjectWebsiteUrl() throws Exception {
        final Document document = loadXml(Files.readString(Path.of("src/main/resources/META-INF/plugin.xml")));

        assertThat(document.getDocumentElement().getAttribute("url"), is(PROJECT_URL));
    }

    private static String projectVersion() throws Exception {
        final Properties properties = new Properties();
        try (var input = Files.newInputStream(Path.of("gradle.properties"))) {
            properties.load(input);
        }
        return properties.getProperty("version");
    }

    private static String bundledOpenFastTraceVersion() throws Exception {
        final String prefix = "org.itsallcode.openfasttrace:openfasttrace:";
        try (var lines = Files.lines(Path.of("gradle.lockfile"))) {
            return lines
                    .filter(line -> line.startsWith(prefix))
                    .findFirst()
                    .map(line -> line.substring(prefix.length(), line.indexOf('=')))
                    .orElseThrow();
        }
    }

    private static Document loadXml(final String xml) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
