import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;
import com.intellij.remoterobot.utils.Keyboard;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import org.assertj.core.api.Assertions;
import org.intellij.examples.simple.plugin.utils.IdeaFrame;
import org.intellij.examples.simple.plugin.utils.RemoteRobotExtension;
import org.intellij.examples.simple.plugin.utils.WelcomeFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

@ExtendWith(RemoteRobotExtension.class)
public class GradleImportSmokeTest {

    @Test
//    @Video
    void createCommandLineProject(final RemoteRobot remoteRobot) {
        var welcome = remoteRobot.find(WelcomeFrame.class);
        welcome.getOpenProject().click();
        var keyboard = new Keyboard(remoteRobot);
        welcome.getPath().setText("/Users/reinholddegenfellner/Documents/GitHub/ide-smoke-tests/01-build-src");
//        /Users/reinholddegenfellner/Documents/GitHub/ide-smoke-tests/01-build-src/buildSrc/src/main/groovy/org/gradle/example/BuildSrcGroovyPlugin.groovy
//        waitFor(ofMinutes(5), () -> !idea.isDumbMode());
        welcome.getOk().click();

//        waitFor(ofMinutes(5), () -> !didTheProjectImportFinish(remoteRobot));
        var idea = remoteRobot.find(IdeaFrame.class, ofSeconds(10));
        waitFor(ofMinutes(5), () -> !idea.isDumbMode());

//        waitForIgnoringError(Duration.ofMinutes(3), Duration.ofSeconds(5),
//                "Wait for Ide started", "Ide is not started", () -> remoteRobot.callJs("true"));
        step("open build.gradle.kts", () -> {
//            keyboard.pressing();
            remoteRobot.find(ComponentFixture.class, byXpath("//div[@text='File']")).click();
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
            remoteRobot.find(ComponentFixture.class, byXpath("//div[@text='File']//div[@text='Open...']")).click(new Point(1,1));
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
            remoteRobot.find(JTextFieldFixture.class, byXpath("//div[@class='BorderlessTextField']")).setText("/Users/reinholddegenfellner/Documents/GitHub/ide-smoke-tests/01-build-src/build.gradle.kts");
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
            remoteRobot.find(JButtonFixture.class, byXpath("//div[@text='OK']")).click();
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
            remoteRobot.find(JButtonFixture.class, byXpath("//div[@text='Open as File']")).click();
///            var projectView = idea.getProjectViewTree();
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


        step("Write a code", () -> {
            final TextEditorFixture editor = idea.textEditor(Duration.ofSeconds(2));
            EditorFixture editor1 = editor.getEditor();
            Point locationOnScreen = editor1.getLocationOnScreen();
            System.out.println(locationOnScreen);
//            RemoteText t = editor1.findText("buildsrc-java-plugin");
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
//            editor.getStatusButton()
            editor1.extractData().stream()
                    .filter(t -> t.getText().contains("buildsrc-java-plugin"))
                    .findFirst()
                    .ifPresent(t -> {
                        remoteRobot.runJs("robot.pressModifiers(" + InputEvent.META_MASK + ");" +
                                "robot.click(new Point(" + (locationOnScreen.getX() + t.getPoint().getX()) + ", " + (locationOnScreen.getY() + t.getPoint().getY()) + "), MouseButton.LEFT_BUTTON, 1);" +
                                "robot.releaseModifiers(" + InputEvent.META_MASK + ");");
                    });
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
            remoteRobot.find(ComponentFixture.class, byXpath("//div[@text='Accept']")).click();
            waitFor(ofMinutes(5), () -> !idea.isDumbMode());
            Assertions.assertThat(remoteRobot.findAll(ComponentFixture.class, byXpath("//div[@accessiblename='PluginDependencySpecAccessorsKt.class' and @class='SimpleColoredComponent']")).isEmpty()).isFalse();
//            System.out.println(editor1.extractData().get(0).getText());
//            text.click();
//            editor.getEditor().fin;
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

    private static boolean didTheProjectImportFinish(RemoteRobot remoteRobot) {
        try {
            remoteRobot.find(ComponentFixture.class, byXpath("//div[@class='EngravedLabel']"));
        } catch (WaitForConditionTimeoutException e) {
            return true;
        }
        return false;
    }
}
