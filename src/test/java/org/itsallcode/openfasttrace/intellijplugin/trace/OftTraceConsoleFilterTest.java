package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.filters.Filter;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class OftTraceConsoleFilterTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~open-specification-item-from-trace-output-window~1]
    public void testGivenDeclaredSpecificationItemInTraceLineWhenFilteringThenItCreatesHyperlinkResult() {
        myFixture.addFileToProject("doc/spec.md", """
                req~trace_console_target~1
                Needs: dsn
                """);
        final String line = "not ok req~trace_console_target~1" + System.lineSeparator();

        final Filter.Result result = new OftTraceConsoleFilter(getProject()).applyFilter(line, line.length());

        assertThat(result, notNullValue());
        assertThat(result.getResultItems(), hasSize(1));
        assertThat(
                line.substring(
                        result.getResultItems().getFirst().getHighlightStartOffset(),
                        result.getResultItems().getFirst().getHighlightEndOffset()
                ),
                is("req~trace_console_target~1")
        );
        assertThat(result.getResultItems().getFirst().getHyperlinkInfo(), notNullValue());
    }

    // [itest->dsn~open-specification-item-from-trace-output-window~1]
    public void testGivenUnknownSpecificationItemInTraceLineWhenFilteringThenItReturnsNoHyperlink() {
        final String line = "not ok req~trace_console_missing~1" + System.lineSeparator();

        final Filter.Result result = new OftTraceConsoleFilter(getProject()).applyFilter(line, line.length());

        assertThat(result, nullValue());
    }
}
