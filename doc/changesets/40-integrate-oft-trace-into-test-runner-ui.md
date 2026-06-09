# GH-40 Integrate OFT Trace into Test Runner UI

## Goal

Let users run an OpenFastTrace run configuration and inspect the structured trace result in IntelliJ's built-in test runner UI instead of only reading the plain text trace output.

The implementation should preserve the existing plain text output as the default result view while adding a run-configuration option that presents trace results as source-file suites, specification-item tests, and trace-link sub-tests with pass/fail status and navigation back to the source.

## Scope

In scope:

* Add a result-view option to the `OpenFastTrace` run configuration editor for plain text output or IntelliJ test runner output.
* Preserve the existing plain text output behavior for `Tools | OpenFastTrace | Trace Project` and for run configurations unless the user selects the test runner view.
* Keep the structured `Trace` object produced by `OftTraceService` available to output presenters.
* Implement an `OftTraceOutputPresenter` that feeds trace results into `SMTRunnerConsoleView`.
* Build the test runner hierarchy by source file, specification item, and incoming or outgoing trace link.
* Map clean specification items and links to passed tests and defective items or links to failed tests.
* Show item and link status labels in brackets, for example `(covered)` and `(orphaned)`.
* Mark links as incoming or outgoing as required by GH-40.
* Reuse `OftTraceNavigationResolver` so test runner nodes can navigate to specification declarations and source-side coverage tags.

Out of scope:

* Replacing the existing plain text output tab for the global `Trace Project` action.
* Adding Problems view integration, editor inspections, inline annotations, or automatic trace-on-save behavior.
* Debugging OpenFastTrace execution.
* Remote or external-process OFT execution.
* New third-party dependencies beyond IntelliJ Platform APIs and the existing OpenFastTrace library.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [GH-12 Run OFT trace from the plugin](12-run-oft-trace-from-plugin.md)
* [GH-37 Introduce Run Configurations for OpenFastTrace](37-introduce-run-configurations.md)

## Strategy

Reuse IntelliJ's SM test runner APIs for the structured presentation instead of parsing the plain text report or building a custom tree widget. The OpenFastTrace service already imports, links, and traces the project into a structured `Trace`, so the result model should carry that structure to the presenter while still keeping the rendered text report for the existing console presenter.

Keep presentation selection at the run-configuration layer. The global `Trace Project` action remains a simple text-output workflow, while `OftRunProfileState` selects the text or test-runner presenter based on the saved run-configuration option.

## Task List

- [ ] Create and checkout a new Git branch `feat/40-integrate-oft-trace-into-test-runner-ui`

### Requirements And Design

- [x] Update `doc/system_requirements.md` with a user requirement for selecting the trace result view in an `OpenFastTrace` run configuration.
- [x] Add system scenarios for showing trace results in the test runner by source file, specification item, and trace link.
- [x] Add system scenarios for test-runner pass/fail mapping, bracketed status labels, incoming/outgoing link markers, and navigation from result nodes to source.
- [x] Keep the existing plain text trace-output requirements and scenarios valid for the global `Trace Project` action and default run-configuration behavior.
- [x] Stop and ask user for a review of the system requirements.
- [x] Update `doc/design/solution_strategy.md` to document reuse of IntelliJ's SM test runner infrastructure for structured trace result presentation.
- [x] Update `doc/design/building_block_view.md` with a test-runner trace presentation building block and the run-configuration result-view option.
- [x] Update `doc/design/runtime_view.md` with design items for presenter selection, test-runner hierarchy construction, status mapping, and navigation.
- [ ] Stop and ask user for a review of the design.

### Implementation

- [ ] Add a run-configuration presentation mode, defaulting existing configurations to plain text output.
- [ ] Extend `OftRunConfiguration` persistence and `OftRunConfigurationSettingsEditor` so users can choose plain text output or test runner output.
- [ ] Update `OftRunProfileState` to select `OftTraceRunContentOutputPresenter` or the new test-runner presenter from the saved presentation mode.
- [ ] Keep `OftTraceProjectAction` wired to the existing plain text presenter.
- [ ] Extend `OftTraceResult` so successful and defective trace runs retain the structured `Trace` in addition to the rendered text report.
- [ ] Update `OftTraceService` to return structured trace data without changing importer, linker, tracer, class-loader, filter, or text-rendering behavior.
- [ ] Implement a trace-to-test-tree mapper that groups results by source file, creates specification-item test nodes, and creates incoming and outgoing link sub-test nodes.
- [ ] Implement status-label derivation for specification items and links from OpenFastTrace trace data.
- [ ] Implement pass/fail mapping for clean and defective specification items and links.
- [ ] Implement `OftTraceTestRunnerOutputPresenter` using `SMTRunnerConsoleView` and `SMTestProxy`.
- [ ] Connect test-runner node navigation through `OftTraceNavigationResolver`.
- [ ] Present invalid input, cancellation, and unexpected errors coherently when the test-runner output mode is selected.
- [ ] Preserve existing ANSI-colored plain text output and console hyperlink behavior.
- [ ] Avoid adding third-party dependencies unless a separate design decision is approved.

### Verification

- [ ] Add tests for run-configuration presentation-mode defaults, persistence, and backward-compatible XML reading.
- [ ] Add settings-editor tests for selecting and applying the result-view option.
- [ ] Add `OftTraceResult` and `OftTraceService` tests proving structured `Trace` data is retained for clean and defective runs and absent for invalid-input, cancelled, and unexpected-error results.
- [ ] Add focused mapper tests for source-file grouping, specification-item nodes, incoming/outgoing link nodes, bracketed status labels, and pass/fail status.
- [ ] Add presenter tests for `SMTRunnerConsoleView` creation and event emission for successful and defective traces.
- [ ] Add run-profile tests proving plain text output remains the default and test-runner output is selected only when configured.
- [ ] Add navigation tests proving test-runner nodes navigate to specification declarations and source-side coverage tags through `OftTraceNavigationResolver`.
- [ ] Keep existing trace action, trace service, console presenter, run-configuration, and navigation tests green.
- [ ] Keep the OpenFastTrace trace clean for `feat`, `req`, `scn`, `dsn`, `impl`, `utest`, and `itest` artifacts in scope.
- [ ] Keep path coverage at or above the documented 80% threshold.
- [ ] Keep dependency policy unchanged and dependency verification compatible with the build.
- [ ] Run `./gradlew test`.
- [ ] Run `./gradlew check`.
- [ ] Run `./gradlew verifyPlugin`.
- [ ] Keep the SonarQube Cloud quality gate green after CI analysis.

### Update user documentation

- [ ] Update `README.md` to describe selecting the test runner result view from an OpenFastTrace run configuration and using navigation from the structured result tree.

## Version and Changelog Update

- [x] Check if the current version mentioned in the build scripts and code parameters is the same as the latest GitHub release. Current `gradle.properties` version is `0.7.0`; latest GitHub release is `0.7.0` as of 2026-06-09.
- [x] Decide if and which part of the version needs to be incremented. GH-40 is a feature, so the minor version needs to be incremented.
- [ ] Raise the version to `0.8.0` (this is a feature release).
- [ ] Write the changelog entry for `0.8.0`.
- [ ] Update release date to `2026-06-09`.
- [ ] Ensure that the issue list contains `#40: Integrate OFT Trace into Test Runner UI`.
