# Default Test Runner Trace Result View

## Goal

Make the IntelliJ Test Runner UI the default result view for OpenFastTrace traces started from `Tools | OpenFastTrace | Trace Project` and for OpenFastTrace run configurations that do not store an explicit result-view selection.

## Scope

In scope:

* Change the global `Trace Project` action to present successful, defective, and startup-failure trace results in the IntelliJ Test Runner UI by default.
* Change new and previously unconfigured OpenFastTrace run configurations to default to the IntelliJ Test Runner UI.
* Keep plain text output available when a run configuration explicitly selects it.
* Update traced requirements, runtime design, README, and the current `0.8.0` changelog text.

Out of scope:

* Removing the plain text trace-output presenter.
* Changing OpenFastTrace trace execution, input resolution, filters, or trace-result mapping semantics.
* Adding Problems view integration, inspections, or automatic trace-on-save behavior.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [GH-40 Integrate OFT Trace into Test Runner UI](40-integrate-oft-trace-into-test-runner-ui.md)

## Strategy

Reuse the existing test-runner trace mapper and presenter. Add only the run-content wrapper needed for the global Tools-menu action, because run configurations already receive their console from IntelliJ's execution framework while the global action must explicitly show run content.

## Task List

- [x] No tracker issue was supplied; keep this descriptive changeset filename instead of creating an issue-numbered file.

### Requirements And Design

- [x] Revise the Run OFT Trace feature and output-window requirement so the Test Runner UI is the default and plain text is opt-in.
- [x] Add a global `Trace Project` default-test-runner scenario.
- [x] Replace the plain-text-default run-configuration scenario with a test-runner-default scenario and add explicit plain-text selection coverage.
- [x] Update building-block and runtime design items for the new defaults.

### Implementation

- [x] Default missing or invalid run-configuration result-view selections to `TEST_RUNNER`.
- [x] Change the global `Trace Project` action to use a test-runner run-content presenter by default.
- [x] Keep explicit plain text run-configuration execution wired to the existing plain text presenter.

### Verification

- [x] Update focused run-configuration, run-profile, and trace-action tests for the new defaults.
- [x] Add focused coverage for the global test-runner run-content presenter.
- [x] Keep the OpenFastTrace trace clean for `feat`, `req`, `scn`, `dsn`, `impl`, `utest`, and `itest` artifacts in scope.
- [x] Run `./gradlew test`.
- [x] Run `./gradlew check`.
- [x] Run `./gradlew verifyPlugin`.

### Update user documentation

- [x] Update README.md to describe Test Runner UI as the default trace result view and plain text as a run-configuration option.

## Version and Changelog Update

- [x] Keep version `0.8.0` because this refines the unreleased test-runner feature already documented for `0.8.0`.
- [x] Update the `0.8.0` changelog entry to describe the new default behavior.

`./gradlew test` passes with one skipped `OftTraceServiceTest` parameterized case.

`./gradlew traceRequirements` passes.

`./gradlew check` passes.

`./gradlew verifyPlugin` passes. The verifier still reports the existing two experimental API usages in `OftHighlightingPass`, and both configured IDE checks are compatible.
