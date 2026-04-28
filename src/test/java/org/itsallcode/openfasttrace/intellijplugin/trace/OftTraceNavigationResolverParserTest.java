package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.testFramework.LightVirtualFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class OftTraceNavigationResolverParserTest {
    @Test
    void testGivenNamedSourceAndNeedsWhenParsingCoverageTagThenItReturnsAllFields()
            throws ReflectiveOperationException {
        final String line = coverageTag(" tst~my.name_1-target~12 -> dsn~target.name-2~34 >> req, dsn ");

        final Object tag = parseCoverageTag(line);

        assertThat(tag, notNullValue());
        assertThat(recordComponent(tag, "sourceArtifact"), is("tst"));
        assertThat(recordComponent(tag, "sourceName"), is("my.name_1-target"));
        assertThat(recordComponent(tag, "sourceRevision"), is(12));
        assertThat(recordComponent(tag, "targetId"), is("dsn~target.name-2~34"));
        assertThat(recordComponent(tag, "targetName"), is("target.name-2"));
        assertThat(recordComponent(tag, "hasNeedsArtifactTypes"), is(true));
    }

    @Test
    void testGivenUnnamedSourceWhenParsingCoverageTagThenItReturnsRevisionOnlySource()
            throws ReflectiveOperationException {
        final Object tag = parseCoverageTag(coverageTag("tst~~7->dsn~target~1"));

        assertThat(tag, notNullValue());
        assertThat(recordComponent(tag, "sourceArtifact"), is("tst"));
        assertThat(recordComponent(tag, "sourceName"), nullValue());
        assertThat(recordComponent(tag, "sourceRevision"), is(7));
    }

    @ValueSource(strings = {
            "1->dsn~target~1",
            "tst dsn~target~1",
            "tst->1",
            "tst->dsn~target~1 >> ",
            "tst->dsn~target~1 trailing",
            "tst~"
    })
    @ParameterizedTest
    void testGivenInvalidCoverageTagsWhenParsingThenItReturnsNull(final String tag) throws ReflectiveOperationException {
        assertThat(parseCoverageTag(coverageTag(tag)), nullValue());
    }

    @Test
    void testGivenDifferentLineSeparatorsWhenScanningTextThenOffsetsAdvanceCorrectly()
            throws ReflectiveOperationException {
        assertThat(invokeStatic("findLineEnd", String.class, int.class, "plain text", 0), is(10));
        assertThat(invokeStatic("skipLineSeparator", String.class, int.class, "line\r\nnext", 4), is(6));
        assertThat(invokeStatic("skipLineSeparator", String.class, int.class, "line", 4), is(4));
    }

    @Test
    void testGivenGeneratedAndNamedCoverageIdsWhenScanningLineThenItResolvesMatchingOffsets()
            throws ReflectiveOperationException {
        final LightVirtualFile file = new LightVirtualFile("Main.java", "");
        final String line = "// " + coverageTag("tst->dsn~first~1") + " " + coverageTag("tst~named~3->dsn~second~2");
        final int secondOpeningBracket = line.indexOf('[', line.indexOf(']') + 1);
        final int secondClosingBracket = line.indexOf(']', secondOpeningBracket + 1);
        final Object secondTag = parseCoverageTag(line, secondOpeningBracket, secondClosingBracket);
        final String secondId = (String) invokeStatic(
                "createCoverageTagSourceId",
                new Class<?>[]{com.intellij.openapi.vfs.VirtualFile.class, int.class, int.class, secondTag.getClass()},
                file,
                1,
                1,
                secondTag
        );

        final OftTraceNavigationTarget secondTarget = (OftTraceNavigationTarget) invokeStatic(
                "findCoverageTagTargetInLine",
                new Class<?>[]{com.intellij.openapi.vfs.VirtualFile.class, String.class, int.class, int.class, String.class},
                file,
                line,
                1,
                7,
                secondId
        );

        assertThat(secondTarget, notNullValue());
        assertThat(secondTarget.offset(), is(7 + line.indexOf("tst~named~3")));

        final Object needsTag = parseCoverageTag(coverageTag("tst->dsn~target~1 >> req"));
        final String needsId = (String) invokeStatic(
                "createCoverageTagSourceId",
                new Class<?>[]{com.intellij.openapi.vfs.VirtualFile.class, int.class, int.class, needsTag.getClass()},
                file,
                2,
                0,
                needsTag
        );

        assertThat(needsId, is("tst~target~0"));
    }

    @Test
    void testGivenMissingOrUnclosedCoverageTagWhenScanningLineThenItReturnsNull()
            throws ReflectiveOperationException {
        final LightVirtualFile file = new LightVirtualFile("Main.java", "");

        assertThat(
                invokeStatic(
                        "findCoverageTagTargetInLine",
                        new Class<?>[]{com.intellij.openapi.vfs.VirtualFile.class, String.class, int.class, int.class, String.class},
                        file,
                        "no coverage tag here",
                        1,
                        0,
                        "tst~target~0"
                ),
                nullValue()
        );
        assertThat(
                invokeStatic(
                        "findCoverageTagTargetInLine",
                        new Class<?>[]{com.intellij.openapi.vfs.VirtualFile.class, String.class, int.class, int.class, String.class},
                        file,
                        unclosedCoverageTag("tst->dsn~target~1"),
                        1,
                        0,
                        "tst~target~0"
                ),
                nullValue()
        );
    }

    @Test
    void testGivenInvalidTokenLikeInputsWhenParsingHelpersThenTheyFailGracefully()
            throws ReflectiveOperationException {
        assertThat(invokeStatic("parseName", String.class, int.class, int.class, "_name", 0, 5), nullValue());
        assertThat(invokeStatic("parseUnsignedInteger", String.class, int.class, int.class, "x1", 0, 2), nullValue());
        assertThat(invokeStatic("parseArtifactType", String.class, int.class, int.class, "1abc", 0, 4), nullValue());
        assertThat(
                invokeStatic("startsWith", String.class, int.class, String.class, "value", -1, "va"),
                is(false)
        );
        assertThat(
                invokeStatic("startsWith", String.class, int.class, String.class, "value", 4, "toolong"),
                is(false)
        );
    }

    private static Object parseCoverageTag(final String line) throws ReflectiveOperationException {
        return parseCoverageTag(line, 0, line.length() - 1);
    }

    private static String coverageTag(final String body) {
        return "[" + body + "]";
    }

    private static String unclosedCoverageTag(final String body) {
        return "[" + body;
    }

    private static Object parseCoverageTag(final String line, final int openingBracket, final int closingBracket)
            throws ReflectiveOperationException {
        return invokeStatic(
                "parseCoverageTag",
                new Class<?>[]{String.class, int.class, int.class},
                line,
                openingBracket,
                closingBracket
        );
    }

    private static Object recordComponent(final Object obj, final String componentName)
            throws ReflectiveOperationException {
        final Method accessor = obj.getClass().getDeclaredMethod(componentName);
        accessor.setAccessible(true);
        return accessor.invoke(obj);
    }

    private static Object invokeStatic(
            final String methodName,
            final Class<?> firstParameterType,
            final Class<?> secondParameterType,
            final Object firstArgument,
            final Object secondArgument
    ) throws ReflectiveOperationException {
        return invokeStatic(
                methodName,
                new Class<?>[]{firstParameterType, secondParameterType},
                firstArgument,
                secondArgument
        );
    }

    private static Object invokeStatic(
            final String methodName,
            final Class<?> firstParameterType,
            final Class<?> secondParameterType,
            final Class<?> thirdParameterType,
            final Object firstArgument,
            final Object secondArgument,
            final Object thirdArgument
    ) throws ReflectiveOperationException {
        return invokeStatic(
                methodName,
                new Class<?>[]{firstParameterType, secondParameterType, thirdParameterType},
                firstArgument,
                secondArgument,
                thirdArgument
        );
    }

    private static Object invokeStatic(
            final String methodName,
            final Class<?>[] parameterTypes,
            final Object... arguments
    ) throws ReflectiveOperationException {
        final Method method = OftTraceNavigationResolver.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }
}
