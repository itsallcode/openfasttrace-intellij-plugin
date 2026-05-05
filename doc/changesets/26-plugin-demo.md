# GH-26 Plugin demo

## Goal

Create a presenter-ready demonstration for the OpenFastTrace IntelliJ plugin.

The demo should show the plugin's existing authoring, navigation, trace, and user-guide workflows through a small isolated example project under `doc/demo/example`, without changing the plugin's user-visible requirements or technical design.

## Scope

In scope:

* write a demo script with preparation and execution sections
* create the required demo project under `doc/demo/example`
* keep the example intentionally small: one Markdown specification and one bogus shell script
* use the lighthearted "excuse of the day" domain so the trace chain is easy for software developers to understand
* demonstrate bundled live templates, the `feat` chapter, symbol search, declaration navigation, red and green OFT traces, a source coverage tag, and the user-guide link
* make the checked-in starting state trace-clean for the repository even though the live demo intentionally creates failing traces while editing
* update end-user documentation so maintainers and presenters can find the demo material

Out of scope:

* changing plugin behavior, plugin code, or automated plugin tests
* changing `doc/system_requirements.md` or the design documents unless implementation findings reveal a real specification mismatch
* adding new product requirements for demo material
* recording or committing screencast files
* adding dependencies, build plugins, or custom demo tooling

## Design References

* [GitHub Issue #26](https://github.com/itsallcode/openfasttrace-intellij-plugin/issues/26)
* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [README](../../README.md)

## Strategy

Treat GH-26 as a documentation and demonstration change. The existing requirements and design already cover the plugin capabilities that the demo exercises: live templates, `Go to Symbol`, declaration navigation, project tracing, trace output, coverage tags, and the user-guide action.

Because `traceRequirements` scans `doc/`, the committed demo files must not leave dangling OFT needs or coverage links. The demo should start from a small green baseline and the script should tell the presenter exactly which edits create the intended red traces and how to return to a green trace. Use demo-specific item names so the example never covers or is covered by the plugin's product requirements.

## Task List

- [x] Create and checkout a new Git branch `feature/26_plugin-demo`

### Requirements And Design

- [x] Confirm GH-26 remains a documentation-only issue and is fully covered by existing product requirements for live templates, navigation, tracing, coverage tags, and user-guide access
- [x] Confirm no change is needed in `doc/system_requirements.md`
- [x] Confirm no change is needed in `doc/design.md` or linked design chapters
- [x] Confirm implementation findings did not contradict those requirements and design decisions

### Demo Project

- [x] Create `doc/demo/example` as the isolated demo project directory
- [x] Add a Markdown specification file for the "excuse of the day" demo
- [x] Add a bogus shell script that contains the source-side OFT coverage tag used in the final green trace
- [x] Keep the example to exactly those two demo files unless a reviewer approves an additional helper file
- [x] Use demo-specific OFT item IDs that do not overlap with product specification IDs
- [x] Make the committed starting state trace-clean when scanned as part of this repository's `doc/` tree
- [x] Make the demo suitable for opening `doc/demo/example` as its own IntelliJ project during the presentation

### Demo Script

- [x] Add a presenter-facing demo script with preparation and execution sections
- [x] Include preparation steps for installing IntelliJ Presenter Plugin, installing the OpenFastTrace plugin from a GitHub release, and opening the demo project
- [x] Refine the execution flow from the issue into a concise live sequence with expected red and green trace outcomes
- [x] Include the live-template step for adding a `feat` item
- [x] Include the step that shows the existing feature chapter
- [x] Include symbol search for OFT specification item IDs
- [x] Include `Go To Declaration` navigation from an OFT reference
- [x] Include trace runs after each meaningful edit and state which runs should fail or pass
- [x] Include the step that adds the final coverage tag in the demo shell script
- [x] Include the final step that opens or points to the user guide
- [x] Keep the script short enough for a live demonstration and clear enough for later screencast recording

### Update User Documentation

- [x] Update `README.md` so users can find the demo script and the example project
- [x] Replace or supplement the existing `examples/` mention if `doc/demo/example` becomes the preferred manual demo entry point

### Verification

- [x] Run `./gradlew traceRequirements` and keep the repository OpenFastTrace trace clean
- [x] Run the demo trace against `doc/demo/example` in its committed starting state and verify it is green
- [x] Manually rehearse the demo script in an IDE with the plugin installed, verifying the intended red and green trace transitions
- [x] Keep dependency policy unchanged and avoid adding third-party libraries
- [x] Confirm required build and plugin verification tasks are not additionally required because implementation touched only documentation and demo files

## Version And Changelog Update

- [x] Add a change log entry for version 0.5.1 (documentation release, no features)
