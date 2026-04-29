# System Requirements

## Introduction

OpenFastTrace users create and maintain specification documents directly in their development projects. This work requires careful handling of specification item IDs, trace links, and document structure across Markdown and reStructuredText files. When authoring support is missing, users must rely on memory, manual searches, and repeated checks against external documentation. This slows down routine work and increases the risk of inconsistent or hard-to-maintain specifications.

The IntelliJ plugin makes authoring OpenFastTrace documents easier in the environment where users already inspect and change their project files. Users recognize OpenFastTrace elements quickly, find specification items across the project, navigate directly to the item they need, and open the official user guide without leaving the IDE. This improves efficiency for individual contributors and teams alike, especially when they work with larger requirement sets or exchange specification documents across project boundaries.

## Notation

This document uses OpenFastTrace specification items to express product features, user requirements, scenarios, and design items. Each specification item has a unique identifier in the form `<artifact-type>~<name>~<revision>`.

In this document, feature items use the artifact type `feat`, user requirements use `req`, scenarios use `scn`, and design items use `dsn`. The item name describes the subject of the item in a project-wide unique form. The revision distinguishes semantically different versions of the same item.

Informative text explains background, scope, and intent. Specification items define the normative content of the document. Relationships between items are expressed with OpenFastTrace keywords such as `Needs` and `Covers`.

## Terms and Abbreviations

### IntelliJ Platform IDE

An integrated development environment based on the IntelliJ Platform. In this document, this includes IntelliJ IDEA and other compatible JetBrains IDEs that can host the plugin.

### MVP

Minimum Viable Product. The smallest useful product scope that delivers the essential user value intended for the first release.

### OFT

Short form of OpenFastTrace.

### OpenFastTrace

A requirement tracing suite that uses specification documents and coverage markers to relate features, requirements, design, implementation, and tests.

### Plugin

The OpenFastTrace IntelliJ plugin described by this specification.

### RST

Short form of reStructuredText.

### Specification Item

An OpenFastTrace artifact with a unique identifier that represents normative content in a specification or a coverage marker in another project artifact.

## User Roles

The plugin addresses users who author, review, and maintain OpenFastTrace documents as part of their regular project work. In smaller projects one person often combines several of these roles.

This document uses the general term `user` whenever a feature does not depend on a specific role. This keeps role-independent requirements concise while the role definitions still define the intended audience and project context.

### Requirement Engineers

Requirement engineers define and maintain system requirements, refine incoming stakeholder needs, and keep specification items consistent across documents. They benefit from quick recognition of OpenFastTrace syntax, a project-wide view of specification items, and direct navigation to referenced items when reviewing or updating requirement structures.

### Software Developers

Software developers work with specification documents while implementing and clarifying features. They use the plugin to understand which specification items exist in the project, locate the relevant item quickly, and keep authoring work close to the source code and tests they already work with in the IDE.

### Quality Engineers

Quality engineers inspect specification documents to understand intended behavior, review completeness, and prepare verification activities. They benefit from being able to discover specification items across the project and navigate to the exact item that defines the behavior they need to verify.

### Technical Writers

Technical writers contribute to specification documents, user-facing descriptions, and other structured project documentation. They benefit from clear visual feedback when authoring OpenFastTrace content and from direct access to the official user guide when they need to confirm wording or document conventions.

## Features

This chapter describes the product features at a level suitable for product communication. Detailed user needs and constraints are refined in the requirement items that cover these features.

### Syntax Highlighting
`feat~syntax-highlighting~1`

The plugin highlights OpenFastTrace content in supported specification documents and source code comments. Users recognize OpenFastTrace syntax directly in the editor.

Needs: req

### Go to Specification Item
`feat~go-to-specification-item~1`

The plugin lets users search specification items by name across the project and navigate from OpenFastTrace references in the editor. Users can select a matching item from the result list or invoke Go To on a specification reference or coverage-tag side and open the corresponding definition in the editor.

Needs: req

### Open OFT User Guide
`feat~open-oft-user-guide~1`

The plugin lets users open the OpenFastTrace user guide from within the IDE. Users can access the guide without leaving their current work context.

Needs: req

### OFT Live Templates
`feat~oft-live-templates~1`

