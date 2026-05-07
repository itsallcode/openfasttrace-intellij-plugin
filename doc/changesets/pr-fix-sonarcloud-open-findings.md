# PR Fix SonarCloud Open Findings

## Goal

Close the 23 open or confirmed SonarCloud findings currently reported for the
`org.itsallcode.openfasttrace:openfasttrace-intellij-plugin` project without
adding user-visible plugin behavior.

The pull request should keep the cleanup compact: fix straightforward code
smells directly, add only narrow analyzer suppressions when a finding conflicts
with IntelliJ Platform API requirements or the documented OpenFastTrace runtime
design, and leave unrelated refactoring for later.

## Scope

In scope:

* fix the current SonarCloud findings from the open/confirmed issue view
* preserve existing completion, navigation, trace configuration, and trace
  execution behavior
* add predictable Gradle dependency metadata accepted by SonarCloud
* remove hard-coded dependency versions from `buildSrc/build.gradle.kts`
* keep implementation changes covered by existing focused tests, extending tests
  only where behavior-preserving refactoring needs protection
* avoid new third-party dependencies

Out of scope:

* changing SonarCloud quality profiles, quality gates, issue severities, or
  project permissions
* adding new plugin features or changing end-user workflows
* broad package restructuring unrelated to the reported findings
* changing OpenFastTrace, IntelliJ Platform, or OSS Index policy beyond what the
  findings require
* version or changelog updates unless implementation work introduces a
  user-visible behavior change

## Design References

* [System Requirements](../system_requirements.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)

## Strategy

Treat this as static-analysis and security-gate cleanup. The product
requirements and runtime design should remain unchanged unless a finding exposes
an actual behavior defect.

Prefer small refactorings over suppressions. Use a suppression only when the
Sonar rule conflicts with a deliberate project or platform constraint, for
example IntelliJ's `Configurable.apply()` validation contract or the explicit
plugin class loader required for OpenFastTrace `ServiceLoader` discovery. Put
the reason next to the suppression so future cleanup does not need to rediscover
the context.

## SonarCloud Findings To Close

Build and dependency metadata:

* `build.gradle.kts`: add Gradle dependency locking or verification metadata so
  dependency versions are predictable (`text:S8569`)
* `buildSrc/build.gradle.kts`: move hard-coded test dependency versions out of
  the build script (`kotlin:S6624`)

Completion and navigation:

* `OftSpecificationCompletionProvider`: split complex comment-marker detection
  and finish the quote-state branching with an explicit default path
  (`java:S1067`, `java:S126`)
* `OftTraceNavigationResolver`: make private helper methods static where they do
  not access instance state (`java:S2325`)
* `OftDeclarationNavigationElement`: remove the unnecessary
  `serialVersionUID` field (`java:S4926`)

Trace configuration and input resolution:

* `OftTraceProjectConfigurable`: preserve or log invalid-path exceptions and
  resolve the checked `ConfigurationException` findings without weakening IDE
  settings validation (`java:S1166`, `java:S1162`)
* `OftTraceInputResolver`: split long lines, reduce nested control flow when
  resolving source folders, and preserve or log invalid-path exceptions
  (`java:S103`, `java:S134`, `java:S1166`)
* `OftTraceSettingsSnapshot`: precompile the line-splitting pattern
  (`java:S4248`)

Trace execution:

* `OftTraceService`: make `buildInputHeader` static and resolve the class-loader
  finding in a way that keeps the documented OpenFastTrace `ServiceLoader`
  behavior intact (`java:S2325`, `java:S3032`)

Tests:

* `OftTraceProjectConfigurableTest`: replace boolean literal assertions with
  `assertTrue` and `assertFalse` (`java:S2701`)

## Task List

- [x] Create and checkout a new Git branch `quality/fix-sonarcloud-open-findings`

### Requirements And Design

- [x] Confirm no `doc/system_requirements.md` change is needed because the PR is
      behavior-preserving cleanup
- [x] Update `doc/design/quality_requirements.md` so committed Gradle dependency
      verification metadata is an ongoing dependency-policy requirement
- [x] Confirm no runtime design change is needed because implementation preserves
      trace execution, trace configuration, completion, and navigation semantics

### Implementation

- [x] Add Gradle dependency locking or dependency verification metadata and keep
      it committed
- [x] Move `buildSrc` dependency versions to project properties or equivalent
      centralized Gradle metadata
- [x] Refactor completion-provider comment and quote detection to satisfy
      complexity and branching rules without broadening completion activation
- [x] Refactor trace input resolution to reduce nesting, split long lines, and
      preserve exception context
- [x] Keep trace configuration validation compatible with IntelliJ settings UI
      while closing or narrowly suppressing checked-exception findings
- [x] Apply the small static-method, regex-pattern, serial-field, and assertion
      cleanups
- [x] Keep OpenFastTrace coverage tags accurate for changed implementation and
      test code

### Verification

- [x] Run `./gradlew test`
- [x] Run `./gradlew traceRequirements`
- [x] Run `./gradlew check`
- [x] Run `./gradlew buildPlugin`
- [x] Run `./gradlew verifyPlugin` and record any remaining pre-existing
      verifier findings if it is not green
- [ ] Run `./gradlew --no-configuration-cache sonar` when a valid
      Sonar token is available; otherwise rely on the SonarCloud PR analysis to
      prove the findings are closed
- [ ] Keep OSS Index audit results clean when credentials and service quota are
      available

`./gradlew --warning-mode=all buildSrc:test` passes.

`./gradlew test` passes.

`./gradlew traceRequirements` passes.

`./gradlew traceRequirements` passes after adding the dependency verification
metadata maintenance policy.

`./gradlew check` passes, including `spotlessCheck`, `traceRequirements`,
tests, and path coverage verification.

`./gradlew buildPlugin` passes.

`./gradlew verifyPlugin` still fails because of pre-existing IntelliJ Platform
experimental API usage in `OftHighlightingPass` and internal API usage in
`OftTraceRunContentOutputPresenter`. The verifier reports the plugin as
compatible otherwise.

`./gradlew sonarWholeProject -Dsonar.skip=true` was attempted but the task is
not available on this branch.

`./gradlew --no-configuration-cache sonar -Dsonar.skip=true` passes and
confirms the existing Sonar task still prepares test and JaCoCo XML inputs
before the skipped analysis.

A real Sonar upload was not run because this shell has no `SONAR_TOKEN`.

An OSS Index audit was not run because this shell has no `OSSINDEX_USERNAME` and
`OSSINDEX_TOKEN`.

## Version And Changelog Update

- [x] Do not raise the version or update the changelog unless the implementation
      introduces a user-visible behavior change
