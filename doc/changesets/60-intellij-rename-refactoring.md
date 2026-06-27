# GH-60 Rename OpenFastTrace specification items with IntelliJ refactoring

## Goal

Add rename refactoring support for OpenFastTrace specification item IDs so users can rename a declared item ID through IntelliJ's native rename workflow and have the plugin update OFT references consistently.

The implementation should stay as close as possible to IntelliJ's standard rename/refactoring behavior instead of introducing a custom rename UI or plugin-specific editing flow.

## Scope

In scope:

* rename OFT specification item IDs from their declaration header in supported specification documents
* update matching OFT references in `Covers:` entries and coverage tags when they resolve to the renamed declaration
* keep the refactoring behavior aligned with IntelliJ's built-in rename action, preview, validation, and usage update mechanisms
* update traced requirements, design, tests, and user documentation for the new refactoring support

Out of scope:

* a custom rename dialog, wizard, or bulk-edit UI
* renaming arbitrary plain text that is not part of an OFT declaration or reference
* changing Go to Symbol, Go To Declaration, or completion semantics beyond what rename support needs
* adding new dependencies or a new editor workflow outside the IntelliJ refactoring infrastructure

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)

## Strategy

Model OFT declarations as renameable PSI-backed symbols and let IntelliJ drive the actual rename workflow. The plugin should expose the declaration anchor as the rename target, treat `Covers:` entries and coverage tags as usages, and let the platform propagate the item-ID rename through the existing refactoring machinery.

This keeps the implementation close to ordinary IntelliJ rename behavior:

* rename starts from a declaration, not from a custom plugin action
* usage discovery comes from the same PSI/reference model that already powers navigation
* preview and in-place rename should behave the way users expect from other IntelliJ symbol renames

## Task List

- [ ] Create and checkout a new Git branch `feature/60-intellij-rename-refactoring`

### Requirements And Design

- [ ] Update `doc/system_requirements.md` with a feature requirement for OFT rename refactoring
- [ ] Add scenarios for renaming a declared OFT specification item and updating all resolved OFT references
- [ ] Stop and ask user for a review of the system requirements
- [ ] Update `doc/design/solution_strategy.md` to describe the IntelliJ-native rename approach
- [ ] Update `doc/design/building_block_view.md` and `doc/design/runtime_view.md` with the rename/refactoring flow
- [ ] Stop and ask user for a review of the design

### Implementation

- [ ] Make OFT specification item declarations participate in IntelliJ rename refactoring as the rename source
- [ ] Ensure OFT `Covers:` references and coverage tags resolve as rename usages for the renamed declaration
- [ ] Keep rename behavior limited to declarations that the plugin can map back to a canonical OFT item ID

### Verification

- [ ] Add rename-refactoring tests that rename a specification declaration and verify the updated declaration text
- [ ] Add rename-refactoring tests that verify resolved `Covers:` references and coverage-tag usages are updated
- [ ] Add an index-and-navigation regression test that renames a specification item ID, confirms the new ID is searchable and navigable, and confirms the old entry no longer appears in index-driven navigation
- [ ] Add negative tests showing that non-declaration text does not participate in the rename flow
- [ ] Keep existing navigation, completion, and indexing tests green
- [ ] Keep the OpenFastTrace trace clean for the requirement and design artifact types in scope
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean

### Update User Documentation

- [ ] Update `README.md` and the user guide to describe OFT rename refactoring from the IDE's standard rename action

## Version and Changelog Update

- [ ] Check whether this change should be part of a release version update or remain in the current unreleased line
- [ ] Write the changelog entry for the chosen release version if this issue is included in a release
