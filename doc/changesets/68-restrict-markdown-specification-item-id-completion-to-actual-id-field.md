# GH-68 Restrict Markdown specification item ID completion to the actual ID field

## Goal

Make Markdown specification-item ID completion fire only while the caret is in the actual declaration ID field, not in the title field or other non-ID text.

Keep the existing `Covers:` and coverage-tag completion behavior, ranking, and candidate source unchanged while tightening the Markdown declaration context and standardizing the ID/name-part terminology in user-facing text.

## Scope

In scope:

* add Markdown declaration-ID completion scenarios that activate only in the actual specification-item ID field
* suppress completion in the title field, surrounding body text, and other non-ID Markdown text
* cover live-template expansion states where the title is still being edited and the template also contains a derived ID field
* keep the existing `Covers:` and coverage-tag completion paths unchanged
* normalize user-facing wording to use `ID` for the technical anchor and `name-part` for the human-readable part where this issue touches text

Out of scope:

* changing completion ranking or the declaration index
* changing completion behavior for `Covers:` entries or coverage-tag targets
* adding completion to new file types or new OFT contexts
* changing bundled live-template placeholder order or the generated skeleton structure unless a wording-only rename is needed
* touching unrelated historic changesets

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [GH-24 Auto-completion for "Covers" section](24-auto-completion-for-covers-section.md)
* [GH-29 Completion in Covers Sections During Active Live-Template Expansion](29-completion-in-covers-sections-during-live-template-expansion.md)

## Strategy

Treat GH-68 as a completion-context refinement for Markdown declaration authoring rather than a change to the shared completion ranking or indexed candidate source.

The implementation should reuse the existing declaration index and completion lookup path, but add a Markdown declaration-ID field detector that can distinguish the actual ID anchor from the title field and any other non-ID text in the same specification item. If the current live-template structure makes the ID field and title field ambiguous to completion, tighten the field detection there instead of broadening completion to unrelated text.

The terminology cleanup should follow the same rule: prefer `ID` for the canonical technical anchor and `name-part` for the human-readable portion of the identifier wherever this issue updates user-facing text, template text, or test names.

## Task List

- [ ] Create and checkout a new Git branch `bugfix/68-restrict-markdown-specification-item-id-completion-to-actual-id-field`

### Requirements And Design

- [x] Add a feature-level requirement and user requirement in `doc/system_requirements.md` for Markdown specification item ID completion that activates only in the actual declaration ID field
- [x] Add scenarios for completion in the Markdown declaration ID field
- [x] Stop and ask user for a review of the system requirements
- [x] Update `doc/design/solution_strategy.md` so Markdown declaration-ID completion is described as a separate authoring context that still reuses IntelliJ completion and the existing declaration index
- [x] Update `doc/design/building_block_view.md` and `doc/design/runtime_view.md` with the Markdown declaration-ID completion responsibilities, field detection, and negative activation cases
- [x] Stop and ask user for a review of the design

### Implementation

- [ ] Refine the Markdown completion context detection in `src/main/java/org/itsallcode/openfasttrace/intellijplugin/navigation/OftSpecificationCompletionProvider.java` so the shared completion path only activates in the actual declaration ID field
- [ ] Keep the existing `Covers:` and coverage-tag completion paths unchanged while the Markdown declaration-ID path is tightened
- [ ] Update bundled live-template text or helper metadata in `src/main/resources/liveTemplates/OpenFastTrace.xml` and `src/main/java/org/itsallcode/openfasttrace/intellijplugin/templates/OftLiveTemplates.java` only if wording needs to be aligned to `ID` and `name-part`
- [ ] Add or adjust implementation coverage tags for the new design items and terminology updates

### Verification

- [ ] Add platform tests that prove completion appears in the Markdown declaration ID field and does not appear in the title field, surrounding body text, or an active live-template title entry
- [ ] Add regression coverage showing that existing `Covers:` and coverage-tag completion behavior, ranking, and candidate selection remain unchanged
- [ ] Add focused unit tests for the Markdown completion context detector and any live-template field helpers introduced by the fix
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [ ] Keep path coverage at or above the documented threshold
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean

### Update User Documentation

- [ ] Update `README.md` and any authoring guidance touched by this issue so the terminology uses `ID` and `name-part` consistently

## Version And Changelog Update

- [ ] Check whether the current project version needs a bugfix release bump
- [ ] Write the changelog entry if this issue is included in a release