The plugin bundles OpenFastTrace live templates for common specification items. Users can insert OFT item skeletons directly in the IDE instead of copying and adapting existing text manually.

Needs: req

### OFT Reference Completion
`feat~oft-reference-completion~1`

The plugin suggests existing OpenFastTrace specification item IDs while users fill `Covers:` entries in supported specification documents. Users can complete references from indexed declarations instead of memorizing or manually searching for IDs.

Needs: req

### Run OFT Trace
`feat~run-oft-trace~1`

The plugin lets users run an OpenFastTrace trace for the currently opened IntelliJ project and inspect the textual result inside the IDE.

Needs: req

## User Requirements

### Syntax Highlighting

The following requirements refine the syntax-highlighting feature into user-visible capabilities.

### Markdown Syntax Highlighting
`req~markdown-syntax-highlighting~1`

The plugin highlights OpenFastTrace specification items in Markdown documents. Users distinguish OpenFastTrace syntax from surrounding Markdown text while reading or editing a specification.

Covers:
- `feat~syntax-highlighting~1`

Needs: scn

### RST Syntax Highlighting
`req~rst-syntax-highlighting~1`

The plugin highlights OpenFastTrace specification items in RST documents. Users distinguish OpenFastTrace syntax from surrounding RST text while reading or editing a specification.

Covers:
- `feat~syntax-highlighting~1`

Needs: scn

### Coverage Tag Syntax Highlighting
`req~coverage-tag-syntax-highlighting~1`

The plugin highlights OpenFastTrace coverage tags in supported source code comments. Users distinguish OFT coverage tags from ordinary comment text while reading or editing source files.

Covers:
- `feat~syntax-highlighting~1`

Needs: scn

### Go to Specification Item

The following requirements refine the 'Go to Specification' Item feature into user-visible capabilities.

### Go to Symbol for Specification Items
`req~go-to-symbol-for-specification-items~1`

The plugin contributes OpenFastTrace specification item declarations to `Go to Symbol`.

Rationale:

Users can search specification items by full ID and see matching declarations in the result list.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Recognize Markdown Specification Item Declaration Variants
`req~recognize-markdown-specification-item-declaration-variants~1`

The plugin recognizes an OpenFastTrace specification item declaration in a Markdown document both when the full OFT item ID appears as the plain declaration line and when the full OFT item ID is enclosed in single backticks on that declaration line.

Rationale:

Users can choose either Markdown style without losing declaration-based search and navigation.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Open Specification Item from Go to Symbol
`req~open-specification-item-from-go-to-symbol~1`

The plugin opens the selected OpenFastTrace specification item from the `Go to Symbol` result list in the editor at the declaration of that item. Users can jump from the result list to the defining item ID of the selected specification item.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Open Specification Item from Coverage Definition
`req~open-specification-item-from-coverage-definition~1`

The plugin opens the covered OpenFastTrace specification item when a user invokes `Go To Declaration` on an OFT item ID under `Covers:` in a supported specification document.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Stay on Specification Item Declaration on Go To Declaration
`req~stay-on-specification-item-declaration-on-go-to-declaration~1`

The plugin keeps the user on the current OpenFastTrace specification item declaration when a user invokes `Go To Declaration` on the declared item ID itself. Users can distinguish the declaration from coverage-providing occurrences of the same item.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Show Covering Occurrences from Specification Item Declaration
`req~show-covering-occurrences-from-specification-item-declaration~1`

The plugin shows the coverage-providing occurrences of an OpenFastTrace specification item when a user invokes `Go To Implementations` on the declared item ID. The result includes supported `Covers:` entries in specification documents and OFT coverage tags in source files.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Open Specification Item from Coverage Tag Left Side
`req~open-specification-item-from-coverage-tag-left-side~1`

The plugin resolves the covering OpenFastTrace specification item when a user invokes `Go To Declaration` on the left side of an OFT coverage tag in a supported file and opens that item's declaration in the specification document. If the left side omits the name and revision, the plugin resolves the effective ID by copying the missing parts from the covered ID on the right side of the arrow.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Open Specification Item from Coverage Tag Right Side
`req~open-specification-item-from-coverage-tag-right-side~1`

The plugin opens the covered OpenFastTrace specification item when a user invokes Go To on the right side of an OFT coverage tag in a supported file.

