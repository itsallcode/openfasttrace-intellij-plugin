package org.itsallcode.openfasttrace.intellijplugin.help;

import com.intellij.openapi.project.Project;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class OpenFastTraceUserGuideTest extends AbstractOftPlatformTestCase {
    public void testOpensUrlInHtmlEditor() {
        final AtomicReference<EditorCall> call = new AtomicReference<>();

        OpenFastTraceUserGuide.open(
                getProject(),
                OpenFastTraceUserGuide.TITLE,
                OpenFastTraceUserGuide.URL,
                (project, title, url, timeoutHtml) -> call.set(new EditorCall(project, title, url, timeoutHtml))
        );

        final EditorCall actual = call.get();
        assertThat(actual.project(), is(getProject()));
        assertThat(actual.title(), is(OpenFastTraceUserGuide.TITLE));
        assertThat(actual.url(), is(OpenFastTraceUserGuide.URL));
        assertThat(actual.timeoutHtml(), is(nullValue()));
    }

    public void testGivenProjectWhenOpeningUserGuideWithDefaultOpenerThenTheTestFixtureThrowsNullPointerException() {
        final NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> OpenFastTraceUserGuide.open(getProject(), OpenFastTraceUserGuide.TITLE, OpenFastTraceUserGuide.URL)
        );

        assertThat(exception.getMessage(), is(nullValue()));
    }

    private record EditorCall(Project project, String title, String url, String timeoutHtml) {
    }
}
