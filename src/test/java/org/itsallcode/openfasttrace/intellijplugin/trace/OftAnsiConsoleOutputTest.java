package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.ui.ConsoleViewContentType;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class OftAnsiConsoleOutputTest extends AbstractOftPlatformTestCase {
    public void testGivenPlainTextWhenDecodingThenItKeepsNormalConsoleOutput() {
        final List<OftAnsiConsoleOutput.Chunk> chunks = new OftAnsiConsoleOutput().decode("plain text");

        assertThat(chunks, hasSize(1));
        assertThat(chunks.getFirst().text(), is("plain text"));
        assertThat(chunks.getFirst().contentType(), is(ConsoleViewContentType.NORMAL_OUTPUT));
    }

    public void testGivenAnsiColoredTextWhenDecodingThenItSplitsColoredAndPlainChunks() {
        final List<OftAnsiConsoleOutput.Chunk> chunks = new OftAnsiConsoleOutput()
                .decode("\u001B[31mnot ok\u001B[0m plain");

        assertThat(chunks, hasSize(2));
        assertThat(chunks.get(0).text(), is("not ok"));
        assertThat(chunks.get(0).contentType(), Matchers.not(Matchers.sameInstance(ConsoleViewContentType.NORMAL_OUTPUT)));
        assertThat(chunks.get(1).text(), is(" plain"));
        assertThat(chunks.get(1).contentType(), is(ConsoleViewContentType.NORMAL_OUTPUT));
    }
}
