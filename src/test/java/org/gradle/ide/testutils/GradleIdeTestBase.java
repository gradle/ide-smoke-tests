package org.gradle.ide.testutils;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base class for Gradle IDE integration tests.
 * Provides common setup and utility methods for testing Gradle features in IntelliJ IDEA.
 */
public abstract class GradleIdeTestBase extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    /**
     * Loads a Gradle scenario from the scenarios directory.
     * Copies all files from the scenario into the test fixture.
     *
     * @param scenarioName the name of the scenario directory under testData/scenarios/
     */
    protected void loadScenario(String scenarioName) throws IOException {
        Path scenarioPath = Paths.get(getTestDataPath(), "scenarios", scenarioName);

        if (!Files.exists(scenarioPath)) {
            fail("Scenario not found: " + scenarioPath);
        }

        // Copy all files from the scenario into the test fixture
        try (var stream = Files.walk(scenarioPath)) {
            stream.filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Path relativePath = scenarioPath.relativize(file);
                        String content = Files.readString(file);
                        myFixture.addFileToProject(relativePath.toString(), content);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load file: " + file, e);
                    }
                });
        }
    }

    /**
     * Creates a test Gradle build file with the given content.
     * Use this for inline test content.
     */
    protected void createBuildFile(String content) {
        myFixture.configureByText("build.gradle.kts", content);
    }

    /**
     * Creates a test Gradle settings file with the given content.
     */
    protected void createSettingsFile(String content) {
        myFixture.configureByText("settings.gradle.kts", content);
    }
}
