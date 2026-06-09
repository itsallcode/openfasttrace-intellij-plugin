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

### How Does the Project Keep Gradle Dependency Metadata Predictable?

The project needs predictable Gradle dependency resolution for static analysis
and reproducible local and CI builds. It also needs a lightweight way to check
whether newer dependency, build-plugin, or Gradle versions are available without
making ordinary builds fail when an update exists.

This decision is architecture-relevant because it impacts:

* build reproducibility
* dependency maintenance effort
* SonarCloud analysis compatibility
* the project's dependency-policy boundary for build plugins

We considered the following alternatives:

1. Use Gradle dependency verification metadata only.

   This would verify artifact integrity, but it would require reviewing and
   maintaining checksums for each resolved artifact. The project explicitly does
   not want that review burden at this stage.

1. Use Gradle dependency locking and a custom version-check task.

   This would keep the dependency set predictable, but a custom version-check
   task would add project-specific Gradle logic for behavior that already has a
   maintained plugin solution.

1. Use Gradle dependency locking and the Gradle Versions Plugin.

   This keeps normal dependency resolution locked through Gradle's built-in
   mechanism and delegates update discovery to a maintained build plugin that
   can be run on demand by maintainers.

#### Gradle Dependency Maintenance Uses Locks And Versions Plugin
`dsn~gradle-dependency-maintenance-uses-locks-and-versions-plugin~1`

The Gradle build uses dependency locking for predictable dependency resolution
and the Gradle Versions Plugin for on-demand dependency, build-plugin, and
Gradle wrapper update reports.

Rationale:

Gradle dependency locking satisfies the need for stable resolved dependency
versions without enabling full dependency verification metadata. The Gradle
Versions Plugin is an approved build-only dependency for GH-45 and avoids
custom version-check logic in the build script.

Comment:

The version-check task is informational and stays outside the standard `check`
lifecycle. Available updates change over time, so update discovery must not
make normal local or CI builds unstable.

Needs: bld

Tags: Build, Gradle, Dependencies, SonarCloud

### How Does the Project Monitor Dependency Vulnerabilities?

The project needs vulnerability monitoring for Gradle dependencies without making local and CI builds depend on an external vulnerability-audit service during every run.

This decision is architecture-relevant because it impacts:

* reliability of local and CI builds
* trust in dependency vulnerability monitoring
* time-to-feedback for vulnerable dependency findings

We considered the following alternatives:

1. Keep OSS Index in the Gradle build.

   This preserves immediate feedback, but OSS Index quota, authentication, and service availability issues can block development without revealing anything about the project's dependency security state.

1. Keep OSS Index in the build but make audit failures non-fatal.

   This avoids blocked builds, but it keeps a fragile build integration without a strict enforcement benefit.

1. Remove OSS Index from the Gradle build and rely on GitHub Dependabot alerts.

   This removes the immediate build-time vulnerability gate, but it keeps vulnerability monitoring in GitHub without making every build depend on OSS Index availability.

#### Dependency Vulnerability Monitoring Uses Dependabot
`dsn~dependency-vulnerability-monitoring-uses-dependabot~1`

The Gradle build does not run OSS Index dependency auditing. Dependency vulnerability monitoring happens through GitHub Dependabot alerts.

Rationale:

Dependabot lacks the immediate local build feedback that OSS Index provided, but it gives the project a vulnerability check without blocking local and CI builds on OSS Index quotas, credentials, or service availability.

Comment:

Builds remain responsible for ordinary compilation, tests, tracing, plugin verification, and static analysis. Dependency verification metadata still protects artifact integrity, but it is not a vulnerability scanner.

We add no "needs" here, since we cannot trace into dependabot.

Tags: Build, Security, Dependabot

## Test Framework Decisions

### Which JUnit Baseline Does the Plugin Use?

The project needs a test setup that follows the preferred JUnit 5 direction from the quality requirements while still working with the IntelliJ Platform light test infrastructure used for MVP verification.

This decision is architecture-relevant because it impacts:

* consistency of the project test stack
* compatibility with JetBrains-provided test base classes
* long-term maintainability of the automated test setup

We considered the following alternatives:

1. Using only JUnit 5 artifacts in the test classpath.

   This is the preferred direction for ordinary project tests, but the IntelliJ Platform `BasePlatformTestCase` hierarchy still depends on the legacy JUnit `TestCase` API. Using only JUnit 5 breaks compilation of the light platform test base used by the plugin tests.

1. Using JUnit 5 as the test platform and adding JUnit 4 only as a compatibility dependency for IntelliJ light platform tests.

   This keeps the primary test runner and authored tests on JUnit 5 while limiting the legacy dependency to the narrow compatibility gap imposed by the JetBrains test framework.

#### IntelliJ Light Tests Keep a Narrow JUnit 4 Compatibility Dependency
`dsn~intellij-light-tests-keep-junit4-compatibility-dependency~1`

The plugin uses JUnit 5 as the primary automated test platform and keeps JUnit 4 only as a compatibility dependency required by IntelliJ Platform light test base classes such as `BasePlatformTestCase`.

Rationale:

This keeps the authored test suite aligned with the preferred JUnit 5 direction while avoiding custom wrappers or heavier test infrastructure just to work around a dependency inherited from the JetBrains test framework.

Comment:

The JUnit 4 dependency is not a license to write new project tests against JUnit 4 APIs by default. It exists only because the IntelliJ light test infrastructure still exposes that compatibility requirement.

Needs: tst

Tags: Test, JUnit, IntelliJ Platform
