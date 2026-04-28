# GH-24 Auto-completion for "Covers" section

## Goal

Add editor completion for OFT item IDs inside `Covers:` sections so users can reference existing specification items without leaving the editor or memorizing IDs.

The implementation should reuse the plugin's existing declaration index and navigation model, but extend the authoring experience with search-as-you-type suggestions that prioritize the most likely specification IDs for the current input.

## Scope

In scope:

* add user-visible auto-completion for OFT specification item IDs entered under `Covers:` in supported specification documents
* source completion suggestions from existing specification item declarations already indexed by the plugin
* rank search-as-you-type matches by the issue's required priority order: full-ID prefix, name-prefix, name-substring, then artifact-type prefix
* keep the completion behavior aligned with the existing distinction between declarations in the index and `Covers:` entries as references
* update requirements, design, and end-user documentation for the new authoring support

Out of scope:

* completion for non-`Covers:` sections such as `Needs:` or free-form prose
* creating new specification items from the completion UI
* fuzzy-ranking heuristics beyond the explicit priority order from GH-24 unless they are needed only as deterministic tie-breakers
* a custom popup or UI flow separate from IntelliJ's standard completion infrastructure
* the optional "next best matches near the input point" behavior unless it falls out naturally from the standard completion implementation

## Design References

* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)

## Strategy

GH-24 is a user-visible authoring feature, so the plan should extend the traced requirements and design instead of treating completion as an incidental implementation detail.

The preferred direction is to reuse the existing specification declaration index as the source of truth and add a completion contributor that activates only for OFT item IDs inside `Covers:` sections of supported specification files. That keeps the current architecture intact: declarations remain indexed project symbols, `Covers:` entries remain references to those declarations, and completion becomes an editor-facing consumer of the same indexed specification data that already powers symbol search and navigation.

Ranking should be deterministic and acceptance-criteria-driven. The implementation should therefore define one explicit matcher and ordering policy for completion suggestions rather than relying on IntelliJ defaults alone. If the existing index shape does not support efficient ranking by full ID, name part, and artifact type, extend the index or add a focused lookup layer without introducing new third-party dependencies.

## Task List

### Requirements And Design

- [x] Extend `doc/system_requirements.md` with a feature-level requirement for completion of specification IDs in `Covers:` sections
- [x] Add user-facing scenarios for completion in `Covers:` entries, including search by full-ID prefix, name-prefix, name-substring, and artifact-type prefix
- [x] Update the solution strategy to state that authoring assistance reuses IntelliJ completion infrastructure together with the existing declaration index
- [x] Add a building-block design item for specification-reference completion or extend the existing navigation/index design items to cover completion responsibilities explicitly
- [x] Add a runtime design item that describes how completion is triggered in a `Covers:` section, how candidates are loaded from the index, and how ranking is applied before suggestions are shown

### Completion Integration

- [x] Add a completion contributor that activates in supported specification documents only when the caret is inside an OFT item reference under `Covers:`
- [x] Reuse the existing `Covers:`-section parsing logic where practical so completion scope matches reference-resolution scope
- [x] Add a lookup service or equivalent adapter that reads indexed specification declarations and exposes completion candidates with canonical full ID, artifact type, and name-part metadata
- [x] Implement deterministic ranking that prioritizes exact full-ID prefix matches over name-prefix matches, name-substring matches, and artifact-type prefix matches
- [x] Define stable tie-breakers for equally ranked matches so completion results stay predictable across runs and files
- [x] Keep completion suggestions limited to declared specification items and exclude coverage occurrences or synthetic non-declaration matches
- [x] Register the completion extension in [plugin.xml](/home/seb/git/openfasttrace-intellij-plugin/src/main/resources/META-INF/plugin.xml) without adding new dependencies
- [x] Evaluate whether IntelliJ's normal popup behavior already satisfies the optional "next best matches near the input point" acceptance criterion and only add extra logic if a concrete gap remains

### Automated Verification

- [x] Add focused tests for completion availability in `Covers:` sections of supported specification files
- [x] Add ranking tests that prove the required priority order for full-ID prefix, name-prefix, name-substring, and artifact-type prefix matches
- [x] Add negative tests showing that completion does not trigger in non-`Covers:` sections or from non-declaration occurrences
- [x] Keep existing navigation and indexing tests green to prove completion does not regress declaration search or reference resolution
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [x] Keep path coverage at or above the documented threshold
- [x] Keep dependency policy unchanged and avoid adding new third-party libraries unless a design decision is approved first
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean

`./gradlew verifyPlugin` still fails because of pre-existing internal API usages in `OftTraceRunContentOutputPresenter` that are unrelated to GH-24.

### Update User Documentation

- [x] Update `README.md` so users can discover the new `Covers:` auto-completion workflow

## Version And Changelog Update

- [x] Raise the version for the next feature release
- [x] Write the changelog entry for that release
