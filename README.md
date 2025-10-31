# IDE Smoke Tests

## Project Overview
This project tests the interactions between Gradle and IntelliJ IDEA, verifying that the user experience of using Gradle in IntelliJ IDEA is what we expect.

**Features tested:**
- **Code completion** in Gradle Kotlin DSL scripts
- **Documentation accessibility** (hover/quick doc)
- **Navigation** from build scripts to Gradle API sources

## Technology Stack
- **Java 25** (Latest LTS release)
- **JUnit 4.13.2** (Required by IntelliJ Platform test framework)
- **Gradle 9.2.0** with Kotlin DSL
- **IntelliJ Platform 2024.1** Test Framework

## Project Structure
```
ide-smoke-tests/
├── build.gradle.kts          # Main build configuration with IntelliJ Platform plugin
├── settings.gradle.kts        # Project settings
└── src/test/java/org/example/ide/smoke/
    ├── GradleIdeTestBase.java           # Base test class with common utilities
    ├── GradleCodeCompletionTest.java    # Tests for code completion features
    ├── GradleDocumentationTest.java     # Tests for documentation accessibility
    └── GradleNavigationTest.java        # Tests for navigation to source
```

## Quick Start

### Prerequisites
- Java 25 (install via SDKMAN: `sdk install java 25-tem`)
- Gradle 9.2+ (included via wrapper)

### Running Tests

**Run all tests:**
```bash
./gradlew test
```

**Run a specific test class:**
```bash
./gradlew test --tests "org.example.ide.smoke.GradleCodeCompletionTest"
```

**Run a specific test method:**
```bash
./gradlew test --tests "org.example.ide.smoke.GradleCodeCompletionTest.testBasicPluginCompletion"
```

**Run in IntelliJ IDEA:**
1. Open the project in IntelliJ IDEA
2. Wait for Gradle sync to complete
3. Right-click on a test class and select "Run"

### Useful Gradle Tasks

- `./gradlew tasks` - Show all available tasks
- `./gradlew build` - Build the project
- `./gradlew runIde` - Launch IntelliJ IDEA with your test plugin loaded
- `./gradlew verifyPlugin` - Verify plugin compatibility
- `./gradlew printVersion` - Print project info (Gradle, Java versions)

## Writing Tests

### 1. Create a new test class

Extend `GradleIdeTestBase` for common setup:

```java
package org.gradle.ide.smoke;

import org.junit.Test;

public class MyNewTest extends GradleIdeTestBase {

    @Test
    public void testSomething() {
        // Use Java text blocks for clean multi-line content
        createBuildFile("""
                plugins {
                    <caret>
                }
                """);

        // Your test logic here
        var element = myFixture.getElementAtCaret();
        assertNotNull("Element should exist", element);
    }
}
```

### 2. Test Helper Methods

Available from `GradleIdeTestBase`:
- `createBuildFile(String content)` - Creates a build.gradle.kts file for testing
- `createSettingsFile(String content)` - Creates a settings.gradle.kts file
- `myFixture.complete()` - Trigger code completion
- `myFixture.getElementAtCaret()` - Get element at caret position
- `myFixture.getLookupElementStrings()` - Get completion suggestions

### 3. Common Test Patterns

**Code Completion:**
```java
@Test
public void testCompletion() {
    createBuildFile("""
        dependencies {
            <caret>
        }
        """);

    myFixture.complete(CompletionType.BASIC);
    var lookupStrings = myFixture.getLookupElementStrings();

    assertNotNull(lookupStrings);
    assertTrue(
        "Should suggest 'implementation'",
        lookupStrings.contains("implementation")
    );
}
```

**Documentation:**
```java
@Test
public void testDocumentation() {
    createBuildFile("""
        repositories {
            <caret>mavenCentral()
        }
        """);

    var element = myFixture.getElementAtCaret();
    assertNotNull("Element should exist", element);

    // Test that documentation is available
    var providers = DocumentationProvider.EP_NAME.getExtensionList();
    var hasDoc = providers.stream()
        .anyMatch(p -> p.generateDoc(element, element) != null);

    assertTrue("Should have documentation", hasDoc);
}
```

