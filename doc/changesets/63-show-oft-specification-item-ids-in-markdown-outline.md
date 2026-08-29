# GH-63 Show OFT Specification Item IDs in the Markdown Outline

## Goal

Let users see the OpenFastTrace specification item declarations in the Markdown document they are currently reading or editing through the native Markdown Structure / File Structure view. The outline keeps the existing Markdown header hierarchy, lists declarations in source order under the last preceding header, and opens the exact declaration when the user selects an item.

## Scope

In scope:

* Add a Markdown-only outline feature for OpenFastTrace specification item declarations.
* Preserve the native Markdown header tree and add specification item ID declaration nodes beneath the last preceding header.
* Show declarations that occur before the first header at the document root.
* Retain source order among declaration nodes under the same parent.
* Recognize the existing plain and single-backtick Markdown declaration variants.
* Navigate from an outline node to the declaration anchor in the focused editor.
* Rebuild the outline from current document text so unsaved edits are represented.
* Update requirements, design, user documentation, release notes, and Marketplace change notes for this user-visible feature.

Out of scope:

* RST, plain text, source code, coverage-tag files, and other non-Markdown formats.
* `Covers:` references, coverage tags, and other non-declaration OFT ID occurrences.
* Specification titles, trace status, coverage, trace defects, or requirement-network rendering.
* A separate OFT tool window, a project-wide OFT browser, or replacement of the existing Markdown Structure view or Go to Symbol.
* New third-party dependencies.

## Design References

* [GH-63](https://github.com/itsallcode/openfasttrace-intellij-plugin/issues/63)
* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [GH-24 Auto-completion for Covers section](24-auto-completion-for-covers-section.md)

## Strategy

Add a new chain for Markdown specification-item outline navigation. The new requirement must state that only declaration IDs appear, that they retain source order below their last preceding Markdown header, that pre-header declarations appear at the root, and that selecting an item navigates to its declaration after unsaved edits.

Extend the Markdown-specific editor support rather than the project declaration index. Register a `StructureViewExtension` that preserves native Markdown structure nodes and contributes synthetic OFT declaration tree elements. Derive those elements from `OftSyntaxCore.findDefinitionSpecificationItems()` over the focused document text, map their offsets to the last preceding Markdown header, and reuse offset-based editor navigation. This preserves existing Markdown outline behavior and avoids stale index data while the user is editing.

## Task List

- [ ] Create and checkout a new Git branch `feat/63-show-oft-specification-item-ids-in-markdown-outline`

### Requirements And Design

- [ ] Add a feature item for the Markdown OpenFastTrace specification-item outline in `doc/system_requirements.md`.
- [ ] Add a user requirement stating that the Markdown Structure / File Structure view shows only recognized OFT declaration IDs while preserving Markdown headers.
- [ ] Add scenarios for placing declaration IDs under their last preceding header in source order, placing pre-header IDs at the root, navigating from an ID, and reflecting unsaved document edits.
- [ ] Stop and ask user for a review of the system requirements.
- [ ] Update `doc/design/solution_strategy.md` to record reuse of IntelliJ Structure-view extension APIs and current-document parsing rather than the project index.
- [ ] Extend `doc/design/building_block_view.md` with a Markdown specification-item outline component and its dependencies on Markdown PSI/Structure infrastructure and the shared syntax core.
- [ ] Add one runtime design item per outline scenario in `doc/design/runtime_view.md`, covering tree construction and placement, root-level declarations, navigation, and document-change refresh.
- [ ] Stop and ask user for a review of the design.

### Implementation

- [ ] Add a Markdown Structure-view extension registration without replacing the existing Markdown Structure-view factory.
- [ ] Implement an immutable, file-local outline model that extracts only `findDefinitionSpecificationItems()` matches from the current Markdown document and retains their source offsets and order.
- [ ] Implement synthetic, navigable outline tree elements that present the full OFT ID and navigate to its declaration offset in the editor.
- [ ] Associate each declaration with the closest preceding Markdown header PSI element and contribute it below that header; contribute declarations with no preceding header at the document root.
- [ ] Preserve all native Markdown header children and ordering while merging OFT declaration nodes, and exclude `Covers:` entries, coverage tags, invalid IDs, and incomplete IDs.
- [ ] Refresh the outline when the underlying Markdown document changes, including unsaved edits, without using the file-based project index.
- [ ] Keep the implementation limited to Markdown and IntelliJ Platform APIs; do not change RST, generic text, coverage-tag, project-index, or reference-resolution behavior. Don't use internal or deprecated APIs.

### Verification

- [ ] Add focused syntax/model tests for declaration extraction and source-order/header-association decisions, including plain and single-backtick declaration forms, pre-header declarations, and exclusion of references and invalid or incomplete IDs.
- [ ] Add IntelliJ light-fixture tests proving that Markdown headers remain present and that OFT IDs appear beneath the last preceding header in source order.
- [ ] Add IntelliJ light-fixture tests for root-level pre-header IDs, navigation to the exact declaration offset, and outline refresh after an unsaved document edit.
- [ ] Keep existing syntax, indexing, navigation, completion, and highlighting tests green.
- [ ] Keep the OpenFastTrace trace clean for the new `feat`, `req`, `scn`, `dsn`, `impl`, and `itest` artifacts in scope.
- [ ] Keep path coverage at or above 80%.
- [ ] Keep the dependency policy unchanged; add no third-party dependencies.
- [ ] Run `./gradlew test`.
- [ ] Run `./gradlew traceRequirements`.
- [ ] Run `./gradlew check`.
- [ ] Run `./gradlew verifyPlugin`.
- [ ] Keep the SonarQube Cloud quality gate green after CI analysis.

### Update User Documentation

- [ ] Update `README.md` and `../user_guide/images/user_guide.md` to explain the Markdown Structure / File Structure outline, header placement, declaration-only scope, and navigation behavior.

## Version And Changelog Update

- [ ] Check whether the current project version is the latest GitHub release.
- [ ] GH-63 is a user-visible feature and therefore requires a minor release.
- [ ] Raise the version to the selected semantic version.
- [ ] Write the release-changelog entry for the selected version.
- [ ] Update to the latest OpenFastTrace library version available on GitHub.
- [ ] Add `## Bundled OpenFastTrace` followed by `OpenFastTrace <version>` immediately after the release summary and before the first release category.
- [ ] Copy the active release notes, including the bundled OpenFastTrace version, into the Marketplace `changeNotes` through the existing Markdown-to-HTML build flow.
- [ ] Update the release date to the release date.
- [ ] Ensure the issue list contains `#63: Show OFT Specification Item IDs in the Markdown Outline`.
