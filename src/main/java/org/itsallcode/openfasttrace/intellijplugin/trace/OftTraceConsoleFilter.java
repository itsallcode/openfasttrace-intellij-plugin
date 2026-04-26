package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftIndexedSpecification;
import org.itsallcode.openfasttrace.intellijplugin.indexing.OftSpecificationIndex;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftTextSpan;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds declaration hyperlinks for OFT specification item IDs shown inside the trace console output.
 */
final class OftTraceConsoleFilter implements Filter {
    private final Project project;

    // [impl->dsn~open-specification-item-from-trace-output-window~1]
    OftTraceConsoleFilter(final Project project) {
        this.project = project;
    }

    @Override
    public @Nullable Result applyFilter(final String line, final int entireLength) {
        final int lineStartOffset = entireLength - line.length();
        final List<ResultItem> resultItems = findConsoleResultItems(line, lineStartOffset);
        return resultItems.isEmpty() ? null : new Result(resultItems);
    }

    private List<ResultItem> findConsoleResultItems(final String line, final int lineStartOffset) {
        final List<ResultItem> resultItems = new ArrayList<>();
        for (final OftSpecificationItemMatch match : OftSyntaxCore.findSpecificationItems(line)) {
            final HyperlinkInfo hyperlink = createHyperlink(match.item().id());
            if (hyperlink != null) {
                resultItems.add(toResultItem(lineStartOffset, match.span(), hyperlink));
            }
        }
        return List.copyOf(resultItems);
    }

    private ResultItem toResultItem(
            final int lineStartOffset,
            final OftTextSpan span,
            final HyperlinkInfo hyperlink
    ) {
        return new ResultItem(
                lineStartOffset + span.startOffset(),
                lineStartOffset + span.endOffset(),
                hyperlink
        );
    }

    private @Nullable HyperlinkInfo createHyperlink(final String specificationId) {
        final OftTraceDeclaration declaration = findDeclaration(specificationId);
        if (declaration == null) {
            return null;
        }
        return projectToNavigate -> new OpenFileDescriptor(
                projectToNavigate,
                declaration.file(),
                declaration.specification().offset()
        ).navigate(true);
    }

    private @Nullable OftTraceDeclaration findDeclaration(final String specificationId) {
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        final OftTraceDeclaration[] declaration = new OftTraceDeclaration[1];
        FileBasedIndex.getInstance().processValues(
                OftSpecificationIndex.SPECIFICATION_ID,
                specificationId,
                null,
                (file, values) -> {
                    if (!values.isEmpty()) {
                        declaration[0] = new OftTraceDeclaration(file, values.getFirst());
                        return false;
                    }
                    return true;
                },
                scope
        );
        return declaration[0];
    }

    private record OftTraceDeclaration(
            com.intellij.openapi.vfs.VirtualFile file,
            OftIndexedSpecification specification
    ) {
    }
}
