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
    // TODO add year/week parameters
    // TODO add assignees
    println """
        ---
        name: Test Plan 2021.41
        about: Test plan for manual IDE smoke tests
        title: Test Plan 2021.41
        labels: test-iteration
        ---

        # Test Plan

        ## Prerequisites

        - Install required IDEs
        - Install IntelliJ IDEA, EAP and latest stable release
        - Clone this repository
        - Update Gradle wrappers to the latest snapshot version
        - Clean projects
          - `git clean -fdx`

        ## Document components used in the test plan:
        
        - [ ] Operating System:
        - [ ] Gradle version: 
        - [ ] IntelliJ Idea EAP version (with build number):
        - [ ] IntelliJ Idea Stable version (with build number): 

        ## Verify scenarios

        The sample projects are located in the repository root and their name all starts with a number. For each project, execute the following steps:
        - Start the EAP version of IDEA
        - Import the sample project into IDEA and wait for the project sync to finish
        - Use text search to locate the scenario (listed below) in the build
        - Verify the functionality in the IDE
        - Check off the verified scenario in the list below.

        In case of a failure
        - Verify the same scenario in the latest released Idea version
        - Open an issue at https://youtrack.jetbrains.com
        - Add a `// Known issue: https://link-to-new-issue` comment to corresponding file in the sample project
        - add a X (red cross emoji) to the list below
    """.stripIndent(8).strip()

    allScenarios.each {project, scenarios ->
        println ""
        println "## Scenarios in `$project`"
        println ""
        scenarios.each { scenario ->
            println "- [ ] $scenario"
        }
    }
}

boolean isSourceFile(File file) {
    if (file.directory) {
        return false
    } else {
        ['.gradle.kts', '.gradle', '.groovy', '.kt', '.java'].find { file.name.endsWith(it) }
    }
}