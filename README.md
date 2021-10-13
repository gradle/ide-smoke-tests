# IDE smoke tests

## Setup Test Environment

- Install IntelliJ IDEA EAP
- Import project
- Check features below

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