Covers:
- `feat~go-to-specification-item~1`

Needs: scn

### Open OFT User Guide

The following requirements refine the Open OFT User Guide feature into user-visible capabilities.

### Open OFT User Guide in Help Menu
`req~open-oft-user-guide-in-help-menu~1`

The plugin adds an action for the OpenFastTrace user guide to the global Help menu. Users can find the user guide entry in the established IDE location for help content.

Covers:
- `feat~open-oft-user-guide~1`

Needs: scn

### Open OFT User Guide in Integrated Web View
`req~open-oft-user-guide-in-integrated-web-view~1`

The plugin opens the OpenFastTrace user guide from GitHub in an integrated web view tab. Users can read the guide inside the IDE.

Covers:
- `feat~open-oft-user-guide~1`

Needs: scn

### OFT Live Templates

The following requirements refine the OFT live-template feature into user-visible capabilities.

### Bundle OFT Live Templates
`req~bundle-oft-live-templates~1`

The plugin bundles an OpenFastTrace live-template group into the IDE installation. Users can access OFT live templates without importing template files manually.

Covers:
- `feat~oft-live-templates~1`

Needs: scn

### Provide OFT Scenario Live Template
`req~provide-oft-scenario-live-template~1`

The bundled OpenFastTrace live-template group includes a scenario template for `scn` items. Users can insert a scenario skeleton with placeholders for the scenario title, item name, given-when-then text, and covered requirement.

Covers:
- `feat~oft-live-templates~1`

Needs: scn

### OFT Reference Completion

The following requirements refine the OFT reference-completion feature into user-visible capabilities.

### Complete Specification Item IDs in Covers Section
`req~complete-specification-item-ids-in-covers-section~1`

The plugin suggests existing OpenFastTrace specification item IDs when a user invokes completion while editing an OFT item ID under `Covers:` in a supported specification document. The suggestion list is ranked first by exact prefix match against the full ID, then by prefix match against the item name, then by substring match against the item name, and finally by prefix match against the artifact type.

Covers:
- `feat~oft-reference-completion~1`

Needs: scn

### Run OFT Trace

The following requirements refine the Run OFT Trace feature into user-visible capabilities.

### Show Trace Project Action in Tools Menu
`req~show-trace-project-action-in-tools-menu~1`

The plugin adds an `OpenFastTrace` group with a `Trace Project` action to the global `Tools` menu. Users can find the trace entry in the established IDE location for project-level tooling actions.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Disable Trace Project Action without Open Project
`req~disable-trace-project-action-without-open-project~1`

The plugin disables the `Trace Project` action when no IntelliJ project is open.

Rationale:

Users cannot invoke the trace action when the IDE has no project context to trace.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Trace Open Project from Project Root
`req~trace-open-project-from-project-root~1`

The plugin traces the currently opened IntelliJ project by using the opened project directory as the default OpenFastTrace input root. Users can run a whole-project trace without manually selecting files and directories first.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Configure Trace Scope in Project Settings
`req~configure-trace-scope-in-project-settings~1`

The plugin integrates OpenFastTrace trace-scope settings into IntelliJ project configuration. Users can configure whether the `Trace Project` action traces the whole opened project or only selected resources and can edit the selected-resource paths directly in the IDE settings workflow.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Trace Selected Project Resources
`req~trace-selected-project-resources~1`

The plugin can trace selected project resources instead of the whole opened project directory. Users can restrict the OpenFastTrace scan to the resources they intend to include in the trace.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Include IntelliJ Source Directories in Selected-Resource Trace
`req~include-intellij-source-directories-in-selected-resource-trace~1`

When selected-resource tracing is active, the plugin can include source directories known to IntelliJ in the effective OpenFastTrace input set. Users do not need to discover and configure ordinary source roots by hand.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Include IntelliJ Test Directories in Selected-Resource Trace
`req~include-intellij-test-directories-in-selected-resource-trace~1`

When selected-resource tracing is active, the plugin can include test directories known to IntelliJ in the effective OpenFastTrace input set. Users do not need to discover and configure ordinary test roots by hand.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Add Project-Relative Paths to Selected-Resource Trace
`req~add-project-relative-paths-to-selected-resource-trace~1`

