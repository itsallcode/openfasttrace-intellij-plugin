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

## Completion

### Complete Specification Item ID in Covers Section
`dsn~complete-specification-item-id-in-covers-section~1`

**Given** a supported specification document contains a `Covers:` entry and the opened project already indexes declared OpenFastTrace specification items
**When** a user invokes IntelliJ basic completion while editing an OFT item ID in that `Covers:` entry
**Then** the completion component confirms that the caret is inside a `Covers:` reference, loads declared specification item IDs from the declaration index, ranks them by full-ID prefix, name-prefix, name-substring, and artifact-type prefix matches, and shows the ordered suggestions in the standard IDE completion popup.

Covers:
- `scn~complete-specification-item-id-in-covers-section~1`

Needs: impl, itest

### Complete Specification Item ID in Active Live Template Covers Field
`dsn~complete-specification-item-id-in-active-live-template-covers-field~1`

**Given** a bundled OFT live template is active at a `COVERED` placeholder under `Covers:` and the opened project already indexes declared OpenFastTrace specification items
**When** a user invokes IntelliJ basic completion in that placeholder before the live-template session ends
**Then** the completion component reads the active editor document, handles the request like any other `Covers:` reference by reading declared specification item IDs from the declaration index, and shows the matching suggestions in the standard IDE completion popup without ending the live-template session.

Covers:
- `scn~complete-specification-item-id-in-active-live-template-covers-field~1`

Needs: impl, itest

### Complete Specification Item ID in Coverage Tag Target
`dsn~complete-specification-item-id-in-coverage-tag-target~1`

**Given** a source, configuration, or markup file with a default extension supported by the upstream OpenFastTrace Tag Importer contains a comment with an OFT coverage-tag candidate whose left-hand side contains an artifact type and whose right-hand side contains the caret after `->`
**When** a user invokes IntelliJ basic completion on the right-hand side
**Then** the completion component confirms the supported file and comment context, extracts the target-side prefix under the caret, loads declared specification item IDs from the declaration index, ranks them by full-ID prefix, name-prefix, name-substring, and artifact-type prefix matches, and shows the ordered suggestions in the standard IDE completion popup.

Covers:
- `scn~complete-specification-item-id-in-coverage-tag-target~1`

Needs: impl, itest

### Complete Specification Item ID in Spaced Coverage Tag Target
`dsn~complete-specification-item-id-in-spaced-coverage-tag-target~1`

**Given** a source, configuration, or markup file with a default extension supported by the upstream OpenFastTrace Tag Importer contains a comment with an OFT coverage-tag candidate whose arrow has optional spaces around it
**When** a user invokes IntelliJ basic completion on the right-hand side of that arrow
**Then** the completion component accepts the spaced arrow as the same coverage-tag target context and uses the shared indexed specification item suggestions.

Covers:
- `scn~complete-specification-item-id-in-spaced-coverage-tag-target~1`

Needs: impl, itest

### Complete Specification Item ID in Incomplete Coverage Tag Target
`dsn~complete-specification-item-id-in-incomplete-coverage-tag-target~1`

**Given** a source, configuration, or markup file with a default extension supported by the upstream OpenFastTrace Tag Importer contains a comment with an incomplete OFT coverage-tag candidate that has an opening bracket, a left-hand artifact type, and an arrow before the caret but no closing bracket yet
**When** a user invokes IntelliJ basic completion on the right-hand target side
**Then** the completion component accepts the incomplete tag as an editable coverage-tag target context and uses the shared indexed specification item suggestions without requiring the strict valid-tag parser to recognize a complete coverage tag.

Covers:
- `scn~complete-specification-item-id-in-incomplete-coverage-tag-target~1`

Needs: impl, itest

### Suppress Coverage Tag Target Completion Outside Target Context
`dsn~suppress-coverage-tag-target-completion-outside-target-context~1`

**Given** a completion request occurs before the coverage-tag arrow, after an already closed coverage tag, outside a comment, inside a string literal, or in an unsupported file type
**When** the completion component evaluates the coverage-tag target context
**Then** it does not add OpenFastTrace specification item ID suggestions for coverage-tag target completion.

Covers:
- `scn~suppress-coverage-tag-target-completion-outside-target-context~1`

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

## Live Templates

### Show OFT Live Templates in Live Template Settings
`dsn~show-oft-live-templates-in-live-template-settings~1`

**Given** the plugin is loaded into the IDE
**When** IntelliJ loads the plugin's default live-template resources and a user opens the live-template settings
**Then** the IDE shows an `OpenFastTrace` live-template group contributed from the plugin resources.

