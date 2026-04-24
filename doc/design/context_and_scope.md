# Context and Scope

This chapter describes the technical context of the plugin, including neighboring systems and external interfaces.

Terms such as `plugin`, `OpenFastTrace`, and `OFT` use the definitions from [System Requirements](../system_requirements.md).

## System Boundary

The plugin is an IntelliJ Platform plugin that runs inside JetBrains IDEs. The plugin logic stays inside the host IDE process and uses IntelliJ Platform extension points, services, indexes, editors, and navigation facilities.

The MVP scope is limited to authoring support for OpenFastTrace documents and coverage tags inside the IDE. User-visible authoring features are defined in [System Requirements](../system_requirements.md). This chapter focuses on the technical system boundary behind those features.

## Supported Host Environment

The plugin targets JetBrains IDEs based on the IntelliJ Platform. This includes IntelliJ IDEA, PyCharm, CLion, and other compatible JetBrains IDEs.

The plugin supports both Community and commercial editions where the required shared platform modules are available. This supports the OpenFastTrace goal of serving open source projects without restricting the plugin to commercial IDE editions.

The compatibility target starts with the current IntelliJ Platform baseline and later versions. Backward compatibility for older IDE releases is not part of the initial scope and becomes relevant only after the first `1.0.0` release.

## Project And File Scope

For the MVP, the plugin operates only on the files of the currently opened IDE project. It does not scan external repositories, remote content, standalone shared specification stores, or other projects outside the active IDE project context.

The MVP supports OpenFastTrace specification items in Markdown and reStructuredText documents with the suffixes `.md`, `.markdown`, and `.rst`.

The MVP also supports OpenFastTrace coverage tags in source, configuration, and markup files supported by the OFT tag importer. The current list of supported file extensions is defined in the [OpenFastTrace user guide](https://github.com/itsallcode/openfasttrace/blob/main/doc/user_guide.md#supported-file-extensions) and is referenced there instead of being duplicated in this design document.

## External Interfaces

The primary technical context is the IntelliJ Platform itself. The plugin integrates with the editor infrastructure, PSI and parsing infrastructure, project model, symbol search, navigation, help actions, and the IDE state stores and caches that already exist in the platform.

The MVP stays as self-contained as possible. It does not require an external OpenFastTrace CLI installation, a local or remote tracing service, telemetry backends, or custom storage outside the standard IDE persistence mechanisms.

Network access in the MVP is limited to opening the OpenFastTrace user guide from its GitHub page. A later version may replace this with a bundled local copy of the guide.

## State And Persistence

There will be **no** telemetry.

The MVP does not introduce custom user preferences or project-specific configuration because the initial feature set does not require configuration options.

The plugin relies on the standard IntelliJ Platform caches and state stores where the host IDE already persists indexes, plugin state, and related runtime data.

## Explicit MVP Non-Goals

The MVP does not execute OpenFastTrace tracing logic, generate tracing reports, perform batch validation, call the OpenFastTrace library, or integrate with an external OFT process.

The MVP does not add broader IDE integrations such as inspections, intentions, tool windows, notifications, custom settings pages, or other advanced platform features beyond the minimum required authoring support.

The product strategy favors low complexity, predictable behavior, and usefulness for everyday engineering work over feature breadth. The plugin avoids add-on features that increase maintenance cost or distract from the core authoring workflow.

Any feature that risks making the IDE feel slow or unresponsive is outside the acceptable scope. Detailed performance expectations and verification belong to [Quality Requirements](quality_requirements.md).
