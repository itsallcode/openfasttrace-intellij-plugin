# Runtime View

This chapter describes relevant runtime interactions for the main use cases and extension points.

Terms such as `plugin`, `OpenFastTrace`, and `OFT` use the definitions from [System Requirements](../system_requirements.md).

## Syntax Highlighting

### Highlight Markdown Specification Item
`dsn~highlight-markdown-specification-item~1`

**Given** a Markdown document contains a valid OFT specification item
**When** the Markdown specification support analyzes candidate OFT fragments in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as a valid specification item and applies the OFT text attributes to the specification item ID and OFT keywords.

Covers:
- `scn~highlight-markdown-specification-item~1`

Needs: impl, itest

### Ignore Invalid Markdown Specification Item
`dsn~ignore-invalid-markdown-specification-item~1`

**Given** a Markdown document contains text resembling an OFT specification item with an invalid revision
**When** the Markdown specification support analyzes candidate OFT fragments in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as invalid and leaves that text as ordinary Markdown text.

Covers:
- `scn~ignore-invalid-markdown-specification-item~1`

Needs: impl, itest

### Tolerate Incomplete Markdown Specification Item
`dsn~tolerate-incomplete-markdown-specification-item~1`

**Given** a Markdown document contains an incomplete OFT specification item while the user is editing
**When** the Markdown specification support analyzes candidate OFT fragments in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as incomplete and leaves the document visible and editable without treating the fragment as a valid declaration.

Covers:
- `scn~tolerate-incomplete-markdown-specification-item~1`

Needs: impl, itest

### Highlight RST Specification Item
`dsn~highlight-rst-specification-item~1`

**Given** an RST document contains a valid OFT specification item
**When** the RST specification support analyzes candidate OFT fragments in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as a valid specification item and applies the OFT text attributes to the specification item ID and OFT keywords.

Covers:
- `scn~highlight-rst-specification-item~1`

Needs: impl, itest

### Ignore Invalid RST Specification Item
`dsn~ignore-invalid-rst-specification-item~1`

**Given** an RST document contains text resembling an OFT specification item with an invalid revision
**When** the RST specification support analyzes candidate OFT fragments in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as invalid and leaves that text as ordinary RST text.

Covers:
- `scn~ignore-invalid-rst-specification-item~1`

Needs: impl, itest

### Tolerate Incomplete RST Specification Item
`dsn~tolerate-incomplete-rst-specification-item~1`

**Given** an RST document contains an incomplete OFT specification item while the user is editing
**When** the RST specification support analyzes candidate OFT fragments in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as incomplete and leaves the document visible and editable without treating the fragment as a valid declaration.

Covers:
- `scn~tolerate-incomplete-rst-specification-item~1`

Needs: impl, itest

### Highlight Coverage Tag
`dsn~highlight-coverage-tag~1`

**Given** a supported source, configuration, or markup file contains a valid OFT coverage tag
**When** the coverage-tag support analyzes candidate OFT coverage tags in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as a valid coverage tag and applies the OFT text attributes to that coverage tag.

Covers:
- `scn~highlight-coverage-tag-in-source-comment~1`

Needs: impl, itest

### Ignore Invalid Coverage Tag
`dsn~ignore-invalid-coverage-tag~1`

**Given** a supported source, configuration, or markup file contains text resembling an OFT coverage tag with an invalid revision
**When** the coverage-tag support analyzes candidate OFT coverage tags in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as invalid and leaves that text as ordinary comment text.

Covers:
- `scn~ignore-invalid-coverage-tag-in-source-comment~1`

Needs: impl, itest

### Tolerate Incomplete Coverage Tag
`dsn~tolerate-incomplete-coverage-tag~1`

**Given** a supported source, configuration, or markup file contains an incomplete OFT coverage tag while the user is editing
**When** the coverage-tag support analyzes candidate OFT coverage tags in the editor content
**Then** it asks the shared OFT syntax core to classify the fragment as incomplete and leaves the document visible and editable without treating the fragment as a valid coverage tag.

Covers:
- `scn~tolerate-incomplete-coverage-tag-in-source-comment~1`

Needs: impl, itest

## Navigation

### Show Specification Item in Go to Symbol
`dsn~show-specification-item-in-go-to-symbol~1`

**Given** supported specification documents with OpenFastTrace item declarations exist in the opened project
**When** project indexing runs and a user invokes `Go to Symbol` with a full OFT item ID
**Then** the specification item index supplies declaration elements keyed by that full OFT item ID and the IDE shows matching declarations only, excluding coverage occurrences under `Covers:` and in coverage tags.

Covers:
- `scn~show-specification-item-in-go-to-symbol~1`