Covers:
- `scn~show-oft-live-templates-in-live-template-settings~1`

Needs: impl, itest

### Insert OFT Scenario Live Template
`dsn~insert-oft-scenario-live-template~1`

**Given** the bundled OpenFastTrace live-template group is available in a supported editing context
**When** a user expands the `scn` live template
**Then** IntelliJ inserts the scenario template text from the bundled plugin resource and lets the user tab through the placeholders for the title, item name, `Given`, `When`, `Then`, and covered requirement.

Covers:
- `scn~insert-oft-scenario-live-template~1`

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
**Then** the trace-action component starts a background task, the trace-configuration component resolves the effective OpenFastTrace inputs for the project, the trace-execution service invokes the OpenFastTrace library for those inputs, and the IDE keeps the editor UI responsive while showing progress for the running trace.

Covers:
- `scn~run-trace-project-in-background~1`

Needs: impl, itest

### Configure Trace Scope in Project Settings
`dsn~configure-trace-scope-in-project-settings~1`

**Given** an IntelliJ project is open
**When** a user opens the OpenFastTrace project settings
**Then** the trace-configuration component loads the persisted trace-scope mode and lets the user switch between whole-project tracing and selected-resource tracing for that project.

Covers:
- `scn~configure-trace-scope-in-project-settings~1`

Needs: impl, itest

### Trace Selected Project Resources
`dsn~trace-selected-project-resources~1`

**Given** an IntelliJ project is open and OpenFastTrace project settings are configured for selected-resource tracing
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the trace-configuration component resolves only the configured source roots, test roots, and additional project-relative paths and the trace-execution service passes only those inputs to OpenFastTrace.

Covers:
- `scn~trace-selected-project-resources~1`

Needs: impl, itest

### Include IntelliJ Source Directories in Selected-Resource Trace
`dsn~include-intellij-source-directories-in-selected-resource-trace~1`

**Given** selected-resource tracing is active and the source-directory option is enabled
**When** the trace-configuration component resolves the effective OpenFastTrace inputs for the opened IntelliJ project
**Then** it includes the project source roots known to IntelliJ in that input set.

Covers:
- `scn~include-intellij-source-directories-in-selected-resource-trace~1`

Needs: impl, itest

### Include IntelliJ Test Directories in Selected-Resource Trace
`dsn~include-intellij-test-directories-in-selected-resource-trace~1`

**Given** selected-resource tracing is active and the test-directory option is enabled
**When** the trace-configuration component resolves the effective OpenFastTrace inputs for the opened IntelliJ project
**Then** it includes the project test roots known to IntelliJ in that input set.

Covers:
- `scn~include-intellij-test-directories-in-selected-resource-trace~1`

Needs: impl, itest

### Add Project-Relative Paths to Selected-Resource Trace
`dsn~add-project-relative-paths-to-selected-resource-trace~1`

**Given** selected-resource tracing is active and the OpenFastTrace project settings contain additional project-relative paths
**When** the trace-configuration component resolves the effective OpenFastTrace inputs for the opened project
**Then** it resolves those paths against the project directory, keeps the valid files and directories as trace inputs, and rejects invalid configured paths before the trace starts.

Covers:
- `scn~add-project-relative-paths-to-selected-resource-trace~1`

Needs: impl, itest

### Show Per-Line Validation for Additional Trace Paths
`dsn~show-per-line-validation-for-additional-trace-paths~1`

**Given** selected-resource tracing is active and the user edits the multi-line field for additional project-relative trace paths
**When** the trace-configuration component validates the configured lines against the opened project directory
**Then** it keeps the field editable, ignores empty lines, and shows a non-blocking validation hint below the field for each non-empty line that does not resolve to a valid file or directory.

Covers:
- `scn~show-per-line-validation-for-additional-trace-paths~1`

Needs: impl, itest

### Reject Trace Project without Valid Project Path
`dsn~reject-trace-project-without-valid-project-path~2`

**Given** an IntelliJ project is open but its base directory is missing, invalid, or not usable as a local OpenFastTrace input path
**When** a user invokes `Tools | OpenFastTrace | Trace Project`
**Then** the trace-action flow stops before starting the background trace run and reports the invalid project-path or configured-input condition through the IDE-visible trace flow.

Covers:
- `scn~reject-trace-project-without-valid-project-path~1`

Needs: impl, itest

### OpenFastTrace Run Configuration
`dsn~openfasttrace-run-configuration~1`

**When** a user creates or edits an OpenFastTrace run configuration
**Then** the plugin uses the IntelliJ Run Configuration API (type, factory, configuration) to persist the name, scope, additional paths, artifact type filters, and tag filters.

