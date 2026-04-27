# GH-12 Run OFT trace from the plugin

## Goal

Run an OpenFastTrace trace for the currently opened IntelliJ project directly from the plugin and show the textual trace result inside the IDE.

The implementation should let users start the trace from the `Tools` menu, execute it without blocking the UI, and inspect the OFT text output in an IDE output sub-window.

## Scope

In scope:

* add the OpenFastTrace tracing library as a runtime dependency for the plugin
* contribute an OpenFastTrace action group under the global `Tools` menu
* add an action that traces the currently opened project
* determine the project input paths for the trace from the opened IntelliJ project
* execute the trace off the EDT with IDE progress reporting
* show the OFT text trace output in an IDE output sub-window
* report trace success and failure in a way that is visible from the IDE action flow
* update the requirements and design documentation for the new tracing capability

Out of scope:

* a dedicated OFT reporter module that maps results into the IntelliJ Problems view
* inline editor annotations, inspections, or intentions based on trace findings
* automatic tracing on file save, build, or project open
* user-configurable trace settings pages, custom argument editors, or persistent profiles
* HTML or structured report rendering beyond the plain OFT text output required by this issue

## Design References

* [System Requirements](../system_requirements.md)
* [Context and Scope](../design/context_and_scope.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Design Decisions](../design/architecture_decisions.md)

## Strategy

Reuse IntelliJ Platform action, background-task, and console-style output facilities instead of building custom tracing infrastructure.

The plugin should integrate the latest compatible OpenFastTrace library from Maven Central at implementation time and invoke tracing in-process from the plugin. The action entry belongs under `Tools`, grouped under `OpenFastTrace`, so later OFT actions can be added without reshuffling the menu structure.

The first implementation should trace the opened project with project-derived input paths and render the plain OFT text report in an IDE output sub-window. Richer result interpretation is intentionally deferred to the later reporter-module stage described in the issue.

## Task List

### Requirements And Design

- [x] Extend `doc/system_requirements.md` with user-facing requirements and scenarios for running a trace from the plugin
- [x] Update the design documentation so tracing is no longer treated as out of scope for the product after the MVP
- [x] Add or update runtime and building-block design items for trace action handling, trace execution, and trace-output presentation
- [x] Stop here and require user review

### Build And Dependency Setup

- [x] Determine the latest compatible OpenFastTrace library version from Maven Central for this plugin
- [x] Add the OpenFastTrace library dependency to the Gradle build
- [x] Keep plugin packaging, verification, and test setup compatible with the added dependency

### IDE Action Integration

- [x] Add an `OpenFastTrace` group to the global `Tools` menu
- [x] Add a `Trace Project` action under that group
- [x] Find a free keyboard shortcut and add it to the action
- [x] Ensure the action is available only when an IntelliJ project is open
- [x] Keep the action text and description aligned with the issue wording and IDE menu conventions

### Trace Input Resolution

- [x] Define how the plugin derives OFT trace inputs from the opened project
- [x] Start with the current project content rooted at the opened project directory unless implementation findings require a narrower default
- [x] Handle missing or invalid project base paths gracefully before starting the trace

### Trace Execution

- [x] Implement a trace service that invokes the OFT library from plugin code
- [x] Run the trace in a background task so the UI stays responsive
- [x] Provide progress feedback and cancellation behavior consistent with IntelliJ background actions
- [x] Capture the OFT text output and the final success or failure status from the trace run

### Output Presentation

- [x] Show the OFT text trace in an IDE output sub-window that can be reopened after the action completes
- [x] Give each trace run a clear tab or content title so users can identify the output source
- [x] Surface execution failures and exceptional conditions in the output flow without requiring log-file inspection
- [x] Keep the first implementation focused on plain text output rather than structured problem navigation

### Automated Verification

- [x] Add automated tests for the `Tools` menu action registration and invocation path
- [x] Add automated tests for project-path resolution and invalid-project handling
- [x] Add automated tests for successful trace execution and text-output capture
- [x] Add automated tests for failing trace execution and error reporting
- [x] Keep automated test coverage at or above the required threshold
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifact types in scope
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean
- [x] Keep IntelliJ Plugin Verifier checks green for the supported IDE builds

### User Documentation

- [x] Update the Readme with an explanation how to run the test
- [x] Update the change log
