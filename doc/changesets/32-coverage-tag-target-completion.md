# GH-32 Auto-completion for OFT Coverage-Tag Targets

## Goal

Add specification item ID completion while users edit the right-hand target side of OpenFastTrace coverage tags in supported source, configuration, and markup files.

The feature should give developers the same indexed-ID completion that already exists for `Covers:` entries in Markdown and RST specification documents, but only when the editor context strongly indicates that the user is editing an OFT coverage tag.

## Scope

In scope:

* complete declared OpenFastTrace specification item IDs on the right-hand side of likely OFT coverage tags in supported coverage-tag files
* trigger completion only inside comments when the current tag candidate already contains a left-hand artifact token and `->`
* support optional whitespace around `->`
* support incomplete coverage tags while the user is still editing the target side
* reuse the existing declaration index and completion ranking policy from `Covers:` completion
* add automated platform tests for positive and negative activation cases
* update requirements, design, README, version, and changelog for the new authoring capability

Out of scope:

* completion for the left-hand side of coverage tags
* completion outside comments, in ordinary code, in string literals, or in unsupported file types
* creating new specification items from completion
* inserting whole coverage-tag templates such as `[impl->...]`
* changing coverage-tag highlighting, navigation, or strict tag parsing semantics beyond what completion context detection needs
* fuzzy matching or ranking changes beyond the existing completion policy
* adding new third-party dependencies

## Design References

* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [GH-24 Auto-completion for "Covers" section](24-auto-completion-for-covers-section.md)
* [GH-29 Completion in Covers Sections During Active Live-Template Expansion](29-completion-in-covers-sections-during-live-template-expansion.md)

## Strategy

Treat coverage-tag target completion as a second activation context for the existing specification-item completion feature.

The completion candidate source and ranking should stay shared with `Covers:` completion: declared specification items come from the project-local declaration index and are ranked by full-ID prefix, name-prefix, name-substring, and artifact-type prefix matches, with deterministic tie-breaking by full ID.

Add a tolerant coverage-tag completion context detector instead of reusing only the strict complete-tag parser. The strict parser should continue to serve highlighting and navigation for syntactically valid coverage tags, while completion must also recognize incomplete tags such as `[impl->dsn~partial` while the user is typing.

The activation rule should be conservative:

* the file is a supported coverage-tag file
* the caret is in a comment PSI element or equivalent comment context
* the current bracketed candidate starts before the caret and has not already closed before the caret
* the candidate contains a left-hand artifact token followed by `->`
* the caret is on the right-hand target side of the arrow

For file types where IntelliJ does not expose a reliable comment PSI element, use the smallest practical fallback that still requires the supported file type, the bracketed candidate, and the left-hand artifact-plus-arrow signal. The fallback must not make ordinary string literals or unrelated code completion targets.

## Task List

- [x] Create and checkout a new Git branch `feature/32-coverage-tag-target-completion`

### Requirements And Design

- [x] Extend `feat~oft-reference-completion~1` so the feature description includes coverage-tag target completion, not only `Covers:` entries
- [x] Add a user requirement for completing specification item IDs in coverage-tag targets
- [x] Add a positive scenario for completion in a supported source-file comment when the caret is on the right side of `[impl->...]`
- [x] Add scenarios for optional whitespace around `->` and incomplete coverage tags without a closing bracket
- [x] Add a negative scenario covering no suggestions before the arrow, outside comments, in string literals, and in unsupported file types
- [x] Stop and ask user for a review of the system requirements
- [x] Update the solution strategy to state that reference authoring assistance now covers `Covers:` entries and coverage-tag targets while still reusing IntelliJ completion and the declaration index
- [x] Update the building-block design for specification-item completion so it includes coverage-tag target context detection and reuse of indexed declarations
- [x] Add runtime design items for successful coverage-tag target completion and conservative non-activation outside valid target contexts
- [x] Stop and ask user for a review of the design

### Completion Integration

- [x] Extract or preserve a shared completion lookup path so `Covers:` completion and coverage-tag target completion use the same candidate source and ranking
- [x] Add a tolerant coverage-tag completion context detector that recognizes incomplete tag candidates with a left-hand artifact token and `->`
- [x] Gate coverage-tag completion to supported coverage-tag files using the existing supported-file list
- [x] Align the supported coverage-tag file list with the upstream OpenFastTrace Tag Importer default extensions
- [x] Gate completion to comment context where IntelliJ PSI provides reliable comment elements
- [x] Add a conservative fallback only for supported file types where comment PSI detection is unavailable or too weak
- [x] Replace only the target-side prefix under the caret when a completion item is accepted
- [x] Register completion contributors for the language contexts needed by currently supported coverage-tag files without broadening suggestions into ordinary specification documents incorrectly
- [x] Add implementation coverage tags for the new design items

### Automated Verification

- [x] Add platform tests showing completion suggestions inside Java comments for `[impl->dsn~partial<caret>]`
- [x] Add platform tests showing completion with optional spaces around the arrow
- [x] Add platform tests showing completion in an incomplete tag without a closing bracket
- [x] Add platform tests showing completion from an empty target prefix after the arrow on manual invocation
- [x] Add negative platform tests for caret before the arrow, after an already closed tag, outside a comment, inside a Java string literal, and in an unsupported file
- [x] Add focused unit tests for the coverage-tag completion context detector, including prefix extraction and bracket/arrow boundary cases
- [x] Add a regression test that compares the plugin coverage-tag file support against the upstream `TagImporterFactory` default extension list
- [x] Keep existing `Covers:` completion tests green to prove the shared ranking and scope behavior remain unchanged
- [x] Keep coverage-tag highlighting and navigation tests green to prove strict tag parsing semantics did not regress
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [x] Keep path coverage at or above the documented threshold
- [x] Keep dependency policy unchanged and avoid adding new third-party libraries
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [x] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean when the service responds normally

`./gradlew test` passes.

`./gradlew traceRequirements` passes.

`./gradlew check` passes, including `spotlessCheck`, `traceRequirements`, tests, and path coverage verification.

`./gradlew buildPlugin` passes.

`./gradlew verifyPlugin` still fails because of pre-existing experimental API usage in `OftHighlightingPass` and internal API usage in `OftTraceRunContentOutputPresenter` that are unrelated to GH-32.

`./gradlew --no-configuration-cache sonar` passes.

`./gradlew --warning-mode=all ossIndexAudit` reaches OSS Index but fails locally with HTTP 401 Unauthorized. Gradle also reports configuration-cache incompatibilities in that task, so a clean authenticated audit result is not available in this environment.

### Update User Documentation

- [x] Update `README.md` so users can discover specification item ID completion in coverage-tag targets
- [x] Update demo material only if the existing demo flow should show the new coverage-tag completion step

## Version And Changelog Update

- [x] Raise the version to the next feature release
- [x] Write the changelog entry for the release that includes GH-32