Covers:
- `scn~create-and-run-openfasttrace-run-configuration~1`

Needs: impl, itest

### Filter Trace by Artifact Types and Tags
`dsn~filter-trace-by-artifact-types-and-tags~1`

**When** the trace-execution service invokes the OpenFastTrace library
**Then** it passes the configured artifact type and tag filters from the run configuration to the OpenFastTrace engine to restrict the trace result.

Covers:
- `scn~filter-run-configuration-by-artifact-types~1`
- `scn~filter-run-configuration-by-tags~1`

Needs: impl, itest

### Plain Text as Default Run Configuration Result View
`dsn~plain-text-as-default-run-configuration-result-view~1`

**Given** an OpenFastTrace run configuration has no persisted result-view selection
**When** the run-profile state prepares the trace result presentation
**Then** it selects the existing plain text trace-output presentation.

Covers:
- `scn~plain-text-as-default-run-configuration-result-view~1`

Needs: impl, itest

### Select Test Runner Trace Result View
`dsn~select-test-runner-trace-result-view~1`

**Given** an OpenFastTrace run configuration stores the IntelliJ Test Runner UI result-view selection
**When** the run-profile state prepares the trace result presentation
**Then** it selects the trace test-runner presentation for that run configuration.

Covers:
- `scn~select-test-runner-trace-result-view~1`

Needs: impl, itest

### Show Trace Source Files as Test Runner Suites
`dsn~show-trace-source-files-as-test-runner-suites~1`

**Given** the trace test-runner presentation receives a structured OpenFastTrace trace result
**When** it builds the SM test tree
**Then** it groups traced specification items by source file and creates one SM test suite node for each source file, using a project-local suite label for source paths below the opened project directory.

Covers:
- `scn~show-trace-source-files-as-test-runner-suites~1`

Needs: impl, itest

### Show Trace Specification Items as Test Runner Tests
`dsn~show-trace-specification-items-as-test-runner-tests~1`

**Given** the trace test-runner presentation has created a source-file suite
**When** it maps traced specification items from that source file
**Then** it creates one SM test node for each specification item below that source-file suite.

Covers:
- `scn~show-trace-specification-items-as-test-runner-tests~1`

Needs: impl, itest

### Show Specification Item Title in Test Runner UI
`dsn~show-specification-item-title-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a node whose visible name contains a specification item ID
**When** the OpenFastTrace item for that visible ID has a non-blank title
**Then** it prefixes the visible node name with the title, followed by ` — ` and the full specification item ID.

Covers:
- `scn~show-specification-item-title-in-test-runner-ui~1`

Needs: impl, itest

### Sort Specification Items in Test Runner UI
`dsn~sort-specification-items-in-test-runner-ui~1`

**Given** the trace test-runner presentation maps traced specification items for one source-file suite
**When** it creates the specification-item test nodes
**Then** it orders them by artifact type, the specification item ID name part, and revision number.

Covers:
- `scn~sort-specification-items-in-test-runner-ui~1`

Needs: impl, itest

### Show Trace Links as Test Runner Sub-Tests
`dsn~show-trace-links-as-test-runner-sub-tests~1`

**Given** the trace test-runner presentation has created a specification-item test node
**When** it maps that item's incoming and outgoing trace links
**Then** it creates one SM sub-test node for each trace link below the specification-item test node.

Covers:
- `scn~show-trace-links-as-test-runner-sub-tests~1`

Needs: impl, itest

### Show Specification Item Status in Test Runner UI
`dsn~show-specification-item-status-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a specification-item test node
**When** it derives the node name from the OpenFastTrace trace result
**Then** it appends the specification item's trace status in brackets to the visible node name.

Covers:
- `scn~show-specification-item-status-in-test-runner-ui~1`

Needs: impl, itest

### Show Trace Link Status in Test Runner UI
`dsn~show-trace-link-status-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a trace-link sub-test node
**When** it derives the node name from the OpenFastTrace trace result
**Then** it appends the trace link's status in brackets to the visible node name.

Covers:
- `scn~show-trace-link-status-in-test-runner-ui~1`

Needs: impl, itest

### Show Trace Link Direction in Test Runner UI
`dsn~show-trace-link-direction-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a trace-link sub-test node
**When** the trace link is incoming or outgoing for the owning specification item
**Then** it marks the visible node name with that trace-link direction.

Covers:
- `scn~show-trace-link-direction-in-test-runner-ui~1`

Needs: impl, itest