Needs: impl, itest

### Show Markdown Declaration Variants in Go to Symbol
`dsn~show-markdown-declaration-variants-in-go-to-symbol~1`

**Given** a Markdown specification document declares one OFT item as a plain declaration line and another OFT item as a declaration line whose full OFT item ID is enclosed in single backticks
**When** project indexing extracts declaration elements for `Go to Symbol`
**Then** the specification item index recognizes both Markdown forms as declaration anchors keyed by the same canonical full OFT item IDs and the IDE can return both declarations as symbol results.

Covers:
- `scn~show-markdown-declaration-variants-in-go-to-symbol~1`

Needs: impl, itest

### Open Specification Item from Go to Symbol
`dsn~open-specification-item-from-go-to-symbol~1`

**Given** a `Go to Symbol` result list contains a matching OpenFastTrace specification item declaration
**When** a user selects that result
**Then** the navigation component opens the specification document at the declaration anchor of the selected full OFT item ID.

Covers:
- `scn~open-specification-item-from-go-to-symbol~1`

Needs: impl, itest

### Open Specification Item from Search Everywhere
`dsn~open-specification-item-from-search-everywhere~1`

**Given** supported specification documents with OpenFastTrace item declarations exist in the opened project
**When** a user invokes `Search Everywhere`, switches to the Symbols tab, searches for a full OFT item ID, and selects a matching result
**Then** the specification item index supplies the matching declaration element and the navigation component opens the specification document at that declaration anchor.

Covers:
- `scn~open-specification-item-from-search-everywhere~1`

Needs: impl, itest

### Open Specification Item from Coverage Definition
`dsn~open-specification-item-from-coverage-definition~1`

**Given** a specification document contains an OFT item ID under `Covers:` and the referenced specification item is declared in the project
**When** a user invokes `Go To Declaration` on that OFT item ID under `Covers:`
**Then** the navigation component resolves the coverage occurrence as a reference to the declared specification item and opens the specification document at the declaration anchor of the referenced full OFT item ID.

Covers:
- `scn~open-specification-item-from-coverage-definition~1`

Needs: impl, itest

### Stay on Specification Item Declaration
`dsn~stay-on-specification-item-declaration~1`

**Given** a user has placed the caret on the declared OFT item ID in the header of its own specification item
**When** the user invokes `Go To Declaration`
**Then** the IDE treats the current location as the declaration anchor and keeps the user on that declaration instead of navigating to a coverage occurrence.

Covers:
- `scn~stay-on-specification-item-declaration-on-go-to-declaration~1`

Needs: impl, itest

### Show Covering Occurrences from Declaration
`dsn~show-covering-occurrences-from-declaration~1`

**Given** a declared specification item is referenced from `Covers:` entries or supported source-code coverage tags in the project
**When** a user invokes `Go To Implementations` on the declared OFT item ID in its specification item header
**Then** the navigation component returns the coverage-providing occurrences that reference that declaration instead of reopening the declaration itself.

Covers:
- `scn~show-covering-occurrences-from-specification-item-declaration~1`

Needs: impl, itest

### Open Specification Item from Coverage Tag Left Side
`dsn~open-specification-item-from-coverage-tag-left-side~1`

**Given** a supported source file contains an OFT coverage tag whose left side is shortened and the corresponding covering specification item is declared in the project
**When** a user invokes `Go To Declaration` on the left side of the coverage tag
**Then** the navigation component resolves the effective covering item ID by copying the missing name and revision parts from the covered ID on the right side and opens the specification document at the declaration anchor of that covering item.

Covers:
- `scn~open-specification-item-from-coverage-tag-left-side~1`

Needs: impl, itest

### Open Specification Item from Coverage Tag Right Side
`dsn~open-specification-item-from-coverage-tag-right-side~1`

**Given** a supported source file contains an OFT coverage tag whose right side references a declared specification item
**When** a user invokes `Go To Declaration` on the right side of the coverage tag
**Then** the navigation component resolves that reference to the covered specification item and opens the specification document at the declaration anchor of the referenced full OFT item ID.

Covers:
- `scn~open-specification-item-from-coverage-tag-right-side~1`

Needs: impl, itest

## Help Action

### Show OFT User Guide in Help Menu
`dsn~show-oft-user-guide-in-help-menu~1`

**Given** the plugin is loaded into the IDE
**When** the IDE builds the global Help menu
**Then** the plugin contributes the OpenFastTrace user guide action in that menu.

Covers:
- `scn~show-oft-user-guide-in-help-menu~1`

Needs: impl, itest

### Open OFT User Guide in Integrated Web View
`dsn~open-oft-user-guide-in-integrated-web-view~1`

