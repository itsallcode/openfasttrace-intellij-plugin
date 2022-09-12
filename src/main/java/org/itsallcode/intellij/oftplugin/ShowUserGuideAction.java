package org.itsallcode.intellij.oftplugin;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Action to display the OFT user guide.
 */
public class ShowUserGuideAction extends AnAction {
    public static final String USER_GUIDE_URL =
            "https://github.com/itsallcode/openfasttrace/blob/develop/doc/user_guide.md";

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        BrowserUtil.browse(USER_GUIDE_URL);
    }
}