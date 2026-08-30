# Bob project template

This is a project template for a greenfield Java project. Given below are instructions on how to use it.

## Setting up in IntelliJ

Prerequisites: JDK 25 and the most recent IntelliJ version.

1. Open IntelliJ. If needed, select File > Close Project first.
1. Select Open, choose this project directory, and accept the defaults for any further prompts.
1. Configure the project to use **JDK 25** and the SDK default project language level.
1. Locate src/main/java/Bob.java, then right-click it and choose Run Bob.main(). If the editor shows compile errors, try restarting the IDE. The program displays the Bob banner.

**Warning:** Keep the src/main/java folder as the root folder for Java files. Tools such as Gradle expect Java files there.

## Building and validating with Gradle

The project uses the Gradle Wrapper and requires Java 25.

```text
gradlew.bat check
```

This compiles the application, runs automated tests, and checks Java,
Markdown, and Gradle formatting with Spotless. Use the following commands when
needed:

```text
gradlew.bat spotlessApply
gradlew.bat test
gradlew.bat run
```

`spotlessApply` formats files; `test` runs the automated test suite; and `run`
starts Bob. Formatting and validation rules are configured in `build.gradle`.
The project uses Palantir Java Format through Spotless because it supports the
project's four-space indentation and 120-character line-width requirements.
