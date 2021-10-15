/**
 * Generates instructions on how to execute manual IDE smoke tests.
 *
 * The script iterates through all (Java, Groovy, Kotlin and Gradle) source files, grabs the comments starting with
 * `// TODO (scenario)` and outputs the list of scenarios along with instructions on how to perform the
 * tests.
 *
 * The output uses the GitHub Markup format, so the result can be copy-pasted into a GitHub issue.
 */

File workingDir = new File(System.getProperty('user.dir'))
Map<String, List<String>> allScenarios = [:]
workingDir.eachFile {File sampleDir ->
    if (sampleDir.directory && sampleDir.name.matches('\\d+-.*')) {
        String sampleName = sampleDir.name.replaceFirst('\\d+-', '')
        List scenarios = []
        sampleDir.eachFileRecurse {
           if (isSourceFile(it)) {
               it.text.eachLine { line ->
                   if (line.contains("TODO (scenario)")) {
                       scenarios += line.replaceFirst('TODO \\(scenario\\)', '').replaceFirst('//', '').replaceFirst('\\s+', '')
                   }
               }
           }
        }
        allScenarios[sampleName] = scenarios
    }
}

renderTestPlan(allScenarios)

void renderTestPlan(Map<String, List<String>> allScenarios) {
    Calendar calendar = Calendar.getInstance()
    def year = calendar.get(Calendar.YEAR).getClass()
    def weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
    println """
        ---
        name: Test Iteration ${year}.${weekOfYear}
        about: Instructions to execute manual IDE smoke tests
        title: Test Iteration ${year}.${weekOfYear}
        labels: test-iteration
        ---

        # Test Plan

        ## 1. Set up local test environment

        - Assing this ticket to yourself and move it to the the _In Progress_ column.
        - Install IntelliJ IDEA, EAP and latest stable release
          - Tip: you can use the [Toolbox App](https://www.jetbrains.com/toolbox-app/) to manage multiple installations
        - Clone [this](https://github.com/gradle/ide-smoke-tests) repository
        - Update Gradle wrappers in all sample projects to the latest snapshot
        - Run `git clean -fdx` to remove build artifacts from all sample projects

        ## 2. Document components:
        
        - [ ] Operating System:
        - [ ] Gradle version: 
        - [ ] IntelliJ Idea EAP version (with build number):
        - [ ] IntelliJ Idea Stable version (with build number): 

        ## 3. Verify scenarios

        The sample projects are located in the numbered folders in the repository root. For each project, follow the steps below:

        - Launch IDEA EAP
        - Import the sample project into IDEA and wait for the project sync to finish
        - Use text search to locate the scenario (listed below) in the build and follow the instructions
        - Check off the verified scenario in the list below

        In case of a failure
        - Verify the same scenario in the latest released IDEA version
        - Open an issue at https://youtrack.jetbrains.com
        - Link the issue in the sample project:
          -  Add the following comment to the failing line `// Known issue: https://youtrack.jetbrains.com/issue/IDEA-123456`
        - add a X (red cross emoji) to the list below and link the created issue there as well
    """.stripIndent(8).strip()

    allScenarios.each {project, scenarios ->
        println ""
        println "### Scenarios in `$project`"
        println ""
        scenarios.each { scenario ->
            println "- [ ] $scenario"
        }
    }
    println """
        ## 4. Finalize
        
        - Close this issue
        - Consider improving the test plan
          - If you find that some scenarios are incorrect, redundant, or missing, please provide a PR and ask the @bt-support-team for a review
          - You can update the issue template here: https://github.com/gradle/ide-smoke-tests/blob/main/generateTestPlan.groovy
    """.stripIndent(8).stripTrailing()
}

boolean isSourceFile(File file) {
    if (file.directory) {
        return false
    } else {
        ['.gradle.kts', '.gradle', '.groovy', '.kt', '.java'].find { file.name.endsWith(it) }
    }
}