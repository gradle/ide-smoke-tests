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
    println "# Test Plan"

    allScenarios.each {project, scenarios ->
        println ""
        println "## $project"
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