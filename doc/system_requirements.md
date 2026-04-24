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
