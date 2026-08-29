# GH-65 Status filter

## Goal

Let users choose which OpenFastTrace specification-item statuses are included by an OpenFastTrace run configuration, while keeping the OFT default workflow focused on approved items.

## Scope

In scope:

* Add run-configuration checkboxes for the `Draft`, `Proposed`, `Approved`, and `Rejected` OFT item statuses.
* Persist the selected statuses with each run configuration and pass them to OpenFastTrace as import filter criteria.
* Select only `Approved` in every pre-configured run-configuration template by default.
* Prevent users from saving or running a configuration without at least one selected status.
* Upgrade the bundled OpenFastTrace library to a released version that supports `FilterSettings.Builder.wantedStatuses(...)` and refresh the Gradle dependency lock metadata.

Out of scope:

* Adding, renaming, or changing the semantics of statuses supported by OpenFastTrace.
* Filtering the already-produced trace model inside the plugin.
* Changing artifact-type filters, tag filters, trace scopes, or result presentation.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Architecture Decisions](../design/architecture_decisions.md)

## Strategy

Represent selected statuses as `ItemStatus` values at the plugin's settings and execution boundaries. Extend the immutable trace-settings snapshot, run-configuration XML state, templates, and editor together so a round trip preserves the selection. Validate the editor through IntelliJ's run-configuration validation mechanism and reject an empty set before execution as a defensive invariant. Pass the selection to OFT through `FilterSettings.Builder.wantedStatuses(...)`; do not duplicate OFT's filtering rules in plugin code.

At planning time, the locked OpenFastTrace 4.5.0 API did not expose status filtering. Upgrade to OpenFastTrace 4.9.0 or a newer maintainer-approved released version that provides `wantedStatuses(...)`, without adding a new third-party dependency.

## Task List

- [x] Create and checkout a new Git branch `feature/65-status-filter`

### Requirements And Design

- [x] Add `req~filter-trace-by-item-statuses~1` defining the four selectable statuses, persistence per run configuration, and the requirement that at least one status is selected.
- [x] Revise `req~openfasttrace-run-configuration-templates~1` to revision 2 so all four templates explicitly default to `Approved` only, and update its trace links.
- [x] Add separate acceptance scenarios for filtering a trace by selected statuses, defaulting every template to `Approved`, and rejecting an empty status selection; revise the existing template scenario and affected links where its semantics change.
- [x] Stop and ask user for a review of the system requirements.
- [x] Update `doc/design/solution_strategy.md` to state that status selection is delegated to OFT import filtering.
- [x] Update `doc/design/runtime_view.md` with one runtime design item per new status scenario, including settings persistence, template initialization, editor validation, and forwarding the selected `ItemStatus` set to OFT.
- [x] Revise `dsn~trace-configuration-integration~2` and `dsn~use-run-configuration-templates~1` where their semantics change, preserving trace links through revision updates.
- [x] Update `doc/design/building_block_view.md` and its run-configuration editor mockup with the four status checkboxes and the non-empty-selection constraint.
- [x] Record or update an architecture decision only if the dependency upgrade or status representation introduces a decision not already covered by the design. No new decision is required because the existing strategy already delegates filtering to OpenFastTrace and the upgrade adds no dependency category.
- [x] Stop and ask user for a review of the design.

### Implementation

- [x] Upgrade `org.itsallcode.openfasttrace:openfasttrace` from 4.5.0 to OpenFastTrace 4.9.0 or a newer approved release that exposes `FilterSettings.Builder.wantedStatuses(...)`, then refresh and review `gradle.lockfile`.
- [x] Extend `OftTraceSettingsSnapshot` with an immutable, non-empty selected-status set whose default is `ItemStatus.APPROVED`.
- [x] Extend `OftRunConfiguration.State`, `snapshot()`, and `updateFrom(...)` to serialize and restore selected statuses, with a compatibility fallback of `Approved` for configurations saved before GH-65.
- [x] Initialize every factory in `OftRunConfigurationType` with only `Approved` selected.
- [x] Add `Draft`, `Proposed`, `Approved`, and `Rejected` checkboxes to `OftTraceSettingsComponent`, and map them bidirectionally to the settings snapshot.
- [x] Integrate the non-empty status rule with IntelliJ run-configuration validation so Apply and Run report a clear validation error, and retain an execution-boundary guard against invalid persisted state.
- [x] Pass the selected statuses from `OftTraceInputs` through `OftTraceService` to `FilterSettings.Builder.wantedStatuses(...)`.

### Verification

- [x] Add parameterized `OftTraceSettingsSnapshotTest` coverage for each valid status selection and separate invalid empty-selection coverage, following the project's given-when-then naming and Hamcrest rules.
- [x] Add `OftTraceSettingsComponentTest` coverage for all four checkbox labels, settings round trips, and visible validation behavior for an empty selection.
- [x] Extend `OftRunConfigurationTest` to verify XML persistence, legacy-configuration fallback to `Approved`, empty-selection validation, and `Approved` defaults for all factories.
- [x] Extend trace input/service tests to verify that single and multiple selected statuses reach OFT's `FilterSettings` and exclude non-selected specification items in a functional trace.
- [x] Verify exception type and message for the execution-boundary empty-selection guard.
- [x] Run `./gradlew test` and keep path coverage at or above 80%.
- [ ] Run `./gradlew check` and keep the OpenFastTrace requirement/design trace, static checks, and dependency-lock checks green. The task was run, but remains blocked by eight pre-existing GH-60 rename trace defects; all GH-65 trace items are clean.
- [x] Run the repository's plugin packaging/descriptor verification and IntelliJ Plugin Verifier tasks against the supported IDE builds.
- [x] Confirm that the dependency upgrade adds no unapproved third-party library category.
- [ ] Review Dependabot and SonarQube results when CI results are available.

### Update user documentation

- [x] Update the run-configuration section and screenshots or examples in `README.md` to explain status filtering, the `Approved` default, and the requirement to select at least one status.

## Version and Changelog Update

- [x] Check whether the version in the build scripts and code parameters matches the latest GitHub release.
- [x] Decide the next semantic version; treat GH-65 as a minor feature release unless it ships as part of an already planned release.
- [x] Raise the project version to 0.10.0.
- [x] Write the changelog entry for 0.10.0 and include GH-65 with its exact title, `Status filter`.
- [x] Determine the resolved bundled OpenFastTrace version from Gradle dependency metadata after the upgrade.
- [x] Add `## Bundled OpenFastTrace` followed by `OpenFastTrace 4.9.0` immediately after the release summary and before the first issue-category section.
- [x] Update the release date to 2026-08-28.
