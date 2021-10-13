# IDE smoke tests

## Setup Test Environment

- Install required IDEs
  - Install IntelliJ IDEA, EAP and latest stable release
- Clone this repository
- Update Gradle wrappers to the latest snapshot version
- Clean projects
  - `git clean -fdx`


## Run test scenarios

Repeat instructions for all test projects in this repositories. The test projects are the numbered folders in the root of this repository.

- Import test project into IDE
- Wait for the sync to finish

## Features to verify

- Project import
  - outcome
  - sync output
     - deprecation warnings
  - popups
- Build script editing
  - syntax highlight
  - code navigation
  - code completion
    - extensions
    - extensions from local/remote plugins
- Task execution
  - compile
  - verfication
  - application
- Source code editing
  - highlighting, navigation, code completion
  - test sources are marked as such
  - use correct task when running tests
    - ide offers selection of `integTest`
