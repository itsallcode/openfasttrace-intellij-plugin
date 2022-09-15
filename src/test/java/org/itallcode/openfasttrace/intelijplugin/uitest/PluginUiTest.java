package org.itallcode.openfasttrace.intelijplugin.uitest;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.intellij.remoterobot.fixtures.JMenuBarFixture;
import org.itallcode.openfasttrace.intelijplugin.uitest.pages.IdeFrameFixture;
import org.itallcode.openfasttrace.intelijplugin.uitest.pages.NewProjectDialogFixture;
import org.itallcode.openfasttrace.intelijplugin.uitest.pages.WelcomeFrameFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;

import static org.itallcode.openfasttrace.intelijplugin.remoterobot.RemoteRobotProperties.*;

class PluginUiTest {
    final static Duration WITH_PATIENCE = Duration.ofSeconds(10);
    @TempDir
    static Path projectTempDir;

    @Test
    void testOftMenuEntryExists() {
        final RemoteRobot robot = new RemoteRobot(ROBOT_BASE_URL);
        final WelcomeFrameFixture welcomeFrame = robot.find(WelcomeFrameFixture.class, WITH_PATIENCE);
        welcomeFrame.createNewProjectLink().click();
        final NewProjectDialogFixture project = robot.find(NewProjectDialogFixture.class, WITH_PATIENCE);
        project.projectTypes().clickItem("Empty Project", true);
        project.projectLocation().setText(projectTempDir.toAbsolutePath().toString());
        project.finish().click();
        final IdeFrameFixture ide = robot.find(IdeFrameFixture.class, WITH_PATIENCE);
        waitUntilMenuBarIsReady();
        final JMenuBarFixture menuBar = ide.menuBar();
        menuBar.select("Help");
        assertDoesNotThrow(() -> robot.find(ComponentFixture.class,
                byXpath("//div[@class='ActionMenu']//div[@text='OpenFastTrace User Guide']")));
    }

    @SuppressWarnings("java:S2925")
    private void waitUntilMenuBarIsReady() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }
}