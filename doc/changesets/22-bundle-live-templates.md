# GH-22 Bundle Live Templates

## Goal

Bundle the existing OpenFastTrace IntelliJ live templates with the plugin so they are available immediately after installation, and extend that bundled template set with a missing scenario template.

The implementation should keep the templates close to the plugin codebase, register them through the IntelliJ Platform in a way that works from the installed plugin package, and document the intended sync point with the existing external template source.

## Scope

In scope:

* add a user-visible plugin capability for bundled OFT live templates
* keep a copy of the OFT live-template XML inside this repository and package it with the plugin
* register the bundled template set in the plugin so it becomes available after plugin installation
* add a new live template for OFT scenarios alongside the existing bundled templates
* update requirements, design, and end-user documentation for the new template support

Out of scope:

* a custom live-template editor, wizard, or settings UI beyond IntelliJ's standard live-template facilities
* automatic synchronization with the external `openfasttrace-ide-templates` repository during the build
* redesigning all existing OFT templates beyond the minimal changes required to bundle them and add the scenario template
* context-aware template expansion logic beyond what IntelliJ live templates already provide declaratively

## Design References

* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)

## Strategy

Reuse IntelliJ's built-in live-template extension mechanism instead of building plugin-specific authoring infrastructure.

The plugin should package one repository-owned OFT live-template definition file under `src/main/resources`, register that resource from `plugin.xml`, and keep the XML readable enough that future template changes can be maintained locally. The bundled copy should stay aligned with the existing external OFT templates, but GH-22 intentionally keeps synchronization as a documented manual maintenance step instead of introducing a new build-time download or generation path.

## Task List

### Requirements And Design

- [x] Extend `doc/system_requirements.md` with a user-visible feature and requirements for bundled OFT live templates
- [x] Add or update scenarios that cover template availability after plugin installation and insertion of an OFT scenario template in a supported editing context
- [x] Review the existing imported OFT templates and propose minimally invasive UX improvements that stay within standard IntelliJ live-template capabilities and do not require new editor-intelligence features
- [x] Update the solution strategy to state that the plugin reuses IntelliJ live-template infrastructure for OFT authoring assistance
- [x] Add a building-block design item for live-template integration and describe its relationship to plugin resources and IntelliJ template facilities
- [x] Add a runtime design item that describes how the IDE discovers the bundled OFT template set and makes it available to the user after the plugin is loaded

### Template Bundle Integration

- [x] Copy the existing OFT live-template XML from `openfasttrace-ide-templates` into a plugin-owned resource location under `src/main/resources`
- [x] Add the missing OFT scenario live template to the bundled XML and keep the naming, placeholders, and OFT notation aligned with the existing template set
- [x] Register the bundled live-template resource in [plugin.xml](/home/seb/git/openfasttrace-intellij-plugin/src/main/resources/META-INF/plugin.xml)
- [x] Confirm the Gradle plugin packaging includes the live-template resource in the built plugin artifact without introducing new dependencies

### Automated Verification

- [x] Add automated plugin tests that verify the bundled OFT live-template group is available when the plugin is loaded
- [x] Add automated tests that verify the bundled template set includes the new scenario template and that its expansion text matches the intended OFT scenario structure
- [x] Add or update packaging-oriented verification so missing live-template resources or broken plugin registration fail during automated checks
- [x] Keep the OpenFastTrace trace clean for the new requirement and design artifact types in scope
- [x] Keep path coverage at or above the documented threshold
- [x] Keep dependency policy unchanged and avoid adding new third-party libraries unless a design decision is approved first
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean

`./gradlew verifyPlugin` still fails because of pre-existing internal API usages in the trace output presenter that are unrelated to GH-22.

### Update User Documentation

- [x] Update `README.md` so users can find and use the bundled OFT live templates after installing or launching the plugin
- [x] Document the new scenario template together with the general live-template availability in the end-user documentation

## Version And Changelog Update

- [x] Raise the version to `0.4.0` for the next feature release
- [x] Write the changelog entry for `0.4.0`
