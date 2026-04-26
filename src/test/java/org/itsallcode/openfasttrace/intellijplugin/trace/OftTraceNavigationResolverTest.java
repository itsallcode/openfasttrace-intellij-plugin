package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiFile;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OftTraceNavigationResolverTest extends AbstractOftPlatformTestCase {
    public void testGivenUnsavedCoverageTagDocumentWhenResolvingGeneratedTraceItemThenItUsesLiveDocumentText() {
        final PsiFile file = myFixture.addFileToProject("src/Main.java", """
                // [tst""" + "->dsn~old_target~1]\n" + """
                class Main {
                }
                """);
        myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
        final Document document = myFixture.getEditor().getDocument();
        final String updatedCoveredId = "dsn~updated_target~1";

        ApplicationManager.getApplication().invokeAndWait(() -> WriteCommandAction.runWriteCommandAction(
                getProject(),
                () -> document.replaceString(
                        document.getText().indexOf("dsn~old_target~1"),
                        document.getText().indexOf("dsn~old_target~1") + "dsn~old_target~1".length(),
                        updatedCoveredId
                )
        ));

        assertThat(FileDocumentManager.getInstance().isDocumentUnsaved(document), is(true));

        final String generatedTraceItemId = generatedCoverageItemId(
                file.getVirtualFile().getPath(),
                1,
                0,
                "tst",
                updatedCoveredId
        );
        final OftTraceNavigationTarget target = new OftTraceNavigationResolver(getProject())
                .resolve(generatedTraceItemId)
                .orElse(null);

        assertThat(target, notNullValue());
        assertThat(target.file(), is(file.getVirtualFile()));
        assertThat(target.offset(), is(document.getText().indexOf("tst")));
    }

    private String generatedCoverageItemId(
            final String filePath,
            final int lineNumber,
            final int lineMatchCount,
            final String sourceArtifactType,
            final String coveredId
    ) {
        return sourceArtifactType + "~" + coveredId.split("~")[1] + "-" + crc32(filePath + lineNumber + lineMatchCount + coveredId) + "~0";
    }

    private long crc32(final String value) {
        final CRC32 checksum = new CRC32();
        checksum.update(value.getBytes(StandardCharsets.UTF_8));
        return checksum.getValue();
    }
}
