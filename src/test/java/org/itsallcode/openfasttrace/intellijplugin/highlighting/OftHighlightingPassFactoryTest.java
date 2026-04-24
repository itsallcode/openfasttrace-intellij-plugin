package org.itsallcode.openfasttrace.intellijplugin.highlighting;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.psi.PsiFileFactory;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

public class OftHighlightingPassFactoryTest extends AbstractOftPlatformTestCase {
    public void testGivenSpecificationFileWhenCreatingPassThenFactoryReturnsOftHighlightingPass()
            throws ExecutionException, InterruptedException {
        final TextEditorHighlightingPass pass = createHighlightingPassOnPooledThread("spec.md", "req~login~1");

        assertThat(pass, instanceOf(OftHighlightingPass.class));
    }

    public void testGivenUnsupportedFileWhenCreatingPassThenFactoryReturnsNull() {
        myFixture.configureByText("plain.txt", "not an oft file");

        final TextEditorHighlightingPass pass =
                new OftHighlightingPassFactory().createHighlightingPass(myFixture.getFile(), myFixture.getEditor());

        assertThat(pass, nullValue());
    }

    public void testGivenInMemoryFileWithoutVirtualFileWhenCreatingPassThenFactoryReturnsNull() {
        myFixture.configureByText("spec.md", "req~login~1");
        final var inMemoryFile = PsiFileFactory.getInstance(getProject()).createFileFromText(
                "InMemory.txt",
                PlainTextFileType.INSTANCE,
                "not an oft file"
        );

        final TextEditorHighlightingPass pass =
                new OftHighlightingPassFactory().createHighlightingPass(inMemoryFile, myFixture.getEditor());

        assertThat(pass, nullValue());
    }

    private TextEditorHighlightingPass createHighlightingPassOnPooledThread(final String fileName, final String text)
            throws ExecutionException, InterruptedException {
        myFixture.configureByText(fileName, text);
        return ApplicationManager.getApplication()
                .executeOnPooledThread(() ->
                        new OftHighlightingPassFactory().createHighlightingPass(myFixture.getFile(), myFixture.getEditor())
                )
                .get();
    }
}
