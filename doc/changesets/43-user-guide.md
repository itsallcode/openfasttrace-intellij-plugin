# GH-43 User guide

## Goal

Create a use-case-centric user guide for the OpenFastTrace IntelliJ plugin under `doc/user_guide.md`.

The guide should move the user-facing usage documentation out of the short README overview and give users a practical path for installation, authoring, navigation, tracing, run configurations, and trace-result inspection.

## Scope

In scope:

* create `doc/user_guide.md`
* create `doc/user_guide/images/` for screenshots referenced by the guide
* document plugin installation and typical plugin use cases
* reuse and expand the current README usage content where it is still accurate
* link the new plugin user guide from `README.md`
* keep documentation examples trace-safe when they contain OFT-looking IDs or coverage tags

Out of scope:

* changing plugin behavior or production code
* changing automated plugin tests
* changing `doc/system_requirements.md`
* changing `doc/design.md` or linked design chapters
* changing the IDE Help-menu action that opens the upstream OpenFastTrace user guide
* replacing or editing the upstream OpenFastTrace user guide
* adding dependencies, build plugins, or screenshot tooling

## Design References

* [GitHub Issue #43](https://github.com/itsallcode/openfasttrace-intellij-plugin/issues/43)
* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [README](../../README.md)

## Strategy

Treat GH-43 as a documentation-only change. The issue explicitly excludes code, requirement, and design changes.

The existing traced Open OFT User Guide feature describes the plugin action that opens the upstream OpenFastTrace user guide from the IDE. GH-43 adds a repository-local user guide for this IntelliJ plugin, so the product requirements and runtime design remain accurate unless implementation uncovers a contradiction.

Because `traceRequirements` scans `doc/`, wrap any illustrative OFT specification items or coverage tags in `<!-- oft:off -->` / `<!-- oft:on -->` blocks, or phrase examples so they cannot be imported as live trace artifacts. Keep screenshots as ordinary documentation assets under `doc/user_guide/images/` and reference them with relative Markdown paths.

## Task List

- [x] Create and checkout a new Git branch `documentation/43-user-guide`

### Requirements And Design

- [x] Confirm GH-43 remains a documentation-only issue and explicitly excludes code, requirement, and design changes
- [x] Confirm the existing Open OFT User Guide requirements and design items describe the IDE action for the upstream OpenFastTrace guide, not the repository-local plugin user guide requested by GH-43
- [x] Keep `doc/system_requirements.md` unchanged unless guide work reveals a real mismatch
- [x] Keep `doc/design.md` and linked design chapters unchanged unless guide work reveals a real mismatch
- [x] Record any discovered requirement or design mismatch before changing traced artifacts

### User Guide

- [x] Create `doc/user_guide.md`
- [x] Create `doc/user_guide/images/`
- [x] Add an installation section for installing or running the plugin from the available project/release workflows
- [x] Add a quick-start workflow that starts from an opened IntelliJ project containing OFT documents
- [x] Document syntax-highlighting support for supported specification documents and coverage tags
- [x] Document searching specification items through `Go to Symbol`
- [x] Document navigation from `Covers:` entries and coverage tags to specification item declarations
- [x] Document bundled OpenFastTrace live templates and completion in `Covers:` fields
- [x] Document project-wide and selected-resource tracing through `Tools | OpenFastTrace | Trace Project`
- [x] Document custom OpenFastTrace run configurations, trace inputs, artifact-type filters, tag filters, and output modes
- [x] Document the default IntelliJ Test Runner UI trace result view and source navigation from trace results
- [x] Document the existing Help-menu action that opens the upstream OpenFastTrace user guide
- [x] Add a short troubleshooting section for common user-guide-level problems such as no specification items found, missing navigation targets, and unexpected trace results
- [x] Add links to the demo script and demo example project where they help users practice the workflows

### Screenshots

- [x] Capture current screenshots from a sandbox or installed IDE session with the plugin enabled
- [x] Store screenshots under `doc/user_guide/images/` with stable lowercase kebab-case names
- [x] Add `doc/user_guide/images/plugin-installed.png` showing the plugin installed in `Settings | Plugins`
- [x] Add `doc/user_guide/images/syntax-highlighting.png` showing highlighted OFT items in the demo specification
- [x] Add `doc/user_guide/images/go-to-symbol.png` showing symbol search for an OFT item ID
- [x] Add `doc/user_guide/images/reference-completion.png` showing completion in a `Covers:` field or coverage-tag target
- [x] Add `doc/user_guide/images/trace-setting.png` showing `Settings | Tools | OpenFastTrace`
- [x] Add `doc/user_guide/images/run-configurations.png` showing an `OpenFastTrace` run configuration
- [x] Add `doc/user_guide/images/test-runner-results.png` showing trace output in the IntelliJ Test Runner UI
- [x] Add `doc/user_guide/images/help-user-guide.png` showing the Help-menu user-guide action or the opened guide tab
- [x] Reference the screenshots from the relevant sections in `doc/user_guide.md`
- [x] Verify each screenshot path referenced from `doc/user_guide.md` resolves correctly in GitHub Markdown
- [x] Keep screenshots focused on plugin UI and avoid unrelated IDE state or local user data

### README Update

- [x] Add the new plugin user guide link to the README `User Guides` section
- [x] Reduce duplicated usage details in README if the new guide becomes the authoritative user-facing usage document
- [x] Preserve the upstream OpenFastTrace User Guide link because it documents OFT syntax and tracing outside this plugin
- [x] Preserve the plugin demo link as a demonstration resource

### Verification

- [x] Run `./gradlew traceRequirements` and keep the repository OpenFastTrace trace clean
- [x] Verify examples in `doc/user_guide.md` do not create imported trace artifacts unless intentionally excluded from tracing
- [x] Verify all new README and user-guide links resolve locally
- [x] Verify all referenced images exist under `doc/user_guide/images/`
- [x] Confirm no dependency-policy review is needed because the change adds no third-party libraries
- [x] Confirm required build and plugin verification tasks are not additionally required because implementation touched only documentation and screenshot files

## Version And Changelog Update

- [x] Check the current version mentioned in the build scripts and code parameters against the latest GitHub release: `gradle.properties` currently uses `0.8.0`, while the latest GitHub release is `0.7.0` as of 2026-06-09
- [x] Re-check the latest GitHub release immediately before version editing in case `0.8.0` has been published meanwhile
- [x] Keep the project version at `0.8.0` if it is still ahead of the latest GitHub release; otherwise raise the version to `0.8.1` as a documentation patch release
- [x] Add a changelog entry for GH-43 to the active release changelog
- [x] Update release date to 2026-06-09 or the actual implementation date
- [x] Ensure the issue list contains `#43: User guide`