When selected-resource tracing is active, the plugin lets users add additional trace inputs through a multi-line text field in the project settings. Each non-empty line specifies one file or directory path relative to the opened project directory that OpenFastTrace should scan. If the user has not changed that setting, the field contains exactly one default entry: `doc/`.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Run Trace Project in Background
`req~run-trace-project-in-background~1`

The plugin runs the OpenFastTrace project trace in a background task with IDE progress reporting. Users can start a trace without blocking the editor UI while the trace is running.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Show Trace Output in IDE Output Window
`req~show-trace-output-in-ide-output-window~1`

The plugin shows the OpenFastTrace text trace output in an IDE output sub-window and keeps that output available after the trace finishes. Users can inspect the plain text result inside the IDE without looking at log files or an external terminal.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Open Specification Item from Trace Output Window
`req~open-specification-item-from-trace-output-window~1`

The plugin makes OpenFastTrace specification item IDs in the trace output window navigable to their declarations in the opened project. Users can jump from a reported item in the trace output directly to the defining specification item without searching manually.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Show Scanned Base Directory in Trace Output Window
`req~show-scanned-base-directory-in-trace-output-window~1`

When a project trace starts, the plugin writes the resolved project base directory that OpenFastTrace scans into the IDE output sub-window. Users can confirm the actual trace input root directly from the trace output.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Show Resolved Trace Inputs in Trace Output Window
`req~show-resolved-trace-inputs-in-trace-output-window~1`

When a project trace starts with selected-resource tracing, the plugin writes the resolved files and directories that it passes to OpenFastTrace into the IDE output sub-window. Users can confirm the actual configured trace scope directly from the trace output.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Report Invalid Project Path before Trace Start
`req~report-invalid-project-path-before-trace-start~1`

The plugin reports when the opened project does not resolve to a valid local path before starting the trace. Users can see why the trace cannot start without inspecting plugin logs.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Show Failing Trace Result in IDE Output Window
`req~show-failing-trace-result-in-ide-output-window~1`

The plugin shows failing OpenFastTrace executions through the same IDE output flow as successful traces, including the text output and the failing result. Users can inspect trace failures without switching to plugin logs or an external terminal.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

### Show Defect Count for Unclean Trace Chain in IDE Output Window
`req~show-defect-count-for-unclean-trace-chain-in-output-window~1`

The plugin preserves the OpenFastTrace plain-text defect summary for an unclean trace chain in the IDE output window, including the reported total-item count and defect count. Users can verify how many trace issues OpenFastTrace found directly from the text result.

Covers:
- `feat~run-oft-trace~1`

Needs: scn

## Scenarios

### Syntax Highlighting

The following scenarios describe the happy path for syntax highlighting.

### Highlight Markdown Specification Item
`scn~highlight-markdown-specification-item~1`

**Given** a user opens a Markdown document that contains a valid OFT specification item
**When** the document is shown in the editor
**Then** the OFT specification item ID and OFT keywords are highlighted differently from surrounding Markdown text.

Covers:
- `req~markdown-syntax-highlighting~1`

Needs: dsn

### Ignore Invalid Markdown Specification Item
`scn~ignore-invalid-markdown-specification-item~1`

**Given** a user opens a Markdown document that contains text resembling an OFT specification item with an invalid revision such as `feat~foobar~I`
**When** the document is shown in the editor
**Then** the invalid text is not highlighted as a valid OFT specification item.

Covers:
- `req~markdown-syntax-highlighting~1`

Needs: dsn

### Tolerate Incomplete Markdown Specification Item
`scn~tolerate-incomplete-markdown-specification-item~1`

**Given** a user edits a Markdown document that contains an incomplete OFT specification item such as `feat~foobar~`
**When** the document is shown in the editor
**Then** the document remains visible and editable while the incomplete text is present.

Covers:
- `req~markdown-syntax-highlighting~1`

Needs: dsn

### Highlight RST Specification Item
`scn~highlight-rst-specification-item~1`

**Given** a user opens an RST document that contains a valid OFT specification item
**When** the document is shown in the editor
**Then** the OFT specification item ID and OFT keywords are highlighted differently from surrounding RST text.

Covers:
- `req~rst-syntax-highlighting~1`

Needs: dsn

### Ignore Invalid RST Specification Item
`scn~ignore-invalid-rst-specification-item~1`

