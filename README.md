# IDE smoke tests

This repository contains a set of sample projects to manually verify the functionality of the IDE.
The scenarios and the verication instructions are provided in the source files.

## Render test plan

The `generateTestPlan.groovy` script contains a Groovy script that generates a test plan. The result should be copy-pasted to a GitHub issue to track the progress. To generate the test plan manually, run the following command:

```sh
    groovy generateTestPlan.groovy
```