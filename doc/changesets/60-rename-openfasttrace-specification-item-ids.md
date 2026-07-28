# GH-60 Feature: Rename OpenFastTrace specification item IDs

## Goal

Allow users to apply IntelliJ's standard Rename refactoring to an OpenFastTrace specification item ID. Renaming updates the declaration and every resolved OFT reference in supported specification documents and coverage-tag files, while leaving unrelated matching text untouched.

## Scope

In scope:

* Rename a full canonical OFT item ID from its declaration or from a resolved `Covers:`, `Depends:`, or coverage-tag reference.
* Use IntelliJ's standard rename/usages and preview workflow before edits are applied.
* Update the declaration and resolved references in supported Markdown, RST, and coverage-tag file types, including either side of a coverage tag where that side resolves to the renamed declaration.
* Reject a rename to a target full ID that is already declared in the project.
* Preserve the existing declaration-versus-reference model and reuse its   syntax, indexing, resolution, and usage-search infrastructure.
* Update traced requirements, design, user documentation, and release notes.

Out of scope:

* Renaming arbitrary text that happens to equal an OFT ID but is not an indexed declaration or a resolved OFT reference.
* Automatically changing the semantics, coverage structure, or revision policy of an OFT item; intentional semantic trace-model changes remain manual.
* Refactoring partial or invalid OFT ID fragments, including incomplete tags.
* New third-party dependencies or a custom rename dialog, preview, or usages UI.

## Design References

* [GH-60](https://github.com/itsallcode/openfasttrace-intellij-plugin/issues/60)
* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [GH-24 Auto-completion for Covers section](24-auto-completion-for-covers-section.md)

## Strategy

Model the full OFT item ID as the renameable canonical name of its indexed declaration. Adapt the existing declaration navigation element to participate in IntelliJ rename and provide a usage search that returns only the existing PSI references which resolve to that declaration. The standard platform refactoring engine can then collect usages, present its normal preview, replace the precise ID ranges, and refresh the existing file-based declaration index.

The implementation must validate a proposed full ID with the shared OFT syntax model and use the declaration index to detect an existing target declaration. It must not perform a project-wide string replacement. In particular, shortened coverage-tag left sides must only be changed when their resolved effective ID
is the renamed declaration, and their syntactic shorthand must remain intact.

## Task List

- [ ] Create and checkout a new Git branch `feature/60-rename-openfasttrace-specification-item-ids`

### Requirements And Design

- [x] Add a `feat` and user requirement for renaming canonical OFT specification item IDs through IntelliJ Rename, covering the existing navigation feature or a new focused refactoring feature as appropriate.
- [x] Add `scn` items for initiating rename at a declaration and at a resolved reference; updating supported `Covers:`/`Depends:` entries and coverage-tag references; reviewing standard Rename usages before application; excluding non-OFT text; and rejecting an already-declared target ID.
- [ ] Stop and ask user for a review of the system requirements.
- [x] Update `doc/design/solution_strategy.md` to state that OFT refactoring reuses IntelliJ rename and find-usages APIs over canonical declarations and resolved PSI references rather than textual replacement.
- [x] Extend `doc/design/building_block_view.md` with rename/refactoring responsibilities and dependencies between the declaration index, navigation/reference support, OFT syntax core, and IntelliJ refactoring infrastructure.
- [x] Add one `dsn` item per rename scenario in `doc/design/runtime_view.md`, including declaration/reference initiation, resolved usage discovery and preview, selective replacement (including coverage-tag shorthand), target-ID conflict validation, and index refresh after edits.
- [ ] Stop and ask user for a review of the design.

### Implementation

- [ ] Extract or extend the navigation PSI representation so a declaration exposes the canonical full OFT ID as a writable IntelliJ rename target with an exact declaration-anchor range.
- [ ] Add a refactoring/rename provider or PSI integration that resolves a declaration when Rename starts on either its anchor or an existing OFT reference, and delegates preview/apply behavior to IntelliJ's standard Rename workflow.
- [ ] Reuse the existing reference contributors and declaration resolver to find usages only when they resolve to the selected declaration; include supported specification-reference locations (`Covers:` and `Depends:`) and both supported coverage-tag sides.
- [ ] Implement precise replacement of full declarations and full references while preserving Markdown backticks, surrounding content, coverage-tag delimiters, and shortened left-side syntax where applicable.
- [ ] Validate the proposed canonical ID and reject malformed targets; query the declaration index to report an existing target declaration as a rename conflict without editing any files.
- [ ] Declare the shared IntelliJ language module and register only the necessary IntelliJ extension points in `src/main/resources/META-INF/plugin.xml`; do not add third-party dependencies or use internal/deprecated platform APIs.
- [ ] Confirm that affected file/index updates are visible to existing Go to Symbol, Go To Declaration, Go To Implementations, and completion flows after a successful rename.

### Verification

- [ ] Add syntax/model tests for canonical-ID validation, conflict detection, exact ID range replacement, and preserving shortened coverage-tag left-side syntax.
- [ ] Add IntelliJ light-fixture refactoring tests for rename from a declaration and from a `Covers:`, `Depends:`, and coverage-tag reference; assert the declaration and all resolved usages change together.
- [ ] Add fixture tests that inspect the standard Rename usage set/preview inputs and prove unrelated matching prose, invalid fragments, unsupported files, and unresolved references are not changed.
- [ ] Add negative fixture tests for an already-declared target ID and malformed target IDs, verifying that the refactoring reports the conflict and leaves project files unchanged.
- [ ] Add regression tests that navigation, implementation search, and completion resolve the new ID after rename and no longer resolve the old ID.
- [ ] Keep existing syntax, indexing, navigation, completion, and highlighting tests green.
- [ ] Keep the OpenFastTrace trace clean for the new `feat`, `req`, `scn`, `dsn`, `impl`, `utest`, and `itest` artifacts in scope.
- [ ] Keep path coverage at or above 80%.
- [ ] Keep the dependency policy unchanged; add no third-party dependencies.
- [ ] Run `./gradlew test`.
- [ ] Run `./gradlew traceRequirements`.
- [ ] Run `./gradlew check`.
- [ ] Run `./gradlew verifyPlugin`.
- [ ] Keep the SonarQube Cloud quality gate green after CI analysis.

### Update User Documentation

- [ ] Update `README.md` and `doc/user_guide.md` to explain invoking Rename from OFT declarations and references, reviewing IntelliJ's rename preview, supported reference locations, and duplicate-ID conflict behavior.

## Version And Changelog Update

- [ ] Check whether the current project version is the latest GitHub release.
- [ ] GH-60 is a user-visible feature and therefore requires the next minor release.
- [ ] Raise the version to the selected semantic version.
- [ ] Write the release-changelog entry for the selected version and include `#60: Rename OpenFastTrace specification item IDs`.
- [ ] Add `## Bundled OpenFastTrace` followed by the selected OpenFastTrace version immediately after the release summary and before the first release category.
- [ ] Copy the active release notes, including the bundled OpenFastTrace version, into the Marketplace `changeNotes` through the existing Markdown-to-HTML build flow.
- [ ] Update the release date to the release date.
