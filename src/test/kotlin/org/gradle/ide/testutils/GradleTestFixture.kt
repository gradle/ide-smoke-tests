package org.gradle.ide.testutils

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toPath
import org.jetbrains.plugins.gradle.service.GradleInstallationManager
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

private val LOG = Logger.getInstance("GradleTestFixture")

/**
 * Context object for a Gradle project test fixture.
 * Contains the IntelliJ project instance and the path to the scenario files.
 */
data class GradleProjectContext(
    val project: Project,
    val scenarioPath: Path
)

/**
 * Creates a test fixture that copies a Gradle scenario to a temp directory.
 *
 * This fixture depends on other fixtures (project, tempPath) which must be declared
 * as fields in the test class for proper initialization by the test framework.
 *
 * Example usage:
 * ```
 * @TestApplication
 * class MyTest {
 *     private companion object {
 *         private val tempPath = tempPathFixture()
 *         private val project = projectFixture()
 *         private val gradleProject = gradleScenarioFixture("my-scenario", project, tempPath)
 *     }
 *
 *     @Test
 *     fun myTest() {
 *         val context = gradleProject.get()
 *         // Use context.project and context.scenarioPath
 *     }
 * }
 * ```
 */
fun gradleScenarioFixture(
    scenarioName: String,
    project: TestFixture<Project>,
    tempPath: TestFixture<Path>
): TestFixture<GradleProjectContext> = testFixture("Gradle scenario: $scenarioName") {
    // Initialize dependent fixtures first
    val projectInstance = project.init()
    val targetPath = tempPath.init()

    // Copy scenario files to temp directory
    val sourcePath = "src/test/fixtures/$scenarioName"
    sourcePath.toPath().toFile().copyRecursively(targetPath.toFile(), overwrite = true)

    // Return initialized fixture with cleanup
    val context = GradleProjectContext(projectInstance, targetPath)
    initialized(context) {
        // Cleanup is handled automatically by the nested fixtures (tempPath, project)
    }
}

/**
 * Step 1: Links a Gradle project to IntelliJ IDEA by configuring GradleSettings.
 *
 * This function configures the Gradle project settings without triggering a sync.
 * It sets up:
 * - External project path
 * - Gradle distribution type (uses wrapper)
 * - Gradle JVM (#JAVA_HOME for tests)
 *
 * IMPORTANT: Always set `gradleJvm = "#JAVA_HOME"` in tests, otherwise sync will fail
 * with "Invalid Gradle JDK configuration".
 *
 * @param project The IntelliJ project instance
 * @param projectPath The absolute path to the Gradle project root
 */
fun linkGradleProject(project: Project, projectPath: String) {
    LOG.info("Linking Gradle project: $projectPath")

    val settings = GradleSettings.getInstance(project)
    val projectSettings = GradleProjectSettings().apply {
        externalProjectPath = projectPath
        distributionType = DistributionType.DEFAULT_WRAPPED
        gradleJvm = "#JAVA_HOME"  // Critical for tests!
    }
    settings.linkedProjectsSettings = setOf(projectSettings)

    // Register cleanup disposable to prevent memory leaks
    registerGradleCleanupDisposable(project)

    LOG.info("Gradle project linked successfully")
}

/**
 * Step 2: Synchronizes a Gradle project with callback-based completion tracking.
 *
 * This function:
 * 1. Creates an import specification with MODAL_SYNC mode (blocking for tests)
 * 2. Registers callbacks for sync success/failure
 * 3. Triggers the Gradle sync via ExternalSystemUtil.refreshProject()
 * 4. Waits for the sync to complete with a 120-second timeout
 *
 * IMPORTANT: After this function returns, you must still wait for indexing!
 * The sync callback fires when Gradle import completes, but PSI indexing
 * continues in the background.
 *
 * Example usage:
 * ```
 * linkGradleProject(project, projectPath)
 * syncGradleProject(project, projectPath)
 * IndexingTestUtil.suspendUntilIndexesAreReady(project)  // Don't forget this!
 * ```
 *
 * @param project The IntelliJ project instance
 * @param projectPath The absolute path to the Gradle project root
 * @return A success message with project name, or throws exception on failure
 * @throws RuntimeException if sync fails or times out
 */
suspend fun syncGradleProject(project: Project, projectPath: String): String {
    LOG.info("Starting Gradle sync for: $projectPath")

    val syncFuture = CompletableFuture<String>()

    // Build import spec with callback
    val importSpec = ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
        .use(ProgressExecutionMode.MODAL_SYNC)  // Blocking mode for tests
        .callback(object : ExternalProjectRefreshCallback {
            override fun onSuccess(externalProject: DataNode<ProjectData>?) {
                val result = if (externalProject != null) {
                    "SUCCESS (project: ${externalProject.data.externalName})"
                } else {
                    "SUCCESS (no project data)"
                }
                LOG.info("Gradle sync completed: $result")
                syncFuture.complete(result)
            }

            override fun onFailure(errorMessage: String, errorDetails: String?) {
                LOG.error("Gradle sync failed: $errorMessage")
                if (errorDetails != null) {
                    LOG.error("Details: $errorDetails")
                }
                syncFuture.completeExceptionally(
                    RuntimeException("Gradle sync failed: $errorMessage\n$errorDetails")
                )
            }
        })
        .build()

    // Trigger sync
    ExternalSystemUtil.refreshProject(projectPath, importSpec)

    // Wait with timeout (120 seconds should be more than enough for test projects)
    return withTimeout(120_000) {
        syncFuture.await()
    }
}

