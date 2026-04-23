# Runtime View

This chapter describes relevant runtime interactions for the main use cases and extension points.

Terms such as `plugin`, `OpenFastTrace`, and `OFT` use the definitions from [System Requirements](../system_requirements.md).

## Syntax Highlighting

### Markdown Highlighting Runtime
`dsn~markdown-highlighting-runtime~1`

**Given** a Markdown document is opened or edited in the IDE
**When** the Markdown specification support analyzes candidate OpenFastTrace fragments in the editor content
**Then** it asks the shared OFT syntax core to classify those fragments and applies OFT text attributes only to valid specification items and OFT keywords while invalid fragments remain ordinary Markdown text and incomplete fragments do not stop editing

Covers:
- `scn~highlight-markdown-specification-item~1`
- `scn~ignore-invalid-markdown-specification-item~1`
- `scn~tolerate-incomplete-markdown-specification-item~1`

Needs: impl, itest

### RST Highlighting Runtime
`dsn~rst-highlighting-runtime~1`

**Given** an RST document is opened or edited in the IDE
**When** the RST specification support analyzes candidate OpenFastTrace fragments in the editor content
**Then** it asks the shared OFT syntax core to classify those fragments and applies OFT text attributes only to valid specification items and OFT keywords while invalid fragments remain ordinary RST text and incomplete fragments do not stop editing

Covers:
- `scn~highlight-rst-specification-item~1`
- `scn~ignore-invalid-rst-specification-item~1`
- `scn~tolerate-incomplete-rst-specification-item~1`

Needs: impl, itest

### Coverage Tag Highlighting Runtime
`dsn~coverage-tag-highlighting-runtime~1`

**Given** a supported source, configuration, or markup file is opened or edited in the IDE
**When** the coverage-tag support analyzes candidate OFT coverage tags in the editor content
**Then** it asks the shared OFT syntax core to classify those fragments and applies OFT text attributes only to valid OFT coverage tags while invalid fragments remain ordinary comment text and incomplete fragments do not stop editing

Covers:
- `scn~highlight-coverage-tag-in-source-comment~1`
- `scn~ignore-invalid-coverage-tag-in-source-comment~1`
- `scn~tolerate-incomplete-coverage-tag-in-source-comment~1`

Needs: impl, itest

## Navigation

### Specification Item Navigation Runtime
`dsn~specification-item-navigation-runtime~1`

**Given** supported specification documents exist in the opened project
**When** project indexing runs and a user invokes Go to Symbol or Search Everywhere for a specification item name
**Then** the specification item index provides project-local lookup data for matching items and the navigation component opens the selected specification item at its stored file location and offset

Covers:
- `scn~show-specification-item-in-go-to-symbol~1`
- `scn~open-specification-item-from-go-to-symbol~1`
- `scn~open-specification-item-from-search-everywhere~1`

Needs: impl, itest

## Help Action

### User Guide Action Runtime
`dsn~user-guide-action-runtime~1`

**Given** the plugin is loaded into the IDE
**When** the IDE builds the Help menu and the user invokes the OpenFastTrace user guide action
**Then** the plugin provides that action in the global Help menu and opens the configured OpenFastTrace user guide URL in the integrated IDE web view

Covers:
- `scn~show-oft-user-guide-in-help-menu~1`
- `scn~open-oft-user-guide-in-integrated-web-view~1`

Needs: impl, itest
