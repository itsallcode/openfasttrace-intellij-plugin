package org.itsallcode.openfasttrace.intellijplugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class OftIconResourcesTest {
    // [itest->dsn~packaged-plugin-logo-assets~1]
    // [itest->dsn~openfasttrace-run-configuration-icon~1]
    @ParameterizedTest
    @MethodSource("requiredSvgResources")
    void givenRequiredIconResourceWhenLoadingFromClasspathThenSvgHasExpectedSize(
            final String resourcePath,
            final String expectedSize
    ) throws Exception {
        final Element svg = loadSvg(resourcePath);

        assertSvgSize(svg, expectedSize);
    }

    // [itest->dsn~packaged-plugin-logo-assets~1]
    @Test
    void givenOptionalDarkPluginIconWhenLoadingFromClasspathThenItIsAbsentOrHasExpectedSize() throws Exception {
        final URL darkIcon = resource(OftIcons.PLUGIN_DARK_ICON_RESOURCE_PATH);

        if (darkIcon == null) {
            assertThat(darkIcon, nullValue());
        } else {
            assertSvgSize(loadSvg(OftIcons.PLUGIN_DARK_ICON_RESOURCE_PATH), "40");
        }
    }

    private static Stream<Arguments> requiredSvgResources() {
        return Stream.of(
                Arguments.of(OftIcons.PLUGIN_ICON_RESOURCE_PATH, "40"),
                Arguments.of(OftIcons.RUN_CONFIGURATION_ICON_RESOURCE_PATH, "16")
        );
    }

    private static Element loadSvg(final String resourcePath) throws Exception {
        final URL resource = resource(resourcePath);
        assertThat(resource, notNullValue());
        try (InputStream input = resource.openStream()) {
            final Document document = documentBuilderFactory().newDocumentBuilder().parse(input);
            return document.getDocumentElement();
        }
    }

    private static URL resource(final String resourcePath) {
        return OftIconResourcesTest.class.getClassLoader().getResource(resourcePath);
    }

    private static DocumentBuilderFactory documentBuilderFactory() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static void assertSvgSize(final Element svg, final String expectedSize) {
        Assertions.assertAll(
                () -> assertThat(svg.getTagName(), is("svg")),
                () -> assertThat(svg.getAttribute("width"), is(expectedSize)),
                () -> assertThat(svg.getAttribute("height"), is(expectedSize)),
                () -> assertThat(svg.getAttribute("viewBox"), is("0 0 " + expectedSize + " " + expectedSize))
        );
    }
}
