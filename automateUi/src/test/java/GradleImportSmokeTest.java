import com.automation.remarks.junit5.Video;
import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.TextEditorFixture;
import com.intellij.remoterobot.utils.Keyboard;
import org.assertj.swing.core.MouseButton;
import org.intellij.examples.simple.plugin.utils.IdeaFrame;
import org.intellij.examples.simple.plugin.utils.WelcomeFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.intellij.examples.simple.plugin.utils.RemoteRobotExtension;

import java.awt.event.KeyEvent;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitForIgnoringError;
import static java.awt.event.KeyEvent.VK_END;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.assertj.swing.timing.Pause.pause;

@ExtendWith(RemoteRobotExtension.class)
public class GradleImportSmokeTest {

    @Test
//    @Video
    void createCommandLineProject(final RemoteRobot remoteRobot) {
//        /Users/reinholddegenfellner/IdeaProjects/ide-smoke-tests/01-build-src
        WelcomeFrame welcome = remoteRobot.find(WelcomeFrame.class);
        welcome.getOpenProject().click();
        Keyboard keyboard = new Keyboard(remoteRobot);
        welcome.getPath().setText("/Users/reinholddegenfellner/IdeaProjects/ide-smoke-tests/01-build-src");
        welcome.getOk().click();
        IdeaFrame idea = remoteRobot.find(IdeaFrame.class, ofSeconds(10));
        waitFor(ofMinutes(5), () -> !idea.isDumbMode());

//        waitForIgnoringError(Duration.ofMinutes(3), Duration.ofSeconds(5),
//                "Wait for Ide started", "Ide is not started", () -> remoteRobot.callJs("true"));
        step("open build.gradle.kts", () -> {
//            keyboard.pressing();
            remoteRobot.runJs("robot.pressKey(" + KeyEvent.VK_META + ");" +
                    "robot.pressKey(" + KeyEvent.VK_SHIFT + ");"+
                    "robot.pressKey(" + KeyEvent.VK_O + ");"+
                    "robot.releaseKey(" + KeyEvent.VK_O + ");"+
                    "robot.releaseKey(" + KeyEvent.VK_SHIFT + ");"+
                    "robot.releaseKey(" + KeyEvent.VK_META + ");");
            keyboard.enterText("build.gradle.kts");
            keyboard.enter();
//            var projectView = idea.getProjectViewTree();
//            if (projectView.isEmpty()) {
////                remoteRobot.find(byXpath("More Action", "//div[@accessiblename='More Actions']"), ofSeconds(10))
//                idea.getProjectToggle().click();
////                projectView.findText(idea.getProjectName()).doubleClick();
////                waitFor(() -> projectView.hasText("src"));
//            }
//            idea.getProjectViewTree().stream().findFirst().ifPresent(view -> {
//                view.
//            });
//            projectView.findText("src").click(MouseButton.RIGHT_BUTTON);
//            actionMenu(remoteRobot, "New").click();
//            actionMenuItem(remoteRobot, "Java Class").click();
//            keyboard.enterText("App");
//            keyboard.enter();
        });

        final TextEditorFixture editor = idea.textEditor(Duration.ofSeconds(2));

        step("Write a code", () -> {
//            pause(ofSeconds(15).toMillis());
//            keyboard.pressing(KeyEvent.VK_META, kb -> {
//                editor.findAllText().forEach(text -> {
//                    System.out.println(text.getText());
//                });
//                editor.getEditor().findText("buildsrc-java-plugin").click();
//                return null;
//            });
//            keyboard.key(VK_END);
//            keyboard.enter();
//            sharedSteps.autocomplete("main");
//            sharedSteps.autocomplete("sout");
//            keyboard.enterText("\"");
//            keyboard.enterText("Hello from UI test");
        });

    }
}
