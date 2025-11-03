package org.gradle.ide.smoke

import com.intellij.openapi.diagnostic.Logger
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import kotlinx.coroutines.runBlocking
import org.gradle.ide.testutils.copyGradleWrapper
import org.gradle.ide.testutils.gradleScenarioFixture
import org.gradle.ide.testutils.linkSyncAndWaitForGradleProject
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@TestApplication
class SimpleKotlinProjectTest {

    private val LOG = Logger.getInstance(SimpleKotlinProjectTest::class.java)

    private companion object {
        // Base fixtures - these must be declared as fields for the test framework to initialize them
        private val tempPath = tempPathFixture()
        private val project = projectFixture()

        // Gradle scenario fixture that depends on the base fixtures
        private val gradleProject = gradleScenarioFixture("simple-kotlin", project, tempPath)
    }

    @Test
    fun `gradle project syncs successfully`() = runBlocking {
        LOG.info("=== Test: gradle project syncs successfully ===")

        // Get the initialized context
        val context = gradleProject.get()

        // Copy Gradle wrapper to the scenario directory
        copyGradleWrapper(context.scenarioPath.toFile())

        // Link, sync, and wait for the Gradle project
        val syncResult = linkSyncAndWaitForGradleProject(
            context.project,
            context.scenarioPath.toAbsolutePath().toString()
        )

        LOG.info("Sync result: $syncResult")

        // Verify Gradle project is linked
        val gradleSettings = GradleSettings.getInstance(context.project)
        Assertions.assertEquals(1, gradleSettings.linkedProjectsSettings.size)

        val projectSettings = gradleSettings.linkedProjectsSettings.first()
        Assertions.assertEquals(
            context.scenarioPath.toAbsolutePath().toString(),
            projectSettings.externalProjectPath
        )

        LOG.info("=== Test completed successfully ===")
    }

}
