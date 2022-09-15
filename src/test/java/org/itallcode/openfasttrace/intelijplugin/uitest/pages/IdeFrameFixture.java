package org.itallcode.openfasttrace.intelijplugin.uitest.pages;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * The IDE Frame is the main container window (or page if you prefer that) for the IntelliJ IDEA.
 */
@DefaultXpath(by = "IdeFrameImpl type", xpath = "//div[@class='IdeFrameImpl']")
@FixtureName(name = "IDE Frame")
public class IdeFrameFixture extends CommonContainerFixture {
    public IdeFrameFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public JMenuBarFixture menuBar() {
        return find(JMenuBarFixture.class, byXpath("//div[@class='LinuxIdeMenuBar']"), Duration.ofSeconds(20));
    }
}