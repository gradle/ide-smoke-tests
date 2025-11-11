package org.gradle.ide.smoke

import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.ide.testutils.createCodeInsightFixture
import org.gradle.ide.testutils.gradleScenarioFixture
import org.gradle.ide.testutils.syncAndWait
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
    fun `gradle project syncs successfully and tests code completion`() = runBlocking {
        val context = gradleProject.get()

        // Sync AFTER fixture is initialized
        context.syncAndWait()

        println("[Test] Project synced successfully!")
        println("[Test] Project path: ${context.scenarioPath.toAbsolutePath()}")
        println("[Test] Project name: ${context.project.name}")

        // Now test code completion on the synced project
        println("[Test] Starting code completion test...")

        // All UI operations must run on EDT
        withContext(Dispatchers.EDT) {
            // Create CodeInsightTestFixture
            val fixture = createCodeInsightFixture(context.project)
            fixture.setUp()

            try {
                // Find and open the build.gradle.kts file
                val buildFile = context.scenarioPath.resolve("build.gradle.kts").toFile()
                val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(buildFile)
                    ?: error("Could not find build.gradle.kts at ${buildFile.absolutePath}")

                println("[Test] Opening file: ${virtualFile.path}")
                fixture.openFileInEditor(virtualFile)

                val document = fixture.editor.document
                val text = document.text

                // Test 1: Completion after "dependencies {"
                println("\n[Test] === Testing completion after 'dependencies {' ===")
                val dependenciesOffset = text.indexOf("dependencies {") + "dependencies {".length
                testCompletionAt(fixture, dependenciesOffset, "after 'dependencies {'")

                // Test 2: Completion after "repositories {"
                println("\n[Test] === Testing completion after 'repositories {' ===")
                val repositoriesOffset = text.indexOf("repositories {") + "repositories {".length
                testCompletionAt(fixture, repositoriesOffset, "after 'repositories {'")

                // Test 3: Completion at the end of the file (top-level)
                println("\n[Test] === Testing completion at end of file (top-level) ===")
                val endOffset = text.length
                testCompletionAt(fixture, endOffset, "at end of file (top-level)")

            } finally {
                fixture.tearDown()
                println("\n[Test] Code completion test finished")
            }
        }
    }

    private fun testCompletionAt(fixture: com.intellij.testFramework.fixtures.CodeInsightTestFixture, offset: Int, description: String) {
        println("[Test] Testing completion at offset $offset ($description)")

        // Move caret to position
        fixture.editor.caretModel.moveToOffset(offset)

        // Trigger basic completion
        val lookupElements = fixture.completeBasic()

        if (lookupElements == null || lookupElements.isEmpty()) {
            println("[Test] No completion items found")
        } else {
            println("[Test] Found ${lookupElements.size} completion items")

            // Get lookup strings (what the user would see)
            val lookupStrings = lookupElements.map { it.lookupString }.sorted()

            // Log ALL items to understand what we're getting
            println("[Test] ALL Completion items:")
            lookupStrings.forEach { item ->
                println("[Test]   - $item")
            }

            // Also search for Gradle-specific items
            val gradleItems = lookupStrings.filter {
                it.contains("implementation", ignoreCase = true) ||
                it.contains("dependencies", ignoreCase = true) ||
                it.contains("maven", ignoreCase = true) ||
                it.contains("gradle", ignoreCase = true) ||
                it.contains("api", ignoreCase = true) ||
                it.contains("test", ignoreCase = true)
            }
            if (gradleItems.isNotEmpty()) {
                println("[Test] Gradle-related items found: $gradleItems")
            } else {
                println("[Test] WARNING: No Gradle-specific completion items found!")
            }
        }
    }

}