**Given** the OpenFastTrace user guide action is present in the Help menu
**When** a user invokes that action
**Then** the plugin opens the configured OpenFastTrace user guide URL in the integrated IDE web view.

Covers:
- `scn~open-oft-user-guide-in-integrated-web-view~1`

Needs: impl, itest

## Trace Project

### Show Trace Project Action in Tools Menu
`dsn~show-trace-project-action-in-tools-menu~1`

**Given** the plugin is loaded into the IDE and an IntelliJ project is open
**When** the IDE builds the global `Tools` menu
**Then** the plugin contributes an `OpenFastTrace` action group that contains the `Trace Project` action.

Covers:
- `scn~show-trace-project-action-in-tools-menu~1`

Needs: impl, itest

### Disable Trace Project Action without Open Project
`dsn~disable-trace-project-action-without-open-project~1`

**Given** the plugin is loaded into the IDE and no IntelliJ project is open
**When** the IDE updates the presentation of the `Trace Project` action
**Then** the trace-action component disables that action instead of offering a runnable trace command.

Covers:
- `scn~disable-trace-project-action-without-open-project~1`

Needs: impl, itest

### Run Trace Project in Background
`dsn~run-trace-project-in-background~1`

**Given** an IntelliJ project is open and its base directory resolves to a valid local file-system path
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the trace-action component starts a background task, the trace-execution service invokes the OpenFastTrace library for that project path, and the IDE keeps the editor UI responsive while showing progress for the running trace.

Covers:
- `scn~run-trace-project-in-background~1`

Needs: impl, itest

### Reject Trace Project without Valid Project Path
`dsn~reject-trace-project-without-valid-project-path~1`

**Given** an IntelliJ project is open but its base directory is missing, invalid, or not usable as a local OpenFastTrace input path
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the trace-action flow stops before starting the background trace run and reports the invalid project-path condition through the IDE-visible, trace flow.

Covers:
- `scn~reject-trace-project-without-valid-project-path~1`

Needs: impl, itest

### Show Successful Trace Output in IDE Output Window
`dsn~show-successful-trace-output-in-ide-output-window~1`

**Given** a background OpenFastTrace trace run completes successfully for the opened IntelliJ project
**When** the trace-execution service finishes and hands the captured text report to trace-output presentation
**Then** the plugin opens or updates an IDE output sub-window for that trace run and shows the OpenFastTrace text output under a clear trace-specific content title.

Covers:
- `scn~show-successful-trace-output-in-ide-output-window~1`

Needs: impl, itest

### Show Scanned Base Directory in Trace Output Window
`dsn~show-scanned-base-directory-in-trace-output-window~1`

**Given** a user starts a project trace and the plugin has resolved the local project directory that it will pass to OpenFastTrace
**When** the trace-execution service prepares the plain-text trace output for the IDE output sub-window
**Then** it prefixes that output with the resolved base directory so the output window shows the actual scan root before the OpenFastTrace report body

Covers:
- `scn~show-scanned-base-directory-in-trace-output-window~1`

Needs: impl, itest

### Show Failing Trace Output in IDE Output Window
`dsn~show-failing-trace-output-in-ide-output-window~1`

**Given** a background OpenFastTrace trace run finishes with a failing status or throws an exception after output collection has started
**When** the trace-execution service hands the captured output and failure state to trace-output presentation
**Then** the plugin shows the text output together with the failure result through the same IDE output flow without requiring plugin log inspection.

Covers:
- `scn~show-failing-trace-output-in-ide-output-window~1`

Needs: impl, itest

### Open Specification Item from Trace Output Window
`dsn~open-specification-item-from-trace-output-window~1`

**Given** the trace-output presentation shows OpenFastTrace plain-text output that contains specification item IDs declared in the opened project
**When** a user activates one of those item IDs in the trace output window
**Then** the trace-output presentation resolves that ID through the project declaration index and navigates to the declaration anchor in the corresponding specification document

Covers:
- `scn~open-specification-item-from-trace-output-window~1`

Needs: impl, itest

### Preserve Defect Count for Unclean Trace Chain in IDE Output Window
`dsn~preserve-defect-count-for-unclean-trace-chain-in-output-window~1`

**Given** a background OpenFastTrace trace run finishes with defects for an unclean feature-to-requirement-to-design chain and the OFT plain-text report includes a summary line with total-item and defect counts
**When** the trace-execution service renders that report and hands it to trace-output presentation
**Then** the plugin preserves that summary line unchanged in the IDE-visible trace output so users can read the reported counts directly from the trace text

Covers:
- `scn~show-defect-count-for-unclean-trace-chain-in-output-window~1`

Needs: impl, itest
