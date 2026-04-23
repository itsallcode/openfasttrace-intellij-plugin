# Design Decisions

This chapter records important architectural decisions and their rationale.

Terms such as `plugin` and `OpenFastTrace` use the definitions from [System Requirements](../system_requirements.md).

This section elaborates the most important design decisions. Criteria for whether decisions are relevant enough to be discussed here instead of only in other design chapters are:

* the decision makes the system hard to change later
* the decision is likely to be discussed repeatedly without a documented resolution
* the decision has relevant cost implications
* the decision has relevant technical risks

## Build Tool Decisions

### Which Build Tool Does the Plugin Use?

The project needs a build setup for compiling the plugin, running automated tests, packaging the plugin, verifying plugin structure, and checking compatibility against supported JetBrains IDE builds.

This decision is architecture-relevant because it impacts:

* build and CI complexity
* compatibility verification across JetBrains IDEs
* long-term maintainability of the build
* alignment with JetBrains-supported plugin development workflows

We considered the following alternatives:

1. Using Maven as the primary build tool.

   This would match the team's usual build preference, but JetBrains plugin development support is centered on the IntelliJ Platform Gradle Plugin. Using Maven would shift packaging, IDE run configuration, compatibility verification, and related CI integration into custom project-specific build logic.

1. Using Gradle with the IntelliJ Platform Gradle Plugin.

   This follows the officially supported JetBrains workflow for plugin development. It provides direct support for development IDE runs, plugin packaging, plugin structure verification, and Plugin Verifier integration for supported IDE builds.

#### Plugin Build Uses IntelliJ Platform Gradle Plugin
`dsn~plugin-build-uses-intellij-platform-gradle-plugin~1`

The plugin build uses Gradle with the IntelliJ Platform Gradle Plugin.

Rationale:

JetBrains plugin development, packaging, compatibility verification, and cross-IDE targeting are officially supported through the IntelliJ Platform Gradle Plugin. Using that supported path keeps the build simpler and reduces custom maintenance effort compared to a Maven-based solution.

Comment:

This decision does not change the general dependency policy of the project. Additional build plugins and libraries still require the same explicit scrutiny as runtime dependencies.

Needs: bld

Tags: Build, Gradle
