# GH-74 Fix Test Runner count to include specification items only

## Goal

Make the IntelliJ Test Runner total, completion progress, and failed-result count
represent visible OpenFastTrace specification items only. Source-file suites and
trace links remain available as structural and expandable detail nodes, without
inflating the logical result count or preventing a clean trace from reaching
100% completion.

## Scope

In scope:

* Count each visible specification-item entry once in the Test Runner result
  total, excluding source-file suites and displayed trace-link children.
* Report item-only custom progress so a clean trace with expandable item nodes
  reaches 100% completion.
* Count an item with one or more defective visible trace links as one failed
  logical result, while retaining failed link details in the tree.
* Preserve the existing source-file suite, specification-item, and trace-link
  hierarchy, status details, and navigation.
* Update the traced requirements and runtime/building-block design for the new
  counting and progress semantics.

Out of scope:

* Changing OpenFastTrace trace evaluation, defect classification, or
  transitive-defect filtering.
* Removing trace-link nodes, changing their labels, or changing navigation and
  defect-detail behavior.
* Changing the plain-text trace result view or the global trace action.
* Adding third-party dependencies.

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [GH-40 Integrate OFT Trace into Test Runner UI](40-integrate-oft-trace-into-test-runner-ui.md)
* [GH-72 Add filter for transitive tracing defects](72-add-filter-for-transitive-tracing-defects-in-run-config.md)

## Strategy

Keep the existing SM test tree and its normal lifecycle events for rendering
source-file suites, expandable specification items, and trace-link details. Add
a separate custom-progress lifecycle whose total and completed/failed outcomes
are derived only from visible `OftTraceItemNode` instances. This explicitly
excludes source-file suites and link-detail nodes from aggregate counting while
preserving all existing tree behavior. A defective link rolls up to its owning
item for the single logical failure outcome.

## Task List

- [X] Create and checkout a new Git branch `bugfix/74-fix-test-runner-count`

### Requirements And Design

- [x] Revise `req~show-trace-source-files-as-test-runner-suites` and its scenario to state that source-file suites structure the tree but are not logical Test Runner results.
- [x] Revise `req~show-trace-specification-items-as-test-runner-tests` and its scenario to state that each visible specification item is one logical Test Runner result.
- [x] Revise `req~show-trace-links-as-test-runner-sub-tests` and its scenario to describe trace links as visible detail children that do not contribute to the result total.
- [x] Revise `req~map-specification-item-trace-status-to-test-runner-status` and add a scenario covering an item with a defective visible link being one failed logical result.
- [x] Add a requirement and clean-trace scenario for an item-only Test Runner total and 100% completion when source-file suites and items with visible links are present.
- [x] Stop and ask user for a review of the system requirements.
- [x] Update `doc/design/runtime_view.md` with separate design items for item-only custom progress and rolling visible-link defects up to an owning item; each runtime design item covers one scenario.
- [x] Update `doc/design/building_block_view.md` so the Test Runner presentation declares that it separates logical item progress from link-detail rendering.
- [x] Revise the aggregate `dsn~trace-test-runner-presentation` description and its implementation/test trace links if its semantics change.
- [ ] Stop and ask user for a review of the design.

### Implementation

- [ ] Change `OftTraceTestTree` counting so `testCount()` counts visible specification items only, never source-file suites or links; retain link collections and item failure roll-up.
- [ ] Extend `OftTraceTestRunnerOutputPresenter` to report the item-only custom total and one custom started/failed/finished lifecycle per visible specification item, never for source-file suites or links.
- [ ] Retain normal SM events for source suites, item nodes, and trace-link child nodes so hierarchy, details, navigation, and existing pass/fail rendering remain unchanged.
- [ ] Ensure a defective item or any defective visible link produces exactly one failed custom-progress result for its owning specification item.
- [ ] Keep fallback presentation for results without a structured `Trace` unchanged unless IntelliJ custom-progress semantics require an isolated compatibility adjustment.

### Verification

- [ ] Update tree-mapper tests to prove source-file suites and link children contribute no logical results, while linked trace items contribute one result each and link children remain present with their status.
- [ ] Add presenter tests for a clean trace with multiple source-file suites and linked items: item-only total and 100% completion.
- [ ] Add presenter tests for a defective link: unchanged visible link failure plus one failed logical owning-item result.
- [ ] Keep tests for source suites, item/link labels, defect details, navigation, transitive-defect filtering, and result-without-trace fallback green.
- [ ] Use JUnit 5 and Hamcrest; follow given-when-then test names, single/assert-all assertion guidance, and parameter-validation rules where applicable.
- [ ] Keep path coverage at or above 80%.
- [ ] Keep the OpenFastTrace trace clean for affected `feat`, `req`, `scn`, `dsn`, `impl`, and `itest` artifacts.
- [ ] Run `./gradlew test`.
- [ ] Run `./gradlew check`.
- [ ] Run `./gradlew verifyPlugin`.
- [ ] Keep the SonarQube Cloud quality gate green after CI analysis.

### Update User Documentation

- [ ] Update `README.md` if its Test Runner description needs to explain that totals and progress count specification items, while source-file suites and links are structural/detail nodes.

## Version and Changelog Update

- [ ] Check whether the current build version and the latest GitHub release are both `0.10.0` before preparing the release.
- [ ] Treat GH-74 as a bugfix and raise the version to `0.10.1` if that check confirms `0.10.0` is the current release.
- [ ] Add the `0.10.1` changelog entry with GH-74 under `## Bugfix`.
- [ ] Determine the resolved `org.itsallcode.openfasttrace:openfasttrace` version from Gradle dependency metadata.
- [ ] Add `## Bundled OpenFastTrace` and the resolved library version immediately after the release summary and before the issue category.
- [ ] Update the release date when releasing and ensure the issue list contains `#74: Fix Test Runner count to include specification items only`.
