package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;

import java.io.Serial;
import java.util.Optional;

final class OftTraceTestProxy extends SMTestProxy {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Project project;
    private final String navigationId;
    private final transient OftTraceNavigationResolver navigationResolver;

    OftTraceTestProxy(final Project project, final String name, final boolean suite, final String navigationId) {
        super(name, suite, null, true);
        this.project = project;
        this.navigationId = navigationId;
        this.navigationResolver = new OftTraceNavigationResolver(project);
    }

    @Override
    public void navigate(final boolean requestFocus) {
        resolveNavigationTarget()
                .ifPresent(target -> new OpenFileDescriptor(project, target.file(), target.offset())
                        .navigate(requestFocus));
    }

    @Override
    public boolean canNavigate() {
        return resolveNavigationTarget().isPresent();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    private Optional<OftTraceNavigationTarget> resolveNavigationTarget() {
        if (navigationId == null || navigationResolver == null || project == null) {
            return Optional.empty();
        }
        return navigationResolver.resolve(navigationId);
    }
}
