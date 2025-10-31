# IDE Smoke Tests

This project tests the interactions between Gradle and IntelliJ IDEA.

It aims to verify that the user-experience of using Gradle in IntelliJ IDEA is what we expect.
Examples of these are:
 - Code completion
 - Documentation accessibility
 - Navigation into the Gradle codebase from script
And such features.

## Structure

This project uses the IDEA platform test framework to run tests against different versions of IntelliJ IDEA and Gradle.
