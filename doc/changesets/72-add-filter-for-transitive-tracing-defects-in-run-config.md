# GH-72 Add filter for transitive tracing defects in run config

## Goal

Let users choose whether the trace result views should show transitive defects, with the checkbox defaulting on in run configuration templates so the existing defect-oriented view stays available while the UI can be narrowed to direct defects when needed.

## Scope

In scope:

* Add a run-configuration result-view checkbox for showing or hiding transitive defects.
* Default the new setting to show transitive defects in the bundled run configuration templates.
* Apply the setting in the IntelliJ Test Runner UI presentation.
* Apply the setting in the plain-text trace result presentation.
* Document the new behavior in traced requirements and the runtime design.

Out of scope:

* Changing OpenFastTrace trace evaluation or the transitive-defect detection rules.
* Adding an instant toggle in the test runner tree itself.
* Reworking the existing trace result views beyond the filter behavior needed here.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)

## Strategy

Treat the filter as a presentation concern that is stored with the run configuration, propagated into both trace presenters, and left out of the tracing engine. The implementation should keep direct defects visible, hide only transitive defects when the option is off, and preserve the existing default-on behavior in the preconfigured templates.

## Task List

- [ ] Create and checkout a new Git branch `feat/72-add-filter-for-transitive-tracing-defects-in-run-config`

### Requirements And Design

- [x] Update `doc/system_requirements.md` with a run-configuration result-view requirement for showing or hiding transitive defects and scenarios for the default-on template behavior.
- [x] Update `doc/design/runtime_view.md` with a design item describing how the stored result-view filter is applied in the test runner tree and plain-text presenter.
- [ ] Update `doc/design/building_block_view.md` if needed to show the run-configuration/settings-editor and presenter wiring for the new filter.
- [x] Stop and ask user for a review of the system requirements and design.

### Implementation

- [x] Extend the trace settings model and run-configuration persistence with a transitive-defect visibility flag.
- [x] Add the checkbox to the run-configuration settings editor and default it on for the existing templates.
- [x] Filter transitive defects out of the test-runner tree and plain-text output when the setting is off.
- [x] Keep direct defects, navigation, and overall trace failure behavior unchanged.

### Verification

- [x] Add focused tests for settings persistence, template defaults, and editor round-tripping of the transitive-defect flag.
- [x] Add presenter and tree-mapper tests proving transitive defects are shown or hidden according to the saved setting.
- [x] Add plain-text renderer or run-content tests proving the filtered output stays consistent with the configured result view.
- [x] Keep the OpenFastTrace trace clean.
- [x] Keep required build and plugin verification tasks green.
