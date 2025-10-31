package org.gradle.ide.smoke;

import com.intellij.psi.PsiFile;
import org.gradle.ide.testutils.GradleIdeTestBase;
import org.junit.Test;

import java.io.IOException;

/**
 * Simple smoke test to verify we can load a Gradle build file and IntelliJ can parse it.
 */
public class SimpleGradleTest extends GradleIdeTestBase {

    @Test
    public void testCanLoadGradleBuildFile() throws IOException {
        // Load the simple-kotlin scenario
        loadScenario("simple-kotlin");

        // Open the build file
        PsiFile buildFile = myFixture.configureByFile("build.gradle.kts");

        // Verify the file was loaded
        assertNotNull("Build file should be loaded", buildFile);
        assertTrue("Build file should contain kotlin plugin",
            buildFile.getText().contains("kotlin(\"jvm\")"));
    }
}
