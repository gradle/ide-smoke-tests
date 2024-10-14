# IDE smoke tests

This repository contains a set of sample projects to manually verify the functionality of the IDE.
The scenarios and the verication instructions are provided in the source files.

## Workflow

The instructions on how to perform the smoke tests are declared as TODO comments in the sample projects.
Also, there's a [GitHub Action](.github/workflows/create_issue_with_test_plan.yml) set up to initiate a new round of testing.
The action parses the TODO actions from the repository and generates a detailed test plan with the `generateTestPlan.groovy` script.
Then, the action opens a new issue with the test plan.
The issue is posted on the `#bt-support-events` channel. It is the responsibility of the BT support team to execute the test plan.

## Render test plan issue locally

The `generateTestPlan.groovy` script contains a Groovy script that generates a test plan. To generate the test plan manually, run the following command:

```sh
groovy generateTestPlan.groovy
```
