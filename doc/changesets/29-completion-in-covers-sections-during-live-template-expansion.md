# GH-29 Completion in Covers Sections During Active Live-Template Expansion

## Goal

Make OFT specification item ID completion available while a user is still editing an expanded OpenFastTrace live template.

Users should be able to expand a bundled OFT template, type a partial specification item ID into the active `COVERED` placeholder, invoke completion, and select an existing declaration without leaving live-template mode.

## Scope

In scope:

* enable completion for `COVERED` placeholders in bundled OFT live templates that insert `Covers:` entries
* keep completion candidates sourced from the existing declaration index and ranked by the existing `Covers:` completion policy
* add an automated IntelliJ fixture test that expands a relevant OFT live template and verifies completion while the template is active
* update requirements and design where the existing completion and live-template specifications need an explicit active-template scenario
* keep the fix within IntelliJ's standard live-template and completion infrastructure

Out of scope:

* redesigning the OFT completion matcher, ranking policy, or declaration index
* adding completion for sections other than `Covers:`
* changing live-template placeholders that are unrelated to covered specification items
* adding a custom completion popup or template-editing UI
* synchronizing bundled templates automatically with external OFT template repositories

## Design References

* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [GH-22 Bundle Live Templates](22-bundle-live-templates.md)
* [GH-24 Auto-completion for "Covers" section](24-auto-completion-for-covers-section.md)

## Strategy

Treat GH-29 as an integration bug between two existing user-facing capabilities: bundled OFT live templates and `Covers:` reference completion.

The implementation keeps the existing completion contributor as the single source of completion behavior. The active editor document must be used for completion-context checks so live-template edits are visible before the template session finishes.

The bundled templates also use blank formatting lines between some `Covers:` keywords and their bullet entries. Keep the completion context detector inside the `Covers:` section across those blank spacer lines so all affected templates behave the same way while a live-template field is active.

The bundled templates should not force IntelliJ completion automatically when the `COVERED` field receives focus. In IntelliJ versions that enable full-line inline completion, forcing completion from the live-template macro can trigger JetBrains' inline completion provider before it has a supported language holder for the editor context.

The test should exercise the real platform integration, not only template metadata. It should expand at least one bundled template with a `COVERED` placeholder, keep the template session active, type a partial OFT ID into that field, invoke basic completion, and verify that an indexed declaration is suggested.

## Task List

- [x] Create and checkout a new Git branch `bugfix/29-live-template-covers-completion`

### Requirements And Design

- [x] Extend `doc/system_requirements.md` with a scenario for completing an OFT specification item ID in an active live-template `COVERED` field under `Covers:`
- [x] Stop and ask user for a review of the system requirements
- [x] Update `doc/design/solution_strategy.md` only if the current reuse-of-IDE-facilities text does not sufficiently cover completion triggered from live-template variables
- [x] Update `doc/design/building_block_view.md` so the live-template integration and specification-item completion responsibilities cover the active-template scenario without duplicating implementation detail
- [x] Update `doc/design/runtime_view.md` with a runtime design item for completion from an active live-template `COVERED` field, or extend the existing runtime items if forwarding is sufficient
- [x] Stop and ask user for a review of the design

### Live-Template Integration

- [x] Keep bundled `COVERED` variables in `src/main/resources/liveTemplates/OpenFastTrace.xml` editable during live-template editing
- [x] Avoid forcing IntelliJ completion from the bundled `COVERED` variables during live-template field focus
- [x] Keep the bundled template text, group name, abbreviations, variable expressions, and supported contexts unchanged
- [x] Add or update implementation coverage tags for the active-template completion design item
- [x] Keep `Covers:` section detection active across blank spacer lines used by the bundled templates

### Automated Verification

- [x] Add an IntelliJ platform test that expands a bundled OFT template, keeps the live-template session active at `COVERED`, invokes basic completion, and verifies that indexed declaration IDs are suggested
- [x] Add regression coverage for completion when a blank line separates `Covers:` from the edited entry
- [x] Keep the existing plain `Covers:` completion tests green to prove the contributor still works outside live-template mode
- [x] Keep the existing live-template registration and template-text tests green
- [x] Add metadata-level checks that every bundled template with a `COVERED` variable avoids forced completion expressions
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [x] Keep path coverage at or above the documented threshold
- [x] Keep dependency policy unchanged and avoid adding new third-party libraries
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean when the service responds normally

`./gradlew test` passes.

`./gradlew traceRequirements` passes.

`./gradlew check buildPlugin` passes, including `spotlessCheck`, `traceRequirements`, tests, path coverage verification, and plugin packaging.

`./gradlew verifyPlugin` still fails because of pre-existing experimental API usage in `OftHighlightingPass` and internal API usage in `OftTraceRunContentOutputPresenter` that are unrelated to GH-29.

`./gradlew --warning-mode=all ossIndexAudit` reaches OSS Index but fails locally with HTTP 401 Unauthorized. Gradle also reports configuration-cache incompatibilities in that task, so a clean authenticated audit result is not available in this environment.

`./gradlew sonar` fails locally with `ClosedFileSystemException` during IntelliJ test runtime resolution when the Gradle configuration cache is enabled. `./gradlew --no-configuration-cache sonar` passes.

### Update User Documentation

- [x] Update `README.md` so the documented live-template workflow mentions that the `COVERED` field can use specification ID completion while the template is active

## Version And Changelog Update

- [x] Confirm the current release baseline and raise the plugin version to the next patch release for this bug fix if needed
- [x] Write the changelog entry for GH-29