**Given** a user opens an RST document that contains text resembling an OFT specification item with an invalid revision such as `feat~foobar~I`
**When** the document is shown in the editor
**Then** the invalid text is not highlighted as a valid OFT specification item.

Covers:
- `req~rst-syntax-highlighting~1`

Needs: dsn

### Tolerate Incomplete RST Specification Item
`scn~tolerate-incomplete-rst-specification-item~1`

**Given** a user edits an RST document that contains an incomplete OFT specification item such as `feat~foobar~`
**When** the document is shown in the editor
**Then** the document remains visible and editable while the incomplete text is present.

Covers:
- `req~rst-syntax-highlighting~1`

Needs: dsn

### Highlight Coverage Tag in Source Comment
`scn~highlight-coverage-tag-in-source-comment~1`

**Given** a user opens a supported source file that contains an OFT coverage tag inside a comment
**When** the document is shown in the editor
**Then** the OFT coverage tag is highlighted differently from surrounding comment text.

Covers:
- `req~coverage-tag-syntax-highlighting~1`

Needs: dsn

### Ignore Invalid Coverage Tag in Source Comment
`scn~ignore-invalid-coverage-tag-in-source-comment~1`

**Given** a user opens a supported source file that contains text resembling an OFT coverage tag with an invalid revision such as `[impl->feat~foobar~I]`
**When** the document is shown in the editor
**Then** the invalid text is not highlighted as a valid OFT coverage tag.

Covers:
- `req~coverage-tag-syntax-highlighting~1`

Needs: dsn

### Tolerate Incomplete Coverage Tag in Source Comment
`scn~tolerate-incomplete-coverage-tag-in-source-comment~1`

**Given** a user edits a supported source file that contains an incomplete OFT coverage tag such as `[impl->feat~foobar~`
**When** the document is shown in the editor
**Then** the document remains visible and editable while the incomplete text is present.

Covers:
- `req~coverage-tag-syntax-highlighting~1`

Needs: dsn

### Go to Specification Item

The following scenarios describe the happy path for searching declarations and navigating between declarations and coverage occurrences.

### Show Specification Item in Go to Symbol
`scn~show-specification-item-in-go-to-symbol~1`

**Given** a project contains the OpenFastTrace specification item `req~foobar~1`
**When** a user invokes `Go to Symbol` and searches for `req~foobar~1`
**Then** the result list contains the declaration of `req~foobar~1` and does not list coverage occurrences under `Covers:` or in source-code coverage tags.

Covers:
- `req~go-to-symbol-for-specification-items~1`

Needs: dsn

### Show Markdown Declaration Variants in Go to Symbol
`scn~show-markdown-declaration-variants-in-go-to-symbol~1`

**Given** a project contains the Markdown specification item declarations `req~plain_markdown~1` and `` `req~quoted_markdown~1` ``
**When** a user invokes `Go to Symbol` and searches for `req~plain_markdown~1` or `req~quoted_markdown~1`
**Then** the result list contains the declaration of the searched item for both declaration styles.

Covers:
- `req~recognize-markdown-specification-item-declaration-variants~1`

Needs: dsn

### Open Specification Item from Go to Symbol
`scn~open-specification-item-from-go-to-symbol~1`

**Given** a project contains the OpenFastTrace specification item `req~foobar~1`
**When** a user invokes `Go to Symbol`, searches for `req~foobar~1`, and selects the matching specification item from the result list
**Then** the editor opens the selected specification item at the declaration of `req~foobar~1`

Covers:
- `req~open-specification-item-from-go-to-symbol~1`

Needs: dsn

### Open Specification Item from Search Everywhere
`scn~open-specification-item-from-search-everywhere~1`

**Given** a project contains the OpenFastTrace specification item `req~foobar~1`
**When** a user invokes Search Everywhere, switches to the Symbols tab, searches for `req~foobar~1`, and selects the matching specification item from the result list
**Then** the editor opens the selected specification item at the declaration of `req~foobar~1`

Covers:
- `req~go-to-symbol-for-specification-items~1`
- `req~open-specification-item-from-go-to-symbol~1`

Needs: dsn

### Open Specification Item from Coverage Definition
`scn~open-specification-item-from-coverage-definition~1`

