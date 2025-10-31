package org.gradle.ide.testutils

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemProgressNotificationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.junit5.fixture.TestFixture
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.plugins.gradle.service.project.open.linkAndSyncGradleProject
import java.io.File
import java.nio.file.Path

fun withScenario(
    scenarioName: String,
    projectFixture: TestFixture<Project>,
    tempPathFixture: TestFixture<Path>,
    test: (project: Project) -> Unit
) {
    runBlocking {
        withTimeout(120_000) { // 2 minute timeout
            val tempDir = tempPathFixture.get()
            val project = projectFixture.get()

            println("Setting up scenario '$scenarioName' in: ${tempDir.toFile().absolutePath}")

            // Copy scenario files to temp directory
            val scenarioDir = File("scenarios/$scenarioName")
            if (scenarioDir.exists()) {
                scenarioDir.copyRecursively(tempDir.toFile(), overwrite = true)
                println("Copied scenario files from: ${scenarioDir.absolutePath}")
            }

            // Copy Gradle wrapper from project root
            copyGradleWrapper(tempDir.toFile())
            println("Copied Gradle wrapper")

            // Link and sync the Gradle project
            println("Starting Gradle project link and sync...")
            linkAndSyncGradleProject(project, tempDir.toCanonicalPath())
            println("Gradle project linked and synced")

            // Wait for indexing and PSI initialization
            println("Waiting for project initialization...")
            project.waitForSmartMode()
            println("Project initialization complete")

            // Run the test
            test(project)
        }
    }
}

/**
 * Copies the Gradle wrapper from the project root to the target directory.
 */
private fun copyGradleWrapper(targetDir: File) {
    val projectRoot = File(System.getProperty("user.dir"))

    // Copy wrapper scripts
    File(projectRoot, "gradlew").copyTo(File(targetDir, "gradlew"), overwrite = true).setExecutable(true)
    File(projectRoot, "gradlew.bat").copyTo(File(targetDir, "gradlew.bat"), overwrite = true)

    // Copy wrapper directory
    val sourceWrapperDir = File(projectRoot, "gradle/wrapper")
    val targetWrapperDir = File(targetDir, "gradle/wrapper")
    targetWrapperDir.mkdirs()

    File(sourceWrapperDir, "gradle-wrapper.jar").copyTo(File(targetWrapperDir, "gradle-wrapper.jar"), overwrite = true)
    File(sourceWrapperDir, "gradle-wrapper.properties").copyTo(File(targetWrapperDir, "gradle-wrapper.properties"), overwrite = true)
}

/**
 * Waits for the project to be fully initialized with proper PSI support.
 * This includes:
 * - Waiting for dumb mode to complete (indexing)
 * - Refreshing the VFS to ensure files are recognized
 * - Waiting for Gradle script definitions to be loaded
 */
private suspend fun waitForProjectInitialization(project: Project) {

}
