# GH-37 Introduce Run Configurations for OpenFastTrace

## Goal

Introduce run configurations for OpenFastTrace to allow users to define, save, and run different scan parameters directly from the IDE's run configuration toolbar.

## Scope

In scope:

* Creating, naming, and saving OpenFastTrace run configurations.
* Selecting scan scope: Whole project or selected resources.
* Optional inclusion of source and test roots.
* Additional project-relative paths.
* Optional artifact type filter (comma-separated).
* Optional tag filter (comma-separated).
* Integration with the IDE run/debug toolbar.

Out of scope:

* Debugging of OpenFastTrace execution.
* Remote execution of OpenFastTrace.
* Complex filter expressions beyond simple comma-separated lists.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Runtime View](../design/runtime_view.md)

## Strategy

The implementation will follow the standard IntelliJ Platform Run Configuration API. A new `ConfigurationType`, `ConfigurationFactory`, and `RunConfiguration` will be introduced. The existing tracing logic in `OftTraceRunner` and `OftTraceBackgroundRunner` will be refactored to accept configuration snapshots, allowing both the existing global action and the new run configurations to share the same execution core.

## Task List

- [ ] Create and checkout a new Git branch `feat/37-introduce-run-configurations`

### Requirements And Design

- [x] Update `doc/system_requirements.md` to include `feat~oft-run-configurations~1` and related requirements and scenarios.
- [ ] Stop and ask user for a review of the system requirements.
- [x] Update `doc/design/runtime_view.md` to include design items for run configurations.
- [x] Update `doc/design/building_block_view.md` to include the Run Configuration Editor UI mockup.
- [ ] Stop and ask user for a review of the design.

### Implementation

- [ ] Implement `OftRunConfigurationType`, `OftRunConfigurationFactory`, and `OftRunConfiguration`.
- [ ] Implement `OftRunConfigurationSettingsEditor` for the GUI (based on `doc/ui-mockups/ui_run_config.plantuml`).
- [ ] Refactor `OftTraceRunner` and `OftTraceBackgroundRunner` to support parameterized execution.
- [ ] Implement `OftRunProfileState` to handle the actual execution of the trace.

### Verification

- [ ] Add unit tests for `OftRunConfiguration` persistence.
- [ ] Add unit tests for input resolution with filters.
- [ ] Add integration tests for running a trace via run configuration.
- [ ] Keep the OpenFastTrace trace clean.
- [ ] Keep required build and plugin verification tasks green.

### Update user documentation

- [ ] Update the end user documentation in `README.md`.

## Version and Changelog Update

- [ ] Raise the version to 0.6.0 (this is a feature release)
- [ ] Write the changelog entry for 0.6.0