**Given** a project contains the specification items `impl~openfasttrace_navigation_design~1` and `req~openfasttrace_navigation_target~1`, and `impl~openfasttrace_navigation_design~1` contains `Covers:` with the entry `req~openfasttrace_navigation_target~1`
**When** a user invokes `Go To Declaration` on `req~openfasttrace_navigation_target~1` under `Covers:`
**Then** the editor opens `req~openfasttrace_navigation_target~1` at its declaration.

Covers:
- `req~open-specification-item-from-coverage-definition~1`

Needs: dsn

### Stay on Specification Item Declaration on Go To Declaration
`scn~stay-on-specification-item-declaration-on-go-to-declaration~1`

**Given** a project contains the specification item `req~openfasttrace_navigation_target~1`
**When** a user places the caret on the declared item ID `req~openfasttrace_navigation_target~1` in its own header and invokes `Go To Declaration`
**Then** the editor keeps the caret on that declaration and does not navigate to a coverage occurrence.

Covers:
- `req~stay-on-specification-item-declaration-on-go-to-declaration~1`

Needs: dsn

### Open Specification Item from Coverage Tag Left Side
`scn~open-specification-item-from-coverage-tag-left-side~1`

**Given** a project contains the specification items `impl~openfasttrace_navigation_target~1` and `req~openfasttrace_navigation_target~1`, and a supported source file contains the OFT coverage tag `[impl->req~openfasttrace_navigation_target~1]`
**When** a user invokes `Go To Declaration` on `impl` on the left side of the coverage tag
**Then** the editor opens `impl~openfasttrace_navigation_target~1` at its definition by copying the missing name and revision from the covered ID on the right side.

Covers:
- `req~open-specification-item-from-coverage-tag-left-side~1`

Needs: dsn

### Open Specification Item from Coverage Tag Right Side
`scn~open-specification-item-from-coverage-tag-right-side~1`

**Given** a project contains the specification item `req~openfasttrace_navigation_target~1`, and a supported source file contains the OFT coverage tag `[impl->req~openfasttrace_navigation_target~1]`
**When** a user invokes `Go To Declaration` on `req~openfasttrace_navigation_target~1` on the right side of the coverage tag
**Then** the editor opens `req~openfasttrace_navigation_target~1` at its definition.

Covers:
- `req~open-specification-item-from-coverage-tag-right-side~1`

Needs: dsn

### Show Covering Occurrences from Specification Item Declaration
`scn~show-covering-occurrences-from-specification-item-declaration~1`

**Given** a project contains the declared specification item `req~openfasttrace_navigation_target~1`, a `Covers:` entry that references `req~openfasttrace_navigation_target~1`, and a supported source file with an OFT coverage tag that references `req~openfasttrace_navigation_target~1`
**When** a user places the caret on the declared item ID `req~openfasttrace_navigation_target~1` in its own header and invokes `Go To Implementations`
**Then** the IDE shows the coverage-providing occurrences of `req~openfasttrace_navigation_target~1`, including the `Covers:` entry and the supported source-code coverage tag, instead of reopening the declaration

Covers:
- `req~show-covering-occurrences-from-specification-item-declaration~1`

Needs: dsn

### OFT Reference Completion

The following scenarios describe completion support while editing OFT references in `Covers:` sections.

### Complete Specification Item ID in Covers Section
`scn~complete-specification-item-id-in-covers-section~1`

**Given** a project contains declared OpenFastTrace specification items and a user edits a `Covers:` entry in a supported specification document
**When** the user types a partial specification item ID and invokes completion
**Then** the IDE suggests existing declared specification item IDs from the project index and orders the suggestion list by full-ID prefix match, then name-prefix match, then name-substring match, and finally artifact-type prefix match

Covers:
- `req~complete-specification-item-ids-in-covers-section~1`

Needs: dsn

### Open OFT User Guide

The following scenarios describe the happy path for opening the OpenFastTrace user guide from the IDE.

### Show OFT User Guide in Help Menu
`scn~show-oft-user-guide-in-help-menu~1`

**Given** the OpenFastTrace plugin is installed in the IDE
**When** a user opens the global Help menu
**Then** the Help menu contains an action for the OpenFastTrace user guide

Covers:
- `req~open-oft-user-guide-in-help-menu~1`

