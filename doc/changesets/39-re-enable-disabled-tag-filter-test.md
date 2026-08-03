# GH-39 Re-enable disabled tag filter test

## Goal

Re-enable the disabled tag-filter regression test in `OftTraceServiceTest` now that OpenFastTrace 4.8.0 includes the upstream tag-parser fix. The plugin should once again verify that tag filtering works end to end for traced Markdown content.

## Scope

In scope:

* Update the bundled OpenFastTrace dependency from 4.5.0 to 4.8.0 so the plugin exercises the fixed upstream tag parser.
* Remove the temporary `@Disabled` suppression from `testGivenTagFilterMatchingArtifactWhenTracingThenItIncludesTheArtifact`.
* Keep the existing tag-filter behavior and test intent unchanged.
* Verify the fix with focused tests and the normal project quality gates.

Out of scope:

* Adding a new user-facing tag-filter feature.
* Changing requirement or design text, because the current specification already covers tag filtering.
* Broadening trace filter semantics beyond the current comma-separated filter model.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Runtime View](../design/runtime_view.md)
* [Building Block View](../design/building_block_view.md)

## Strategy

Treat this as an upstream dependency alignment issue rather than a product feature change. Move the project to OpenFastTrace 4.8.0, drop the temporary test suppression, and confirm that the existing trace service still passes tag filters through to the library.

## Task List

- [ ] Create and checkout a new Git branch `fix/39-re-enable-disabled-tag-filter-test`

### Requirements And Design

- [ ] Confirm that the existing `req~filter-trace-by-tags~1` and `scn~filter-run-configuration-by-tags~1` coverage already describes the intended behavior, so no requirement or design edits are needed.

### Implementation

- [ ] Update the bundled OpenFastTrace dependency to 4.8.0 in `build.gradle.kts` and refresh `gradle.lockfile` as needed.
- [ ] Remove the `@Disabled` annotation from `testGivenTagFilterMatchingArtifactWhenTracingThenItIncludesTheArtifact`.
- [ ] Keep the tag-filter regression test focused on the existing Markdown-based end-to-end trace flow.

### Verification

- [ ] Run the focused `OftTraceServiceTest` tag-filter case against the updated dependency.
- [ ] Run `./gradlew traceRequirements` to confirm the OFT trace remains clean.
- [ ] Run `./gradlew check`.
- [ ] Run `./gradlew verifyPlugin`.

