plugins {
    // TODO (scenario) Can navigate to plugin implementation from generated plugin accessors
    //   Instructions
    //   - cmd+click on the plugin accessor
    `buildsrc-java-plugin` // Known issue: IDEA navigates to generated accessor and not to the declaration
    `buildsrc-groovy-plugin`
    `buildsrc-kotlin-plugin`
}

// TODO (scenario) Class from buildSrc is available in the build script
//   Instructions
//   - Verify whether syntax highlighting works
//   - Verify whether cmd+click on the method takes you to the declaration
BuildUtils.printDiagnostics("build.gradle.kts")
