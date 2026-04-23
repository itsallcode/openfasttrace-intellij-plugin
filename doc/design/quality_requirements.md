# Quality Requirements

This chapter documents architecture-relevant quality requirements and technical quality goals.

User-facing acceptance scenarios are defined in [System Requirements](../system_requirements.md).

Terms such as `plugin`, `OpenFastTrace`, and `OFT` use the definitions from [System Requirements](../system_requirements.md).

## Code Quality

Production code and test code follow clean-code principles as defined in the Clean Code book.

The implementation prefers speaking names over explanatory comments. Comments are only acceptable when intent cannot be expressed clearly in code.

Methods stay short and keep cyclomatic complexity low. When behavior grows beyond a small, readable unit, the design is refactored into smaller collaborating types or methods.

## Dependency Policy

The plugin uses the minimum set of dependencies required for:

* building and running a JetBrains plugin
* integrating the OpenFastTrace library

Additional libraries are not allowed by default. Any new third-party dependency requires an explicit design decision and approval before it is added to the build.

## Static Analysis And Security Gates

Static code analysis runs in SonarQube Cloud and acts as a build breaker. A failing quality gate blocks integration until the reported issues are resolved or an approved exception exists.

Dependency security scanning runs with OSS Index and acts as a build breaker. The build stays free of known vulnerable dependencies.

## Testability And Coverage

Automated tests use JUnit 5 together with Hamcrest matchers.

Path coverage across the code base stays at or above 80%. Coverage below that threshold fails the build unless a documented exception is accepted in advance.

The architecture favors testable units with clear boundaries so the coverage target can be met without relying mainly on brittle UI-level tests.

Automated plugin testing follows the IntelliJ Platform test strategy and uses real platform components in a headless environment instead of extensive mocking. Most plugin behavior is verified as model-level functional tests against test data files and expected results.

Plugin tests prefer light platform tests whenever possible because they reuse the project setup and run faster. Tests for plugin logic without Java-specific PSI use fixtures or base classes equivalent to `BasePlatformTestCase`. Heavier project-scoped tests are reserved for behavior that requires a fresh project, multiple modules, or project-level services that cannot be covered with light tests.

Editor-facing features such as syntax highlighting, inspections, annotators, references, and navigation use the IntelliJ test fixtures and test data conventions. Highlighting tests store expected results in test files and verify them with the platform highlighting fixture support.

End-to-end IDE startup and workflow checks use dedicated integration tests with the IntelliJ Platform Starter and Driver infrastructure. These tests run from a separate integration test task against the built plugin distribution, verify that the plugin loads into the target IDE, and cover only a small number of critical workflows because the UI driver APIs remain slower and more brittle than model-level tests.

Continuous integration runs plugin-specific verification tasks in addition to ordinary automated tests. The build verifies plugin packaging and descriptor validity and runs IntelliJ Plugin Verifier checks against the supported IDE builds to detect binary compatibility problems early.

## Platform Compatibility

The required Java version follows the standard Java requirement of the targeted JetBrains platform version. The project does not introduce a language level or bytecode target that exceeds that platform requirement.