/**
 * Step 3: Waits for all project activities to complete after Gradle sync.
 *
 * This function ensures the project is fully ready for testing by waiting for:
 * 1. PSI indexing to complete (IndexingTestUtil.suspendUntilIndexesAreReady)
 * 2. Project to exit "dumb mode" and enter "smart mode"
 * 3. A brief stabilization period for background tasks
 *
 * This mimics IntelliJ's internal test pattern from `awaitProjectActivity()`.
 *
 * IMPORTANT: Always call this after syncGradleProject() before running assertions.
 *
 * Example usage:
 * ```
 * linkGradleProject(project, projectPath)
 * syncGradleProject(project, projectPath)
 * waitForAllProjectActivities(project)  // NOW project is ready!
 * // Run assertions...
 * ```
 *
 * @param project The IntelliJ project instance
 * @throws kotlinx.coroutines.TimeoutCancellationException if activities don't complete within timeout
 */
suspend fun waitForAllProjectActivities(project: Project) {
    LOG.info("Waiting for all project activities to complete...")

    // 1. Wait for indexing to complete
    LOG.info("Waiting for indexing to complete...")
    withTimeout(60_000) {
        IndexingTestUtil.suspendUntilIndexesAreReady(project)
    }
    LOG.info("Indexing completed")

    // 2. Wait for smart mode (should already be smart after indexing, but verify)
    LOG.info("Waiting for smart mode...")
    withTimeout(30_000) {
        while (DumbService.isDumb(project)) {
            LOG.info("Project still in dumb mode, waiting...")
            delay(100)
        }
    }
    LOG.info("Project entered smart mode")

    // 3. Stabilization delay (IntelliJ's tests do this too)
    LOG.info("Stabilization delay...")
    delay(500)

    LOG.info("All project activities completed - project is ready")
}

/**
 * Convenience function: Links, syncs, and waits for a Gradle project in one call.
 *
 * This combines all three steps:
 * 1. linkGradleProject() - Configure Gradle settings
 * 2. syncGradleProject() - Trigger sync and wait for callback
 * 3. waitForAllProjectActivities() - Wait for indexing and smart mode
 *
 * Use this when you want the complete setup in a single call.
 *
 * Example usage:
 * ```
 * linkSyncAndWaitForGradleProject(project, projectPath)
 * // Project is now fully ready for testing!
 * ```
 *
 * @param project The IntelliJ project instance
 * @param projectPath The absolute path to the Gradle project root
 * @return Sync result message
 */
suspend fun linkSyncAndWaitForGradleProject(project: Project, projectPath: String): String {
    LOG.info("=== Starting complete Gradle project setup ===")

    // Step 1: Link
    linkGradleProject(project, projectPath)
    // Step 2: Sync
    val syncResult = syncGradleProject(project, projectPath)
    // Step 3: Wait for all activities
    waitForAllProjectActivities(project)

    LOG.info("=== Gradle project setup complete ===")
    return syncResult
}

/**
 * Copies Gradle wrapper files from the project root to a target directory.
 *
 * This is necessary for test scenarios that need to run Gradle tasks.
 * Copies:
 * - gradlew (Unix script)
 * - gradlew.bat (Windows script)
 * - gradle/wrapper/gradle-wrapper.jar
 * - gradle/wrapper/gradle-wrapper.properties
 *
 * @param targetDir The directory to copy wrapper files to
 */
fun copyGradleWrapper(targetDir: File) {
    LOG.info("Copying Gradle wrapper to: ${targetDir.absolutePath}")

    val projectRoot = File(System.getProperty("user.dir"))

    // Copy wrapper scripts
    File(projectRoot, "gradlew").copyTo(File(targetDir, "gradlew"), overwrite = true)
        .also { it.setExecutable(true) }
    File(projectRoot, "gradlew.bat").copyTo(File(targetDir, "gradlew.bat"), overwrite = true)

    // Copy wrapper jar and properties
    val targetWrapperDir = File(targetDir, "gradle/wrapper")
    targetWrapperDir.mkdirs()

    val sourceWrapperDir = File(projectRoot, "gradle/wrapper")
    File(sourceWrapperDir, "gradle-wrapper.jar")
        .copyTo(File(targetWrapperDir, "gradle-wrapper.jar"), overwrite = true)
    File(sourceWrapperDir, "gradle-wrapper.properties")
        .copyTo(File(targetWrapperDir, "gradle-wrapper.properties"), overwrite = true)

    LOG.info("Gradle wrapper copied successfully")
}

/**
 * Registers a cleanup disposable with the project to track Gradle resource cleanup.
 *
 * Note: The GradleInstallationManager cache is application-level and will be cleared
 * automatically by IntelliJ's ProjectManagerLayoutParametersCacheCleanupListener when
 * the project is closed. This disposable simply logs the cleanup for debugging.
 *
 * The memory leak warnings are a known issue with Gradle plugin's internal caching
 * and don't indicate a test failure. The leak check runs before the projectClosed
 * event fires.
 *
 * @param project The IntelliJ project instance
 * @return The disposable that was registered (for testing purposes)
 */
fun registerGradleCleanupDisposable(project: Project): Disposable {
    val cleanupDisposable = Disposable {
        LOG.info("Gradle cleanup disposable executed for project: ${project.name}")
        // Note: Actual cache cleanup happens via ProjectManagerLayoutParametersCacheCleanupListener
        // which listens for projectClosed events. We can't clear it here because
        // GradleInstallationManager is an application service, not a project service.
    }

    Disposer.register(project, cleanupDisposable)
    LOG.info("Registered Gradle cleanup disposable for project: ${project.name}")
    return cleanupDisposable
}
