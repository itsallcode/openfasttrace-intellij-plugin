package org.itallcode.openfasttrace.intelijplugin.uitest.pages;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * The Welcome Frame is the container window (or page if you prefer that) for everything that happens before IntelliJ
 * opens the actual IDE window.
 * <p>
 * Displaying a welcome message is the smaller part of that job. The main reason is that the IDE needs a project context
 * to work with and that context does not exist with a fresh installation. That is also why the welcome screen contains
 * the means of opening, importing or creating projects in a sub dialog.
 * </p>
 */
@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome Frame")
public class WelcomeFrameFixture extends CommonContainerFixture {
    public WelcomeFrameFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public JButtonFixture createNewProjectLink() {
        // The button style changes from an icon to a text button if a project already exists. To make things worse,
        // In case of the icon the button is followed by a label that has the same text on it as the link-style button.
        // We need a bit of XPath complexity, to pick either of the clickable items.
        return find(JButtonFixture.class, byXpath("(//div[@defaulticon='createNewProjectTab.svg']" +
                "|//div[@visible_text='New Project'])[1]"));
    }
}