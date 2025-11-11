package org.gradle.ide.testutils

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import org.jetbrains.plugins.gradle.service.GradleBuildClasspathManager
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Test fixture context containing the IntelliJ project and scenario path.
 */
data class GradleProjectContext(
    val project: Project,
    val scenarioPath: Path
)

/**
 * Creates a test fixture that copies a Gradle scenario to a temp directory.
 * Depends on project and tempPath fixtures which must be declared as fields in the test class.
 *
 * The fixture only sets up scenario files. Use syncAndWait() to perform Gradle sync.
 */
fun gradleScenarioFixture(
    scenarioName: String,
    project: TestFixture<Project>,
    tempPath: TestFixture<Path>
): TestFixture<GradleProjectContext> = testFixture("Gradle scenario: $scenarioName") {
    // Initialize dependent fixtures first
    val projectInstance = project.init()
    val targetPath = tempPath.init()

    // Copy scenario files from src/test/fixtures/{scenarioName} to temp directory
    val scenarioDir = File("src/test/scenarios/$scenarioName")
    if (!scenarioDir.exists()) {
        throw IllegalArgumentException("Scenario directory not found: ${scenarioDir.absolutePath}")
    }

    println("[Setup] Copying scenario '$scenarioName' to ${targetPath.toAbsolutePath()}")
    scenarioDir.copyRecursively(targetPath.toFile(), overwrite = true)

    // Copy Gradle wrapper to the scenario directory
    copyGradleWrapper(targetPath.toFile())

    // Return initialized fixture with cleanup
    val context = GradleProjectContext(projectInstance, targetPath)
    initialized(context) {
        // Clear Gradle cache to prevent memory leaks
        clearGradleInstallationCache(projectInstance)
        // Cleanup is handled automatically by the nested fixtures (tempPath, project)
        println("[Cleanup] Gradle scenario fixture cleaned up: $scenarioName")
    }
}

/**
 * Syncs the Gradle project and waits for indexing to complete.
 * Call this AFTER the fixture is fully initialized via get().
 */
suspend fun GradleProjectContext.syncAndWait() {
    println("[Sync] Starting Gradle project sync...")
    linkGradleProject(project, scenarioPath.toAbsolutePath().toString())
    val syncResult = syncGradleProject(project, scenarioPath.toAbsolutePath().toString())
    println("[Sync] Result: $syncResult")
    waitForAllProjectActivities(project)
    println("[Sync] Project is ready for testing")
}

/**
 * Configures GradleSettings without triggering sync.
 * Sets external project path, wrapper distribution, and gradleJvm = "#JAVA_HOME".
 */
private fun linkGradleProject(project: Project, projectPath: String) {
    println("[Sync] Linking Gradle project: $projectPath")

    val settings = GradleSettings.getInstance(project)
    val projectSettings = GradleProjectSettings().apply {
        externalProjectPath = projectPath
        distributionType = DistributionType.DEFAULT_WRAPPED
        gradleJvm = "#JAVA_HOME"  // Critical for tests!
    }
    settings.linkedProjectsSettings = setOf(projectSettings)

    println("[Sync] Gradle project linked")
}

/**
 * Triggers Gradle sync with callback tracking and 120-second timeout.
 * Uses MODAL_SYNC mode and waits for completion, but indexing continues in background.
 * @return Success message with project name
 * @throws RuntimeException if sync fails or times out
 */
