package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class OftTraceBackgroundRunnerTest {
    @Test
    void testGivenTraceTaskWhenRunningThenItUpdatesProgressAndPresentsSuccessfulResult(@TempDir final Path temporaryDirectory)
            throws Exception {
        writeSuccessfulTraceProject(temporaryDirectory);
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final OftTraceBackgroundRunner runner = new OftTraceBackgroundRunner(
                new OftTraceService(),
                (project, contentTitle, result) -> presenterCall.set(new PresenterCall(project, contentTitle, result))
        );
        final Task.Backgroundable task = newTraceTask(runner, OftTraceInputs.wholeProject(temporaryDirectory), "Trace");
        final List<String> progressEvents = new ArrayList<>();
        final ProgressIndicator indicator = progressIndicator(progressEvents);

        task.run(indicator);
        invokeTaskLifecycle(task, "onSuccess");

        Assertions.assertAll(
                () -> assertThat(progressEvents, Matchers.contains(
                        "indeterminate:false",
                        "text2:" + temporaryDirectory,
                        "text:Importing OpenFastTrace items...",
                        "fraction:0.15",
                        "checkCanceled",
                        "text:Linking OpenFastTrace items...",
                        "fraction:0.4",
                        "checkCanceled",
                        "text:Tracing OpenFastTrace items...",
                        "fraction:0.65",
                        "checkCanceled",
                        "text:Rendering OpenFastTrace report...",
                        "fraction:0.9",
                        "checkCanceled",
                        "text:Finished OpenFastTrace trace.",
                        "fraction:1.0"
                )),
                () -> assertThat(presenterCall.get(), Matchers.notNullValue()),
                () -> assertThat(presenterCall.get().project(), nullValue()),
                () -> assertThat(presenterCall.get().contentTitle(), is("Trace")),
                () -> assertThat(presenterCall.get().result().isSuccessful(), is(true)),
                () -> assertThat(
                        presenterCall.get().result().output(),
                        Matchers.containsString("Scanning base directory: " + temporaryDirectory.toAbsolutePath().normalize())
                )
        );
    }

    @Test
    void testGivenTraceTaskWhenCancelledThenItPresentsCancelledResult() throws Exception {
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final Task.Backgroundable task = newTraceTask(
                new OftTraceBackgroundRunner(new OftTraceService(), capturePresenter(presenterCall)),
                OftTraceInputs.wholeProject(Path.of(".")),
                "Trace"
        );

        invokeTaskLifecycle(task, "onCancel");

        Assertions.assertAll(
                () -> assertThat(presenterCall.get().contentTitle(), is("Trace")),
                () -> assertThat(presenterCall.get().result().statusMessage(), is("OpenFastTrace trace was cancelled.")),
                () -> assertThat(presenterCall.get().result().output(), is("OpenFastTrace trace was cancelled.")),
                () -> assertThat(presenterCall.get().result().requiresAttention(), is(true))
        );
    }

    @Test
    void testGivenProcessCanceledExceptionWhenHandlingThrowableThenItPresentsCancelledResult() throws Exception {
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final Task.Backgroundable task = newTraceTask(
                new OftTraceBackgroundRunner(new OftTraceService(), capturePresenter(presenterCall)),
                OftTraceInputs.wholeProject(Path.of(".")),
                "Trace"
        );

        invokeTaskLifecycle(task, "onThrowable", new ProcessCanceledException());

        Assertions.assertAll(
                () -> assertThat(presenterCall.get().result().statusMessage(), is("OpenFastTrace trace was cancelled.")),
                () -> assertThat(presenterCall.get().result().output(), is("OpenFastTrace trace was cancelled."))
        );
    }

    @Test
    void testGivenUnexpectedThrowableWhenHandlingThrowableThenItPresentsFormattedError() throws Exception {
        final AtomicReference<PresenterCall> presenterCall = new AtomicReference<>();
        final Task.Backgroundable task = newTraceTask(
                new OftTraceBackgroundRunner(new OftTraceService(), capturePresenter(presenterCall)),
                OftTraceInputs.wholeProject(Path.of(".")),
                "Trace"
        );

        invokeTaskLifecycle(task, "onThrowable", new IllegalStateException("boom"));

        Assertions.assertAll(
                () -> assertThat(presenterCall.get().result().statusMessage(), is("OpenFastTrace trace failed unexpectedly.")),
                () -> assertThat(
                        presenterCall.get().result().output(),
                        Matchers.containsString("OpenFastTrace trace failed unexpectedly.")
                ),
                () -> assertThat(presenterCall.get().result().output(), Matchers.containsString("IllegalStateException: boom"))
        );
    }

    private static OftTraceOutputPresenter capturePresenter(final AtomicReference<PresenterCall> presenterCall) {
        return (project, contentTitle, result) -> presenterCall.set(new PresenterCall(project, contentTitle, result));
    }

    private static Task.Backgroundable newTraceTask(
            final OftTraceBackgroundRunner runner,
            final OftTraceInputs inputs,
            final String contentTitle
    ) throws Exception {
        final Constructor<?> constructor = Class.forName(
                "org.itsallcode.openfasttrace.intellijplugin.trace.OftTraceBackgroundRunner$TraceTask"
        ).getDeclaredConstructor(OftTraceBackgroundRunner.class, Project.class, OftTraceInputs.class, String.class);
        constructor.setAccessible(true);
        return (Task.Backgroundable) constructor.newInstance(runner, null, inputs, contentTitle);
    }

    private static void invokeTaskLifecycle(final Task.Backgroundable task, final String methodName, final Object... arguments)
            throws Exception {
        Method method = null;
        for (final Method candidate : task.getClass().getDeclaredMethods()) {
            if (!candidate.getName().equals(methodName) || candidate.getParameterCount() != arguments.length) {
                continue;
            }
            boolean parametersMatch = true;
            final Class<?>[] parameterTypes = candidate.getParameterTypes();
            for (int index = 0; index < arguments.length; index++) {
                if (arguments[index] != null && !parameterTypes[index].isAssignableFrom(arguments[index].getClass())) {
                    parametersMatch = false;
                    break;
                }
            }
            if (parametersMatch) {
                method = candidate;
                break;
            }
        }
        if (method == null) {
            throw new NoSuchMethodException(methodName);
        }
        method.setAccessible(true);
        method.invoke(task, arguments);
    }

    private static ProgressIndicator progressIndicator(final List<String> progressEvents) {
        return (ProgressIndicator) Proxy.newProxyInstance(
                ProgressIndicator.class.getClassLoader(),
                new Class<?>[]{ProgressIndicator.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setIndeterminate" -> progressEvents.add("indeterminate:" + args[0]);
                        case "setText2" -> progressEvents.add("text2:" + args[0]);
                        case "setText" -> progressEvents.add("text:" + args[0]);
                        case "setFraction" -> progressEvents.add("fraction:" + args[0]);
                        case "checkCanceled" -> progressEvents.add("checkCanceled");
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "toString" -> {
                            return "ProgressIndicatorProxy";
                        }
                        default -> {
                            return primitiveDefaultValue(method.getReturnType());
                        }
                    }
                    return null;
                }
        );
    }

    private static Object primitiveDefaultValue(final Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == double.class) {
            return 0D;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == char.class) {
            return (char) 0;
        }
        return null;
    }

    private static void writeSuccessfulTraceProject(final Path projectRoot) throws IOException {
        final Path docDirectory = Files.createDirectories(projectRoot.resolve("doc"));
        Files.writeString(
                docDirectory.resolve("trace.md"),
                """
                ### Feature
                `feat~trace_output_feature~1`

                Needs: req

                ### Requirement
                `req~trace_output_requirement~1`

                Covers:
                - `feat~trace_output_feature~1`

                Needs: impl
                """
        );
        final Path sourceDirectory = Files.createDirectories(projectRoot.resolve("src"));
        Files.writeString(
                sourceDirectory.resolve("Main.java"),
                "// [impl" + "->req~trace_output_requirement~1]" + System.lineSeparator()
                        + "class Main {" + System.lineSeparator()
                        + "}" + System.lineSeparator()
        );
    }

    private record PresenterCall(Project project, String contentTitle, OftTraceResult result) {
    }
}
