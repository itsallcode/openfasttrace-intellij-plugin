# GH-61 Feature: Replace global OFT scan with specialized run configuration templates

## Goal

Remove the redundant global OpenFastTrace scan menu and provide specialized run configuration templates to allow users to quickly set up common scanning scenarios.

## Scope

In scope:

* Remove the 'Trace Project' action and its associated 'OpenFastTrace' menu group from the IntelliJ 'Tools' menu.
* Introduce the following pre-configured run configuration templates:
    - **User requirements**: Scans `doc/`, excludes source directories, filters for artifact types: `feat, req, scn, bconstr`.
    - **Design and above**: Scans `doc/`, excludes source directories, filters for artifact types: `feat, req, scn, bconstr, arch, dsn, constr, bld`.
    - **Typical project**: Scans `doc/` and all project source directories, with no artifact type filtering.
    - **Unfiltered**: Scans the entire project (`.`) with no filters.
* Leverage the IntelliJ `ConfigurationFactory` mechanism to provide these templates as sub-items in the "Add New Configuration" menu.

Out of scope:

* Adding new configuration options beyond what is currently supported.
* Changing the trace execution engine or result presentation.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Runtime View](../design/runtime_view.md)

## Strategy

We will update `OftRunConfigurationType` to register multiple `ConfigurationFactory` instances. Each factory will be initialized with a specific `OftTraceSettingsSnapshot` to provide the template behavior. The `OftRunConfigurationFactory` will be modified to accept these initial settings.

The global action `OftTraceProjectAction` and its registration in `plugin.xml` will be removed.

## Task List

- [x] Create and checkout a new Git branch `feature/61-run-config-templates`

### Requirements And Design

- [x] Update `doc/system_requirements.md` to remove the global trace action and document run configuration templates.
- [x] Stop and ask user for a review of the system requirements
- [x] Update `doc/design/runtime_view.md` to reflect the multi-factory approach for run configurations.
- [x] Stop and ask user for a review of the design

### Implementation

- [ ] Implement the logic to ignore trailing numbers in `impl`, `utest`, and `itest` IDs during tracing.
- [x] Modify `OftRunConfigurationFactory` to support template-based initialization.
- [x] Update `OftRunConfigurationType` to register the four requested templates.
- [x] Remove `OftTraceProjectAction.java` and any classes only used by it.
- [x] Remove `Oft.ToolsMenu` and related actions from `src/main/resources/META-INF/plugin.xml`.

### Verification

- [ ] Add a verification test for ignoring trailing numbers in implementation and test artifact types.
- [x] Update `OftRunConfigurationTest` to verify template-based initialization.
- [ ] Run `check` task to ensure all tests pass and requirements trace is clean.
- [ ] Keep the OpenFastTrace trace clean
- [ ] Keep required build and plugin verification tasks green

### Update user documentation

- [x] Update `README.md` to reflect the removal of the Tools menu and the addition of run configuration templates.

## Version and Changelog Update

- [ ] Check if the current version mentioned in the build scripts and code parameters is the same as the latest GitHub release.
- [x] Raise the version to 0.9.0 (this is a feature release)
- [x] Write the changelog entry for 0.9.0 in `doc/changes/changes_0.9.0.md`
- [x] Update `doc/changes/changelog.md` to include 0.9.0
- [x] Update version in `build.gradle.kts`
- [x] Write the bundled OpenFastTrace version into the fixed changelog location for 0.9.0
- [x] Update release date to today
- [ ] Ensure that issue list contains the GitHub issue number and title
