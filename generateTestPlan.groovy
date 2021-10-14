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