Needs: dsn

### Open OFT User Guide in Integrated Web View
`scn~open-oft-user-guide-in-integrated-web-view~1`

**Given** the OpenFastTrace plugin is installed in the IDE
**When** a user invokes the OpenFastTrace user guide action from the global Help menu
**Then** the IDE opens the OpenFastTrace user guide GitHub page in an integrated web view tab

Covers:
- `req~open-oft-user-guide-in-integrated-web-view~1`

Needs: dsn

### OFT Live Templates

The following scenarios describe how bundled OFT live templates become available and how the scenario template is inserted.

### Show OFT Live Templates in Live Template Settings
`scn~show-oft-live-templates-in-live-template-settings~1`

**Given** the OpenFastTrace plugin is installed in the IDE
**When** a user opens the IDE live-template settings
**Then** the settings contain an `OpenFastTrace` live-template group with the bundled OFT templates

Covers:
- `req~bundle-oft-live-templates~1`

Needs: dsn

### Insert OFT Scenario Live Template
`scn~insert-oft-scenario-live-template~1`

**Given** the OpenFastTrace plugin is installed and a user edits an OFT specification document in a live-template context
**When** the user expands the `scn` live template
**Then** the IDE inserts an OFT scenario skeleton with placeholders for the title, item name, `Given`, `When`, `Then`, and covered requirement

Covers:
- `req~provide-oft-scenario-live-template~1`

Needs: dsn

### Run OFT Trace

The following scenarios describe the trace action flow for the opened IntelliJ project.

### Show Trace Project Action in Tools Menu
`scn~show-trace-project-action-in-tools-menu~1`

**Given** the OpenFastTrace plugin is installed and an IntelliJ project is open
**When** a user opens the global `Tools` menu
**Then** the menu contains an `OpenFastTrace` group with a `Trace Project` action

Covers:
- `req~show-trace-project-action-in-tools-menu~1`

Needs: dsn

### Disable Trace Project Action without Open Project
`scn~disable-trace-project-action-without-open-project~1`

**Given** the OpenFastTrace plugin is installed and no IntelliJ project is open
**When** the IDE shows the global `Tools` menu or resolves the `Trace Project` action presentation
**Then** the `Trace Project` action is disabled

Covers:
- `req~disable-trace-project-action-without-open-project~1`

Needs: dsn

### Run Trace Project in Background
`scn~run-trace-project-in-background~1`

**Given** an IntelliJ project is open and its project directory is available as a local file-system path
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the plugin starts an OpenFastTrace trace for that project directory in a background task with visible IDE progress instead of blocking the editor UI

Covers:
- `req~trace-open-project-from-project-root~1`
- `req~run-trace-project-in-background~1`

Needs: dsn

### Configure Trace Scope in Project Settings
`scn~configure-trace-scope-in-project-settings~1`

**Given** an IntelliJ project is open
**When** a user opens the project settings for the OpenFastTrace plugin
**Then** the user can choose whether `Trace Project` traces the whole project or only selected resources and, for selected-resource tracing, can edit the additional trace paths in a multi-line project-settings field that contains exactly one default entry, `doc/`, until the user changes it

Covers:
- `req~configure-trace-scope-in-project-settings~1`

Needs: dsn

### Trace Selected Project Resources
`scn~trace-selected-project-resources~1`

**Given** an IntelliJ project is open and the OpenFastTrace project settings are configured for selected-resource tracing
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the plugin starts the trace by passing only the resolved selected resources to OpenFastTrace instead of the whole project directory

Covers:
- `req~trace-selected-project-resources~1`
- `req~run-trace-project-in-background~1`

Needs: dsn

### Include IntelliJ Source Directories in Selected-Resource Trace
`scn~include-intellij-source-directories-in-selected-resource-trace~1`

**Given** an IntelliJ project is open, selected-resource tracing is active, and the source-directory option is enabled
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the plugin includes the IntelliJ source directories in the effective OpenFastTrace input set

Covers:
- `req~include-intellij-source-directories-in-selected-resource-trace~1`

Needs: dsn

### Include IntelliJ Test Directories in Selected-Resource Trace
`scn~include-intellij-test-directories-in-selected-resource-trace~1`

