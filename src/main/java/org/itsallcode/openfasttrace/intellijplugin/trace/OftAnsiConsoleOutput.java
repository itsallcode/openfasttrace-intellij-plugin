package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;

import java.util.ArrayList;
import java.util.List;

final class OftAnsiConsoleOutput {
    private final AnsiEscapeDecoder ansiEscapeDecoder = new AnsiEscapeDecoder();

    void print(final ConsoleView console, final String text) {
        for (final Chunk chunk : decode(text)) {
            console.print(chunk.text(), chunk.contentType());
        }
    }

    List<Chunk> decode(final String text) {
        final List<Chunk> chunks = new ArrayList<>();
        ansiEscapeDecoder.escapeText(
                text,
                ProcessOutputTypes.STDOUT,
                (chunkText, attributes) ->
                        chunks.add(new Chunk(chunkText, ConsoleViewContentType.getConsoleViewType(attributes)))
        );
        return chunks;
    }

    record Chunk(String text, ConsoleViewContentType contentType) {
    }
}
