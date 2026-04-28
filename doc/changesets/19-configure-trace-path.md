# GH-19 Configure trace path

## Goal

Let users control which project resources the plugin passes into OpenFastTrace instead of always tracing the whole opened project directory.

The implementation should preserve the current one-click trace workflow for whole-project scans while adding a project-level configuration that supports OFT's whitelist-style input selection for projects that contain example files, generated artifacts, binaries, or other content that must stay out of the trace.

## Scope

In scope:

* add a project-level trace configuration integrated into IntelliJ project settings
* let users choose between tracing the whole project and tracing selected resources
* support selected-resource tracing from IntelliJ source roots, IntelliJ test roots, and additional project-relative paths
* default selected-resource tracing to include source roots, test roots, and `doc/`
* update trace input resolution, execution, and user-visible output so the trace uses the configured inputs
* update requirements and design documentation for configurable trace inputs

Out of scope:

* arbitrary exclude patterns, glob expressions, or blacklist-style filtering
* multiple named trace profiles or per-run input selection dialogs
* custom trace verbosity, reporter, or output-format settings
* tracing resources outside the opened project directory

## Design References

* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)

## Strategy

The current trace flow resolves one project-root directory and passes that directly to OpenFastTrace. GH-19 requires turning trace input resolution into a project-scoped configuration concern.

The preferred direction is to keep the `Trace Project` action unchanged from the user's point of view and introduce a persistent project settings model that resolves either:

* one whole-project directory input, or
* a whitelist of selected directories and files assembled from IntelliJ source roots, IntelliJ test roots, and additional relative paths.

That settings model should stay separate from the raw OFT service so the configuration logic can be tested independently of trace execution and reused consistently by the action, background runner, and output presentation.

## Task List

### Requirements And Design

- [x] Extend `doc/system_requirements.md` so the trace feature no longer assumes only `req~trace-open-project-from-project-root~1`, but also specifies configurable whole-project vs selected-resource tracing
- [x] Add or update user-facing requirements and scenarios for project-level trace configuration, including source-root inclusion, test-root inclusion, and additional relative trace paths
- [ ] Require user review of system requirements
- [ ] Update the design documentation so trace configuration becomes an explicit plugin building block instead of an implicit project-root resolver
- [ ] Update runtime design items for the trace flow to describe how configured inputs are resolved, validated, and handed to OpenFastTrace
- [ ] Require user review of design

### Project Settings Integration

- [ ] Add a persistent project-level settings model for trace scope mode and selected-resource options
- [ ] Integrate the trace settings into IntelliJ project configuration in a way that is discoverable from normal project settings navigation
- [ ] Provide a settings UI that lets users choose whole-project tracing or selected-resource tracing
- [ ] In selected-resource mode, provide toggles for including IntelliJ source roots and IntelliJ test roots, both enabled by default
- [ ] Provide a multi-line field for additional project-relative files or directories with default value `doc/`
- [ ] Validate configured additional paths and present invalid entries in a way users can correct before or during trace execution

### Trace Input Resolution And Execution

- [ ] Replace the single-path trace input resolver with a configuration-aware resolver that produces the effective OFT input set for the current project
- [ ] Resolve IntelliJ source and test roots through project/module metadata instead of hard-coded path conventions
- [ ] Normalize, de-duplicate, and validate the effective trace inputs before starting the OFT run
- [ ] Keep whole-project tracing as the default behavior for existing projects that have no explicit GH-19 settings yet
- [ ] Update the trace service so it can import from the effective configured input list rather than only one base directory
- [ ] Update the trace output text so users can verify which configured directories and files were scanned for the current run

### Automated Verification

- [ ] Add focused tests for persistent trace settings defaults, storage, and project-settings UI behavior
- [ ] Add tests for configuration-aware input resolution from source roots, test roots, additional relative paths, and invalid path entries
- [ ] Add trace execution tests that prove OFT receives only the configured whitelist inputs in selected-resource mode
- [ ] Keep existing whole-project trace tests green and cover backward-compatible default behavior for projects without explicit settings
- [ ] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [ ] Keep path coverage at or above the documented threshold
- [ ] Keep dependency policy unchanged and avoid adding new third-party libraries unless a design decision is approved first
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean

### Update user documentation

- [ ] Update the end user documentation in README.md

## Version and Changelog Update

- [x] Raise the version to 0.3.0 (this is a feature release)
- [x] Write the changelog entry for 0.3.0
