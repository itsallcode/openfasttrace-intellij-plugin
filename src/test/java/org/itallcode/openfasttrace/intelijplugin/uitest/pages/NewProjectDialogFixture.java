package org.itallcode.openfasttrace.intelijplugin.uitest.pages;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

@DefaultXpath(by = "NewProjectDialog type", xpath = "//*[contains(@title.key, 'title.new.project')]")
@FixtureName(name = "New Project Dialog")
public class NewProjectDialogFixture extends CommonContainerFixture {
    public NewProjectDialogFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public JListFixture projectTypes() {
        return find(JListFixture.class, byXpath("//div[@class='JBList']"));
    }

    public JTextFieldFixture projectLocation() {
        return find(JTextFieldFixture.class, byXpath("//div[@class='FieldPanel']/div[1]"));
    }

    public JButtonFixture finish() {
        return  find(JButtonFixture.class, byXpath("//div[@text.key='button.finish']"));
    }
}