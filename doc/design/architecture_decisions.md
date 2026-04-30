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

### How Does the Build Handle OSS Index Quota Limits?

The project uses OSS Index dependency security scanning as a build breaker so vulnerable dependencies block integration. In April 2026 Sonatype introduced quotas on the OSS Index free plan. When that quota is exceeded, OSS Index returns HTTP 429 instead of vulnerability data.

This decision is architecture-relevant because it impacts:

* reliability of local and CI builds
* trust in the dependency security gate
* clarity when a build cannot obtain an OSS Index result

We considered the following alternatives:

1. Keep every OSS Index task failure fatal.

   This preserves a strict gate, but an exceeded external quota would block development without revealing anything about the project's dependency security state.

1. Disable OSS Index or make all audit failures non-fatal.

   This would keep builds moving, but it would also remove the build-breaking protection for known vulnerable dependencies and ordinary audit failures.

1. Treat only HTTP 429 as a documented warning exception.

   This keeps vulnerability detection and ordinary audit failures fatal while letting builds continue when OSS Index does not provide results because the external quota was exceeded.

#### OSS Index Audit Continues on HTTP 429
`dsn~oss-index-audit-continues-on-http-429~1`

The Gradle build keeps OSS Index dependency auditing as a build breaker for detected vulnerabilities and non-429 audit failures. When the OSS Index audit fails because the OSS Index service returns HTTP 429, the build logs a warning that the quota or plan may need checking and continues.

Rationale:

An HTTP 429 response indicates rate limiting or quota exhaustion, not that the scanned dependency set contains known vulnerabilities. Continuing in that case prevents the external plan quota from blocking local and CI development while still making the missing security result visible in the build log.

Comment:

The HTTP 429 exception does not apply to vulnerability findings, authentication failures, ordinary connection failures, malformed responses, or other OSS Index service errors.

Needs: bld

Tags: Build, Security, OSS Index

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