**Given** an IntelliJ project is open, selected-resource tracing is active, and the test-directory option is enabled
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the plugin includes the IntelliJ test directories in the effective OpenFastTrace input set

Covers:
- `req~include-intellij-test-directories-in-selected-resource-trace~1`

Needs: dsn

### Add Project-Relative Paths to Selected-Resource Trace
`scn~add-project-relative-paths-to-selected-resource-trace~1`

**Given** an IntelliJ project is open, selected-resource tracing is active, and the OpenFastTrace project settings multi-line field contains additional project-relative file or directory paths
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the plugin resolves those project-relative files and directories against the opened project and includes them in the effective OpenFastTrace input set

Covers:
- `req~add-project-relative-paths-to-selected-resource-trace~1`

Needs: dsn

### Show Per-Line Validation for Additional Trace Paths
`scn~show-per-line-validation-for-additional-trace-paths~1`

**Given** an IntelliJ project is open, selected-resource tracing is active, and the user edits the multi-line field for additional project-relative trace paths
**When** one or more non-empty lines do not resolve to valid files or directories below the opened project directory
**Then** the project settings show a non-blocking validation hint for each invalid line below the field so the user can see which configured paths are not found.

Covers:
- `req~add-project-relative-paths-to-selected-resource-trace~1`

Needs: dsn

### Reject Trace Project without Valid Project Path
`scn~reject-trace-project-without-valid-project-path~1`

**Given** an IntelliJ project is open but the plugin cannot resolve a valid local project directory for tracing
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the plugin reports that the trace cannot be started because the project path is missing or invalid

Covers:
- `req~report-invalid-project-path-before-trace-start~1`

Needs: dsn

### Show Successful Trace Output in IDE Output Window
`scn~show-successful-trace-output-in-ide-output-window~1`

**Given** an IntelliJ project is open, its project directory is a valid OFT trace input, and the OFT trace completes successfully
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the IDE shows the resulting OpenFastTrace text report in an output sub-window that remains available after the trace completes

Covers:
- `req~show-trace-output-in-ide-output-window~1`

Needs: dsn

### Open Specification Item from Trace Output Window
`scn~open-specification-item-from-trace-output-window~1`

**Given** an IntelliJ project is open, the trace output window shows an OFT specification item ID from the opened project, and that item is declared in a supported specification document
**When** a user activates that specification item ID in the trace output window
**Then** the IDE opens the declaration of that specification item in the editor

Covers:
- `req~open-specification-item-from-trace-output-window~1`

Needs: dsn

### Show Scanned Base Directory in Trace Output Window
`scn~show-scanned-base-directory-in-trace-output-window~1`

**Given** an IntelliJ project is open and its project directory resolves to a valid local OFT trace input
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the IDE output for that trace starts with the resolved base directory that the plugin passes to OpenFastTrace

Covers:
- `req~show-scanned-base-directory-in-trace-output-window~1`

Needs: dsn

### Show Resolved Trace Inputs in Trace Output Window
`scn~show-resolved-trace-inputs-in-trace-output-window~1`

**Given** an IntelliJ project is open, selected-resource tracing is active, and the plugin resolved the configured trace files and directories successfully
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the IDE output for that trace lists the resolved files and directories that the plugin passes to OpenFastTrace

Covers:
- `req~show-resolved-trace-inputs-in-trace-output-window~1`

Needs: dsn

### Show Failing Trace Output in IDE Output Window
`scn~show-failing-trace-output-in-ide-output-window~1`

**Given** an IntelliJ project is open, its project directory is a valid OFT trace input, and the OFT trace reports a failure
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the IDE shows the OpenFastTrace text output together with the failing result through the same trace output flow

Covers:
- `req~show-failing-trace-result-in-ide-output-window~1`

Needs: dsn

### Show Defect Count for Unclean Trace Chain in IDE Output Window
`scn~show-defect-count-for-unclean-trace-chain-in-output-window~1`

**Given** an IntelliJ project is open, its project directory is a valid OFT trace input, and the OFT trace finds an unclean feature-to-requirement-to-design chain with missing implementation coverage
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the IDE output contains the OpenFastTrace plain-text summary line with the reported total-item count and defect count for that unclean chain

Covers:
- `req~show-defect-count-for-unclean-trace-chain-in-output-window~1`

Needs: dsn
