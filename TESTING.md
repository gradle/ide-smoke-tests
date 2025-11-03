# Complete Guide to Testing Gradle Projects in IntelliJ Platform

This comprehensive guide teaches you how to properly test Gradle integration in IntelliJ Platform projects, based on real-world problem-solving and deep analysis of IntelliJ's own test infrastructure.

## Table of Contents

1. [Quick Start](#quick-start)
2. [The Core Problem We Solved](#the-core-problem-we-solved)
3. [Understanding IntelliJ Platform Testing](#understanding-intellij-platform-testing)
4. [Proper Gradle Sync Patterns](#proper-gradle-sync-patterns)
5. [Test Lifecycle Management](#test-lifecycle-management)
6. [Logging and Debugging](#logging-and-debugging)
7. [Complete Working Examples](#complete-working-examples)
8. [Common Pitfalls](#common-pitfalls)
9. [Reference Documentation](#reference-documentation)

---

## Quick Start

**TL;DR:** Don't just call `waitForSmartMode()` after `linkAndSyncGradleProject()`. The sync is asynchronous and needs proper waiting for completion.

### ❌ Wrong Pattern (Hangs Forever)
```kotlin
linkAndSyncGradleProject(project, projectPath)
project.waitForSmartMode()  // HANGS! Project never becomes smart
```

### ✅ Correct Pattern
```kotlin
// 1. Link Gradle project settings
linkGradleProject(project, projectPath)

// 2. Sync with callback tracking
val syncFuture = CompletableFuture<Void>()
val importSpec = ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
    .use(ProgressExecutionMode.MODAL_SYNC)
    .callback(object : ExternalProjectRefreshCallback {
        override fun onSuccess(externalProject: DataNode<ProjectData>?) {
            syncFuture.complete(null)
        }
        override fun onFailure(errorMessage: String, errorDetails: String?) {
            syncFuture.completeExceptionally(RuntimeException(errorMessage))
        }
    })
    .build()

ExternalSystemUtil.refreshProject(projectPath, importSpec)

// 3. Wait for sync completion
withTimeout(120_000) {
    syncFuture.await()
}

// 4. Wait for indexing
IndexingTestUtil.suspendUntilIndexesAreReady(project)

// 5. NOW the project is ready!
```

---

## The Core Problem We Solved

### Original Issue

Tests were hanging indefinitely at this line:
```kotlin
project.waitForSmartMode()  // TestScenario.kt:51
```

### Root Causes

1. **`linkAndSyncGradleProject()` is asynchronous**
   - It's a `suspend` function that returns immediately
   - Gradle sync happens in background
   - No guarantee project will ever exit "dumb mode"

2. **Missing JDK Configuration**
   - Gradle needs a configured JVM to run
   - Without it, sync fails silently with "Invalid Gradle JDK configuration"

3. **No Proper Completion Tracking**
   - Callbacks complete but internal tasks continue
   - PSI indexing happens separately
   - Workspace model updates are async

4. **Project Lifecycle Issues**
   - Tests shared fixtures across methods
   - Second test got disposed project from first test
   - "Project is closed" errors

### The Solution

See [SOLUTION.md](SOLUTION.md) for the complete problem-solving journey.

**Key insight:** Wait for **ALL project activities**, not just the sync callback:
1. Sync completion (via callback)
2. Indexing completion
3. Smart mode achievement
4. Background tasks stabilization

**Result:** Tests now complete in ~1 second instead of hanging forever.

---

## Understanding IntelliJ Platform Testing

### The Project Object

The `Project` represents a complete IntelliJ IDEA project instance with:
- **Project Model**: Files, modules, libraries
- **PSI**: Program Structure Interface (parsed code)
- **Indexing**: Fast code search and navigation
- **Build Systems**: Gradle/Maven integration
- **Services**: VFS, SDKs, all IDE features

### Smart Mode vs Dumb Mode

The project operates in two states:

**Dumb Mode** (`DumbService.isDumb() = true`):
- Indexing in progress
- Limited features available
- Can't do code navigation, completion, etc.

**Smart Mode** (`DumbService.isDumb() = false`):
- Fully indexed
- All IDE features work
- Tests can run assertions

### The Test Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│ 1. Create Project                                       │
│    - JUnit creates fixtures                             │
│    - Project instance created                           │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 2. Setup Scenario                                       │
│    - Copy files to temp directory                       │
│    - Copy Gradle wrapper                                │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 3. Link Gradle Project                                  │
│    - Configure GradleSettings                           │
│    - Set Gradle JVM (#JAVA_HOME)                        │
│    - Link project path                                  │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 4. Sync Gradle Project                                  │
│    - Trigger ExternalSystemUtil.refreshProject()        │
│    - Gradle runs in separate process                    │
│    - IntelliJ imports project structure                 │
│    ⏱  Takes 200-500ms typically                         │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 5. Wait for Sync Callback                               │
│    - onSuccess() or onFailure() called                  │
│    ⚠️  Sync complete BUT async tasks continue!         │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 6. Wait for Indexing                                    │
│    - IndexingTestUtil.suspendUntilIndexesAreReady()     │
│    - PSI building, file indexing                        │
│    ⏱  Takes 200-1000ms typically                        │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 7. Verify Smart Mode                                    │
│    - DumbService.isDumb() should be false               │
│    - Project is now fully ready                         │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 8. Run Test Assertions                                  │
│    - PSI navigation works                               │
│    - Code completion works                              │
│    - All IDE features available                         │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│ 9. Cleanup                                              │
│    - Remove listeners                                   │
│    - JUnit disposes project                             │
│    - Delete temp files                                  │
└─────────────────────────────────────────────────────────┘
```

---

## Proper Gradle Sync Patterns

### Pattern 1: Callback-Based (Recommended for External Tests)

This is what we use in our tests since we can't access IntelliJ's internal test utilities.

```kotlin
suspend fun syncGradleProjectWithCallback(project: Project, projectPath: String): String {
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
                syncFuture.complete(result)
            }

            override fun onFailure(errorMessage: String, errorDetails: String?) {
                syncFuture.completeExceptionally(
                    RuntimeException("Gradle sync failed: $errorMessage\n$errorDetails")
                )
            }
        })
        .build()

    // Trigger sync
    ExternalSystemUtil.refreshProject(projectPath, importSpec)

    // Wait with timeout
    return withTimeout(120_000) {
        syncFuture.await()
    }
}

// IMPORTANT: Still need to wait for indexing after this!
IndexingTestUtil.suspendUntilIndexesAreReady(project)
```

### Pattern 2: IntelliJ's Internal Pattern (Reference Only)

IntelliJ's tests use `awaitProjectActivity()` which waits for everything:

```kotlin
// From intellij-community (internal tests only)
suspend fun awaitProjectActivity(project: Project, action: suspend () -> R): R {
    try {
        return project.trackActivity(TestProjectActivityKey, action)
    } finally {
        TestObservation.awaitConfiguration(project, DEFAULT_SYNC_TIMEOUT)
        IndexingTestUtil.suspendUntilIndexesAreReady(project)
    }
}

// Usage
awaitProjectActivity(project) {
    linkAndSyncGradleProject(project, projectPath)
}
```

**Why we can't use this:** It's in `testSrc/` directories, not accessible to external projects.

### Pattern 3: Complete Activity Waiting

Our implementation mimicking IntelliJ's pattern:

```kotlin
suspend fun waitForAllProjectActivities(project: Project) {
    LOG.info("Waiting for all project activities...")

    // 1. Wait for indexing
    withTimeout(60_000) {
        IndexingTestUtil.suspendUntilIndexesAreReady(project)
    }

    // 2. Wait for smart mode
    withTimeout(30_000) {
        while (DumbService.isDumb(project)) {
            delay(100)
        }
    }

    // 3. Stabilization delay (IntelliJ's tests do this too)
    delay(500)
}
```

---

## Test Lifecycle Management

### The Fixture Sharing Problem

**JUnit 5 fixtures at class level are SHARED across test methods!**

```kotlin
@TestApplication
class MyTest {
    // ❌ SHARED across all tests!
    private val projectFixture = projectFixture()

    @Test
    fun test1() {
        val project = projectFixture.get()  // Gets project
        // Test runs
        // JUnit starts cleanup
    }

    @Test
    fun test2() {
        val project = projectFixture.get()  // Gets SAME project being disposed!
        // ❌ "Project is closed" error
    }
}
```

**What happens:**
1. Test 1 runs and completes
2. Async Gradle operations continue
3. JUnit begins disposing the project
4. Test 2 starts with disposed project
5. **Boom:** "Project is closed"

### How IntelliJ Solves This

IntelliJ Platform tests use **four main patterns**:

#### Solution 1: One Test Per Class (Most Common)

```kotlin
@TestApplication
class GradleProjectSyncTest {
    private val projectFixture = projectFixture()
    private val tempPathFixture = tempPathFixture()

    @Test
    fun `test sync`() = runBlocking {
        // Only one test = no sharing problem!
        val project = projectFixture.get()
        // Test logic...
    }
}

@TestApplication
class GradlePsiResolutionTest {
    private val projectFixture = projectFixture()
    private val tempPathFixture = tempPathFixture()

    @Test
    fun `test PSI`() = runBlocking {
        // Fresh project in fresh class!
        val project = projectFixture.get()
        // Test logic...
    }
}
```

**Pros:** Simple, works with current code, no sharing issues
**Cons:** More test classes

#### Solution 2: JUnit 4 with setUp/tearDown (Traditional)

```kotlin
abstract class GradleImportingTestCase {
    protected lateinit var myProject: Project

    @Before
    fun setUp() {
        myProject = createTestProject()  // Fresh per test
        configureGradleSettings()
    }

    @After
    fun tearDown() {
        myProject.dispose()
        cleanupResources()
    }
}

class MyTests : GradleImportingTestCase() {
    @Test
    fun test1() { /* fresh myProject from setUp */ }

    @Test
    fun test2() { /* fresh myProject from setUp */ }
}
```

**Pros:** Multiple tests in one class, proper lifecycle
**Cons:** Need JUnit 4 or add JUnit 5 `@BeforeEach`/`@AfterEach`

#### Solution 3: Parameterized Tests (Data-Driven)

```kotlin
@RunWith(Parameterized::class)
class GradleVersionTest(private val gradleVersion: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf("7.0", "8.0", "9.0")
    }

    @Test
    fun testWithVersion() {
        // Runs 3 times, fresh project each time
    }
}
```

#### Solution 4: Manual Project Creation

```kotlin
@TestApplication
class MyTests {
    @Test
    fun test1() = runBlocking {
        val project = createTestProject()
        try {
            // Test logic
        } finally {
            Disposer.dispose(project)
        }
    }

    @Test
    fun test2() = runBlocking {
        val project = createTestProject()  // Fresh!
        try {
            // Test logic
        } finally {
            Disposer.dispose(project)
        }
    }
}
```

### Recommendation

**For new tests:** Use Solution 1 (one test per class). It's the simplest and matches IntelliJ's most common pattern.

**For existing tests with multiple methods:** Either:
- Split into separate classes
- Switch to JUnit 4 style with setUp/tearDown
- Manually create/dispose projects

---

## Logging and Debugging

### Basic Logging Setup

```kotlin
import com.intellij.openapi.diagnostic.Logger

class MyTest {
    private val LOG = Logger.getInstance(MyTest::class.java)

    @Test
    fun myTest() {
        LOG.info("Test starting")
        LOG.debug("Debug details")
        LOG.warn("Warning message")
        LOG.error("Error occurred", exception)
    }
}
```

### Capturing Gradle Events

```kotlin
val gradleListener = object : ExternalSystemTaskNotificationListenerAdapter() {
    override fun onStart(id: ExternalSystemTaskId) {
        LOG.info("[Gradle Task] Started: ${id.projectSystemId.readableName}")
    }

    override fun onEnd(id: ExternalSystemTaskId) {
        LOG.info("[Gradle Task] Ended: ${id.projectSystemId.readableName}")
    }

    override fun onStatusChange(event: ExternalSystemTaskNotificationEvent) {
        LOG.info("[Gradle Event] ${event.description}")
    }

    override fun onTaskOutput(id: ExternalSystemTaskId, text: String, stdOut: Boolean) {
        val outputType = if (stdOut) "OUT" else "ERR"
        LOG.info("[Gradle $outputType] ${text.trim()}")
    }
}

ExternalSystemProgressNotificationManager.getInstance()
    .addNotificationListener(gradleListener)

try {
    // Gradle operations
} finally {
    ExternalSystemProgressNotificationManager.getInstance()
        .removeNotificationListener(gradleListener)
}
```

### What You Can/Cannot Capture

✅ **Can Capture:**
- Sync task lifecycle (start/end)
- Status change events
- Some task output

❌ **Cannot Easily Capture:**
- Full Gradle build console output (runs in separate process)
- Gradle daemon logs (written to `~/.gradle/daemon/`)

### Finding Logs

Logs are written to:
```
build/idea-sandbox/IC-2025.1/log-test/idea.log
```

Search for your logger name:
```bash
grep "MyTest" build/idea-sandbox/IC-2025.1/log-test/idea.log
```

### Debugging Hangs

If your test hangs, check:

1. **Is it in dumb mode?**
   ```kotlin
   LOG.info("Dumb mode: ${DumbService.isDumb(project)}")
   ```

2. **Did sync callback fire?**
   ```kotlin
   LOG.info("Sync callback: SUCCESS/FAILURE")
   ```

3. **Is project disposed?**
   ```kotlin
   LOG.info("Project disposed: ${project.isDisposed}")
   ```

4. **Check test results:**
   ```bash
   cat build/test-results/test/TEST-*.xml
   ```

For complete logging patterns, see [HOW-TO.md](HOW-TO.md).

---

## Complete Working Examples

### Example 1: Basic Gradle Sync Test

```kotlin
@TestApplication
class GradleProjectSyncTest {
    private val LOG = Logger.getInstance(GradleProjectSyncTest::class.java)
    private val tempPathFixture = tempPathFixture()
    private val projectFixture = projectFixture()

    @Test
    fun `test gradle sync completes successfully`() = runBlocking {
        val tempDir = tempPathFixture.get()
        val project = projectFixture.get()

        LOG.info("=== Starting Gradle sync test ===")

        // 1. Setup scenario
        setupScenario("simple-kotlin", tempDir)

        // 2. Link Gradle settings
        linkGradleProject(project, tempDir.toCanonicalPath())

        // 3. Sync with callback
        val syncFuture = CompletableFuture<Void>()
        val importSpec = ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
            .use(ProgressExecutionMode.MODAL_SYNC)
            .callback(object : ExternalProjectRefreshCallback {
                override fun onSuccess(externalProject: DataNode<ProjectData>?) {
                    LOG.info("Sync SUCCESS")
                    syncFuture.complete(null)
                }
                override fun onFailure(errorMessage: String, errorDetails: String?) {
                    LOG.error("Sync FAILURE: $errorMessage")
                    syncFuture.completeExceptionally(RuntimeException(errorMessage))
                }
            })
            .build()

        ExternalSystemUtil.refreshProject(tempDir.toCanonicalPath(), importSpec)

        // 4. Wait for sync
        withTimeout(120_000) {
            syncFuture.await()
        }

        // 5. Wait for indexing
        IndexingTestUtil.suspendUntilIndexesAreReady(project)

        // 6. Verify
        val gradleSettings = GradleSettings.getInstance(project)
        Assertions.assertEquals(1, gradleSettings.linkedProjectsSettings.size)

        LOG.info("=== Test completed ===")
    }

    private fun linkGradleProject(project: Project, projectPath: String) {
        val settings = GradleSettings.getInstance(project)
        val projectSettings = GradleProjectSettings().apply {
            externalProjectPath = projectPath
            distributionType = DistributionType.DEFAULT_WRAPPED
            gradleJvm = "#JAVA_HOME"  // Critical for tests!
        }
        settings.linkedProjectsSettings = setOf(projectSettings)
    }

    private fun setupScenario(scenarioName: String, tempDir: Path) {
        val scenarioDir = File("scenarios/$scenarioName")
        scenarioDir.copyRecursively(tempDir.toFile(), overwrite = true)
        copyGradleWrapper(tempDir.toFile())
    }

    private fun copyGradleWrapper(targetDir: File) {
        val projectRoot = File(System.getProperty("user.dir"))
        File(projectRoot, "gradlew").copyTo(File(targetDir, "gradlew"), overwrite = true)
        File(projectRoot, "gradlew.bat").copyTo(File(targetDir, "gradlew.bat"), overwrite = true)
        // ... copy wrapper jar and properties
    }
}
```

### Example 2: PSI Resolution Test

See [ExperimentalGradleTest.kt](src/test/kotlin/org/gradle/ide/smoke/ExperimentalGradleTest.kt) for the complete implementation with:
- Gradle sync with proper waiting
- PSI file resolution
- Element navigation
- Comprehensive logging

---

## Common Pitfalls

### 1. ❌ Not Waiting for Indexing After Sync

```kotlin
// ❌ Wrong
syncFuture.await()
// Test immediately - PSI not ready!

// ✅ Correct
syncFuture.await()
IndexingTestUtil.suspendUntilIndexesAreReady(project)
// NOW test
```

### 2. ❌ No Gradle JVM Configuration

```kotlin
// ❌ Wrong - sync fails silently
GradleProjectSettings().apply {
    externalProjectPath = projectPath
}

// ✅ Correct
GradleProjectSettings().apply {
    externalProjectPath = projectPath
    gradleJvm = "#JAVA_HOME"  // Required!
}
```

### 3. ❌ Forgetting Timeout

```kotlin
// ❌ Wrong - hangs forever on failure
project.waitForSmartMode()

// ✅ Correct
withTimeout(60_000) {
    project.waitForSmartMode()
}
```

### 4. ❌ Sharing Fixtures Across Tests

```kotlin
// ❌ Wrong
class MyTest {
    private val project = projectFixture()

    @Test fun test1() { /* uses project */ }
    @Test fun test2() { /* ERROR: project disposed! */ }
}

// ✅ Correct - one test per class
class MyTest1 {
    private val project = projectFixture()
    @Test fun test() { /* uses project */ }
}

class MyTest2 {
    private val project = projectFixture()  // Fresh!
    @Test fun test() { /* uses project */ }
}
```

### 5. ❌ Not Cleaning Up Listeners

```kotlin
// ❌ Wrong - memory leak
val listener = MyListener()
ExternalSystemProgressNotificationManager.getInstance()
    .addNotificationListener(listener)
// Forgot to remove!

// ✅ Correct
val listener = MyListener()
ExternalSystemProgressNotificationManager.getInstance()
    .addNotificationListener(listener)
try {
    // Operations
} finally {
    ExternalSystemProgressNotificationManager.getInstance()
        .removeNotificationListener(listener)
}
```

### 6. ❌ Logging Control-Flow Exceptions

```kotlin
// ❌ Wrong - IntelliJ's test framework will fail
catch (e: TimeoutCancellationException) {
    LOG.error("Timeout!", e)  // Don't log control-flow exceptions!
    throw e
}

// ✅ Correct
catch (e: TimeoutCancellationException) {
    LOG.error("Timeout occurred")  // Log message only
    throw IllegalStateException("Timeout", e)  // Wrap in non-control-flow exception
}
```

---

## Reference Documentation

### In This Repository

- **[SOLUTION.md](SOLUTION.md)** - Complete problem-solving journey from hanging test to working solution
- **[HOW-TO.md](HOW-TO.md)** - Detailed logging and debugging guide
- **[GRADLE_SYNC_ALTERNATIVES.md](GRADLE_SYNC_ALTERNATIVES.md)** - All approaches to wait for Gradle sync
- **[GRADLE_SYNC_GUIDE.md](GRADLE_SYNC_GUIDE.md)** - Quick reference for sync patterns
- **[SOURCE_FILES_REFERENCE.md](SOURCE_FILES_REFERENCE.md)** - IntelliJ Platform source file locations
- **[INTELLIJ_TEST_PATTERNS.md](INTELLIJ_TEST_PATTERNS.md)** - How IntelliJ's own tests work

### IntelliJ Platform Source Files

Key files explored in `intellij-community/`:

**Gradle Sync:**
- `/plugins/gradle/src/org/jetbrains/plugins/gradle/service/project/open/GradleProjectImportUtil.kt` - `linkAndSyncGradleProject()` implementation
- `/platform/external-system-api/src/com/intellij/openapi/externalSystem/importing/ImportSpecBuilder.java` - Import specification builder
- `/platform/external-system-impl/testSrc/com/intellij/openapi/externalSystem/util/ExternalSystemOperationTestUtil.kt` - `awaitProjectActivity()` (internal)

**Test Infrastructure:**
- `/plugins/gradle/testSources/org/jetbrains/plugins/gradle/testFramework/GradleBaseTestCase.kt` - Modern test base class
- `/plugins/gradle/testSources/org/jetbrains/plugins/gradle/importing/GradleImportingTestCase.kt` - Legacy test base
- `/plugins/gradle/testSources/org/jetbrains/plugins/gradle/testFramework/fixtures/impl/GradleTestFixtureImpl.kt` - Test fixture implementation

**Dumb Mode:**
- `/platform/core-api/src/com/intellij/openapi/project/DumbService.kt` - Dumb mode API
- `/platform/core-api/src/com/intellij/openapi/project/dumb.kt` - `waitForSmartMode()` implementation

**Testing Utilities:**
- `/platform/testFramework/src/com/intellij/testFramework/IndexingTestUtil.kt` - Indexing test utilities
- `/platform/testFramework/src/com/intellij/testFramework/TestObservation.kt` - Activity tracking

---

## Key Takeaways

1. **`linkAndSyncGradleProject()` is asynchronous** - it doesn't wait for completion
2. **Always wait for ALL activities** - sync callback + indexing + smart mode
3. **Configure Gradle JVM** - use `gradleJvm = "#JAVA_HOME"` in tests
4. **One test per class** - avoid fixture sharing issues
5. **Add timeouts everywhere** - prevent tests from hanging forever
6. **Use proper logging** - `Logger.getInstance()`, not `println()`
7. **Clean up resources** - remove listeners in `finally` blocks

---

## Success Metrics

With these patterns, your tests should:
- ✅ Complete in ~1-2 seconds (not hang indefinitely)
- ✅ Have clear logs showing each step
- ✅ Properly clean up resources
- ✅ Work reliably across multiple runs
- ✅ Provide meaningful error messages on failure

---

## Getting Help

If tests still hang or fail:

1. **Check logs:** `build/idea-sandbox/IC-2025.1/log-test/idea.log`
2. **Enable more logging:** Add `LOG.info()` at each step
3. **Check test results:** `build/test-results/test/TEST-*.xml`
4. **Verify project state:** Log `project.isDisposed` and `DumbService.isDumb(project)`
5. **Check fixture lifecycle:** Ensure each test gets fresh fixtures

---

**Document Version:** 1.0
**Last Updated:** Based on solving hanging test issue in November 2024
**Project:** ide-smoke-tests