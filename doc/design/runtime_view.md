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
