# GH-10 MVP

## Goal

Implement the MVP authoring support for the OpenFastTrace IntelliJ plugin.

## Scope

In scope:

* syntax highlighting for OpenFastTrace specification items in `.md`, `.markdown`, and `.rst`
* syntax highlighting for OpenFastTrace coverage tags in supported tag-importer file types
* project-local indexing of specification items
* navigation to specification items through Go to Symbol and Search Everywhere
* Help menu action that opens the OpenFastTrace user guide in the integrated web view

Out of scope:

* OpenFastTrace library integration for tracing logic
* tracing reports and batch validation
* inspections, intentions, tool windows, notifications, and settings pages
* telemetry and custom persisted configuration

## Design References

* [Context and Scope](../design/context_and_scope.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Design Decisions](../design/architecture_decisions.md)

## Task List

### Build Setup

- [ ] Create the Gradle-based IntelliJ Platform plugin build using the IntelliJ Platform Gradle Plugin
- [ ] Configure plugin metadata, target platform baseline, and compatibility range for current and later IDE versions
- [ ] Add the minimum approved dependencies for the MVP implementation and test setup

### OFT Syntax Core

- [ ] Implement shared recognition for valid, invalid, and incomplete OpenFastTrace specification items
- [ ] Implement shared recognition for valid, invalid, and incomplete OpenFastTrace coverage tags
- [ ] Keep the syntax core independent from editor-specific extension-point code

### Editor Support

- [ ] Implement Markdown specification highlighting for `.md` and `.markdown`
- [ ] Implement RST specification highlighting for `.rst`
- [ ] Implement coverage-tag highlighting for supported tag-importer file types
- [ ] Ensure incomplete OpenFastTrace fragments do not block editing

### Navigation

- [ ] Implement project-local extraction and indexing of OpenFastTrace specification items
- [ ] Implement Go to Symbol contribution for specification items
- [ ] Implement opening the selected specification item at its definition from Go to Symbol and Search Everywhere

### Help Integration

- [ ] Register the OpenFastTrace user guide action in the global Help menu
- [ ] Open the configured OpenFastTrace user guide URL in the integrated IDE web view

### Verification

- [ ] Add automated plugin tests for Markdown highlighting scenarios
- [ ] Add automated plugin tests for RST highlighting scenarios
- [ ] Add automated plugin tests for coverage-tag highlighting scenarios
- [ ] Add automated plugin tests for specification-item indexing and navigation scenarios
- [ ] Add automated plugin tests for the user-guide action behavior
- [ ] Keep automated test coverage at or above the required threshold
- [ ] Keep the OpenFastTrace trace clean for the requirement and design artifact types in scope
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean
- [ ] Keep IntelliJ Plugin Verifier checks green for the supported IDE builds
