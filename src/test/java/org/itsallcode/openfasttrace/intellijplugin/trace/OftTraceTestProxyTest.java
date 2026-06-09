package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.testFramework.EdtTestUtil;
import org.hamcrest.Matchers;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class OftTraceTestProxyTest extends AbstractOftPlatformTestCase {
    // [itest->dsn~navigate-from-test-runner-specification-items~1]
    public void testGivenSpecificationItemNodeWhenNavigatingThenItOpensTheSpecificationDeclaration() {
        myFixture.addFileToProject("doc/spec.md", """
                req~runner_navigation_target~1
                Needs: impl
                """);
        final OftTraceTestProxy proxy = new OftTraceTestProxy(
                getProject(),
                "req~runner_navigation_target~1 (uncovered)",
                false,
                "req~runner_navigation_target~1"
        );

        assertThat(proxy.canNavigate(), is(true));
        EdtTestUtil.runInEdtAndWait(() -> proxy.navigate(false));

        assertThat(selectedEditorFileName(), is("spec.md"));
    }

    // [itest->dsn~navigate-from-test-runner-trace-links~1]
    public void testGivenTraceLinkNodeForGeneratedCoverageTagItemWhenNavigatingThenItOpensTheCoverageTag() {
        myFixture.addFileToProject("doc/design.md", """
                dsn~runner_navigation_target~1
                Needs: impl
                """);
        final var coverageTagFile = myFixture.addFileToProject(
                "src/Main.java",
                """
                        // [impl""" + "->dsn~runner_navigation_target~1]\n" + """
                        class Main {
                        }
                        """
        );
        final String generatedCoverageItemId = generatedCoverageItemId(
                coverageTagFile.getVirtualFile().getPath(),
                1,
                0,
                "impl",
                "dsn~runner_navigation_target~1"
        );
        final OftTraceTestProxy proxy = new OftTraceTestProxy(
                getProject(),
                "<- " + generatedCoverageItemId + " (covered)",
                false,
                generatedCoverageItemId
        );

        assertThat(proxy.canNavigate(), is(true));
        EdtTestUtil.runInEdtAndWait(() -> proxy.navigate(false));

        assertThat(selectedEditorFileName(), is("Main.java"));
    }

    public void testGivenNodeWithoutNavigationTargetWhenCheckingNavigationThenItCannotNavigate() {
        final OftTraceTestProxy proxy = new OftTraceTestProxy(getProject(), "doc/spec.md", true, null);

        assertThat(proxy.canNavigate(), is(false));
        assertThat(proxy.canNavigateToSource(), is(false));
        EdtTestUtil.runInEdtAndWait(() -> proxy.navigate(false));
    }

    private String generatedCoverageItemId(
            final String filePath,
            final int lineNumber,
            final int lineMatchCount,
            final String sourceArtifactType,
            final String coveredId
    ) {
        return sourceArtifactType + "~" + coveredId.split("~")[1] + "-"
                + crc32(filePath + lineNumber + lineMatchCount + coveredId) + "~0";
    }

    private long crc32(final String value) {
        final CRC32 checksum = new CRC32();
        checksum.update(value.getBytes(StandardCharsets.UTF_8));
        return checksum.getValue();
    }

    private String selectedEditorFileName() {
        final TextEditor selectedEditor = (TextEditor) FileEditorManager.getInstance(getProject()).getSelectedEditor();
        assertThat(selectedEditor, notNullValue());
        assertThat(selectedEditor.getFile(), Matchers.not(nullValue()));
        return selectedEditor.getFile().getName();
    }
}