**Navigation:**
```java
@Test
public void testNavigation() {
    createBuildFile("""
        tasks.<caret>register("myTask")
        """);

    var element = myFixture.getElementAtCaret();
    var references = element.getReferences();

    assertTrue("Should have references", references.length > 0);

    var resolved = references[0].resolve();
    assertNotNull("Should resolve to definition", resolved);
}
```

## Java 25 Features Used

This project leverages modern Java 25 features:
- **Text Blocks** - For clean multi-line Gradle script content in tests
- **var keyword** - For concise local variable declarations
- **Pattern Matching** - For instanceof checks (when needed)
- **Records** - Can be used for test data structures
- **Enhanced Switch** - For cleaner control flow

## JUnit 4 Best Practices

While using JUnit 4 (required by IntelliJ Platform), we leverage modern Java 25 features:
- Clear assertion messages: `assertNotNull("Element should exist", element)`
- Descriptive test method names: `testNavigateToPluginMethod()`
- `@Before/@After` for test lifecycle management
- `@Test(expected = Exception.class)` for exception testing
- Test can be organized using test suites if needed

## Troubleshooting

### Build fails with plugin compatibility error
- Check Gradle version: `./gradlew --version`
- Update IntelliJ Platform plugin version in build.gradle.kts
- Ensure IntelliJ version in dependencies matches available versions

### Tests fail to find IDE components
- Ensure you've synced Gradle project in IntelliJ
- Check that IntelliJ Platform dependencies are downloaded
- Try invalidating caches: File > Invalidate Caches > Invalidate and Restart

### Code completion doesn't work in tests
- Verify that the Gradle plugin is properly loaded
- Check that test sandbox is prepared: `./gradlew prepareSandbox`
- Ensure your test build script content is valid Kotlin DSL

### Java 25 not found
- Install Java 25 using SDKMAN: `sdk install java 25-tem`
- Or download from [Adoptium](https://adoptium.net/)
- Verify installation: `java -version`

## Next Steps

1. **Expand test coverage**: Add more test scenarios for each feature
2. **Add more DSL elements**: Test different Gradle plugins and configurations
3. **Test different IDE versions**: Update IntelliJ Platform version in build.gradle.kts
4. **Add integration tests**: Test more complex multi-file scenarios
5. **Parameterized tests**: Use JUnit 4 `Parameterized` runner to test multiple Gradle versions
6. **CI/CD setup**: Configure GitHub Actions to run tests automatically

## Example Test Run

```bash
$ ./gradlew test

> Task :test

GradleCodeCompletionTest > Should suggest plugins in plugins block PASSED
GradleCodeCompletionTest > Should suggest dependency configurations PASSED
GradleCodeCompletionTest > Should suggest task methods in tasks block PASSED
GradleCodeCompletionTest > Should suggest repository methods in repositories block PASSED

GradleDocumentationTest > Should provide documentation for plugins DSL PASSED
GradleDocumentationTest > Should provide documentation for dependencies configuration PASSED
GradleDocumentationTest > Should provide documentation for task registration PASSED
GradleDocumentationTest > Should provide documentation for repository methods PASSED

GradleNavigationTest > Should navigate to plugin method definition PASSED
GradleNavigationTest > Should navigate to dependencies configuration PASSED
GradleNavigationTest > Should navigate to TaskContainer PASSED
GradleNavigationTest > Should navigate to register method PASSED
GradleNavigationTest > Should navigate to repository method PASSED

BUILD SUCCESSFUL in 42s
```

## Resources
- [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [IntelliJ Platform Gradle Plugin](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)
- [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Java 25 Documentation](https://openjdk.org/projects/jdk/25/)