### Show Unicode Trace Link Direction in Test Runner UI
`dsn~show-unicode-trace-link-direction-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a trace-link sub-test node
**When** it derives the direction marker from the OpenFastTrace link status
**Then** it uses `←` for incoming links, `→` for outgoing links, and `↔` for links without a single incoming or outgoing direction.

Covers:
- `scn~show-unicode-trace-link-direction-in-test-runner-ui~1`

Needs: impl, itest

### Map Specification Item Trace Status to Test Runner Status
`dsn~map-specification-item-trace-status-to-test-runner-status~1`

**Given** the trace test-runner presentation has created a specification-item test node
**When** the OpenFastTrace trace result marks that specification item as clean or defective
**Then** it reports the SM test node as passed for a clean item and failed for a defective item.

Covers:
- `scn~map-specification-item-trace-status-to-test-runner-status~1`

Needs: impl, itest

### Map Trace Link Status to Test Runner Status
`dsn~map-trace-link-status-to-test-runner-status~1`

**Given** the trace test-runner presentation has created a trace-link sub-test node
**When** the OpenFastTrace trace result marks that link as clean or defective
**Then** it reports the SM sub-test node as passed for a clean link and failed for a defective link.

Covers:
- `scn~map-trace-link-status-to-test-runner-status~1`

Needs: impl, itest

### Roll Up Source File Suite Trace Status
`dsn~roll-up-source-file-suite-trace-status~1`

**Given** the trace test-runner presentation has created a source-file suite
**When** at least one specification-item test or trace-link sub-test below that suite is failed
**Then** it marks the source-file suite as failed before finishing the suite node.

Covers:
- `scn~roll-up-source-file-suite-trace-status~1`

Needs: impl, itest

### Roll Up Top-Level Trace Status
`dsn~roll-up-top-level-trace-status~1`

**Given** the trace test-runner presentation has created the top-level trace suite
**When** at least one source-file suite contains a failed descendant
**Then** it marks the top-level trace suite as failed before finishing the trace result.

Covers:
- `scn~roll-up-top-level-trace-status~1`

Needs: impl, itest

### Show Specification Item Defect Details in Test Runner UI
`dsn~show-specification-item-defect-details-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a failed specification-item test node
**When** it maps the OpenFastTrace item status to the SM test node
**Then** it sets a concise failure message and detail text that identify the item and explain the defective status.

Covers:
- `scn~show-specification-item-defect-details-in-test-runner-ui~1`

Needs: impl, itest

### Show Trace Link Defect Details in Test Runner UI
`dsn~show-trace-link-defect-details-in-test-runner-ui~1`

**Given** the trace test-runner presentation creates a failed trace-link sub-test node
**When** it maps the OpenFastTrace link status to the SM test node
**Then** it combines node-specific IDs and direction with a static shared detail template keyed by the OpenFastTrace link status.

Covers:
- `scn~show-trace-link-defect-details-in-test-runner-ui~1`

Needs: impl, itest

### Navigate from Test Runner Specification Items
`dsn~navigate-from-test-runner-specification-items~1`

**Given** the trace test-runner presentation creates a specification-item test node
**When** it attaches source navigation to that node
**Then** it resolves the specification item ID through OpenFastTrace trace navigation and opens the corresponding declaration in the editor.

Covers:
- `scn~navigate-from-test-runner-specification-items~1`

Needs: impl, itest

### Navigate from Test Runner Trace Links
`dsn~navigate-from-test-runner-trace-links~1`

**Given** the trace test-runner presentation creates a trace-link sub-test node
**When** it attaches source navigation to that node
**Then** it resolves the link target through OpenFastTrace trace navigation and opens the corresponding declaration or source-side coverage tag in the editor.

Covers:
- `scn~navigate-from-test-runner-trace-links~1`

Needs: impl, itest

### Show Successful Trace Output in IDE Output Window
`dsn~show-successful-trace-output-in-ide-output-window~1`

**Given** a background OpenFastTrace trace run completes successfully for the opened IntelliJ project
**When** the trace-execution service temporarily switches the thread context class loader to the plugin class loader for OFT importer and reporter discovery, restores the previous context loader afterward, and hands the captured text report to trace-output presentation
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

### Show Resolved Trace Inputs in Trace Output Window
`dsn~show-resolved-trace-inputs-in-trace-output-window~1`

**Given** a user starts a selected-resource trace and the plugin has resolved the files and directories that it will pass to OpenFastTrace
**When** the trace-execution service prepares the plain-text trace output for the IDE output sub-window
**Then** it lists those resolved trace inputs before the OpenFastTrace report body so the output window shows the actual configured scan scope.

Covers:
- `scn~show-resolved-trace-inputs-in-trace-output-window~1`

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