private suspend fun syncGradleProject(project: Project, projectPath: String): String {
    println("[Sync] Triggering Gradle sync for: $projectPath")

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
                println("[Sync] Gradle sync completed: $result")
                syncFuture.complete(result)
            }

            override fun onFailure(errorMessage: String, errorDetails: String?) {
                println("[Sync] ERROR: Gradle sync failed: $errorMessage")
                if (errorDetails != null) {
                    println("[Sync] Details: $errorDetails")
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
 * Step 3: Waits for indexing, smart mode, and stabilization.
 * Call after syncGradleProject() to ensure project is fully ready for testing.
 * Mimics IntelliJ's internal awaitProjectActivity() pattern.
 * @throws kotlinx.coroutines.TimeoutCancellationException if timeout exceeded
 */
private suspend fun waitForAllProjectActivities(project: Project) {
    println("[Sync] Waiting for all project activities to complete...")

    // 1. Wait for indexing to complete
    println("[Sync]   Waiting for indexing...")
    withTimeout(60_000) {
        IndexingTestUtil.suspendUntilIndexesAreReady(project)
    }
    println("[Sync]   Indexing completed")

    // 2. Wait for smart mode (should already be smart after indexing, but verify)
    println("[Sync]   Waiting for smart mode...")
    withTimeout(30_000) {
        while (DumbService.isDumb(project)) {
            delay(100)
        }
    }
    println("[Sync]   Project entered smart mode")

    // 3. Stabilization delay (IntelliJ's tests do this too)
    delay(500)

    println("[Sync] All project activities completed")
}

/**
 * Copies Gradle wrapper files (scripts, jar, properties) from project root to target directory.
 */
private fun copyGradleWrapper(targetDir: File) {
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

    println("[Setup] Gradle wrapper copied")
}

/**
 * Clears the GradleInstallationManager cache to prevent memory leaks.
 * The cache holds strong references to Project instances which prevents them from being GC'd.
 */
private fun clearGradleInstallationCache(project: Project) {
    try {
        val installationManager = org.jetbrains.plugins.gradle.service.GradleInstallationManager.getInstance()
        // Access the private cache field via reflection and clear it
        val cacheField = installationManager.javaClass.getDeclaredField("myBuildLayoutParametersCache")
        cacheField.isAccessible = true
        val cache = cacheField.get(installationManager) as? java.util.concurrent.ConcurrentHashMap<*, *>
        cache?.clear()
        println("[Cleanup] Cleared GradleInstallationManager cache")
    } catch (e: Exception) {
        println("[Cleanup] Warning: Could not clear Gradle cache: ${e.message}")
    }
}

/**
 * Creates a CodeInsightTestFixture for testing code completion and other IDE features.
 * Based on GradleCodeInsightBaseTestCase pattern from IntelliJ IDEA.
 *
 * Usage:
 * ```
 * val fixture = createCodeInsightFixture(context.project)
 * fixture.setUp()
 * try {
 *     // Use fixture for testing
 *     fixture.openFileInEditor(virtualFile)
 *     val completions = fixture.completeBasic()
 * } finally {
 *     fixture.tearDown()
 * }
 * ```
 */
fun createCodeInsightFixture(project: Project): CodeInsightTestFixture {
    val projectFixture = GradleProjectFixtureAdapter(project)
    val tempDirFixture = TempDirTestFixtureImpl()

    return object : CodeInsightTestFixtureImpl(projectFixture, tempDirFixture) {
        override fun shouldTrackVirtualFilePointers(): Boolean = false

        init {
            setVirtualFileFilter(null)
        }

        override fun setUp() {
            super.setUp()
            // Reload Gradle build classpath for code completion to work properly
            GradleBuildClasspathManager.getInstance(project).reload()
        }
    }
}

/**
 * Adapter to use an existing Project with CodeInsightTestFixture.
 * Based on GradleIdeaProjectTestFixture from IntelliJ IDEA.
 */
private class GradleProjectFixtureAdapter(
    private val existingProject: Project
) : IdeaProjectTestFixture {

    private lateinit var testDisposable: Disposable

    override fun getProject(): Project = existingProject

    override fun getModule(): Module {
        val modules = ModuleManager.getInstance(existingProject).modules
        return modules.firstOrNull() ?: error("No modules found in project")
    }

    override fun getTestRootDisposable(): Disposable = testDisposable

    override fun setUp() {
        testDisposable = Disposer.newDisposable("GradleProjectFixtureAdapter")
    }

    override fun tearDown() {
        Disposer.dispose(testDisposable)
    }
}
