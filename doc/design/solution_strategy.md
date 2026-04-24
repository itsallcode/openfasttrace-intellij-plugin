# Solution Strategy

This chapter summarizes the main technical approach for realizing the system requirements.

The solution strategy follows the shared IntelliJ Platform APIs first and keeps product-specific dependencies out of the MVP.

## Cross-IDE Plugin

The plugin is implemented as an IntelliJ Platform plugin that runs in JetBrains IDEs based on the same platform, such as IntelliJ IDEA, PyCharm, and CLion.

This cross-IDE approach is feasible because JetBrains products share the IntelliJ Platform API. A plugin that depends only on shared platform modules can load in all JetBrains products built on that platform. The MVP therefore stays on common platform modules and avoids product-specific APIs unless a later requirement makes them necessary.

Compatibility across the selected IDEs is verified continuously with plugin compatibility checks against the supported IDE builds.

## Reuse Of Existing IDE Facilities

Where the IntelliJ Platform already provides a fitting concept, the plugin uses that concept instead of introducing parallel infrastructure.

Specification items are exposed through the IDE symbol and navigation facilities so users interact with OpenFastTrace data through established workflows such as 'Go to Symbol', Search Everywhere, and Go To on specification references and coverage tags.

Parsing and syntax-aware editor behavior use the IntelliJ parsing, PSI, lexer, and highlighting infrastructure. The plugin builds only the OpenFastTrace-specific parts that are missing and reuses the existing editor, indexing, and navigation services for the rest.

This strategy reduces custom code, lowers maintenance effort, and improves cross-IDE compatibility because the implementation stays aligned with the platform abstractions that JetBrains supports across products.

## OFT Specification Item Index

The index distinguishes rigorously between OpenFastTrace declarations and OpenFastTrace coverage occurrences. That distinction is the foundation for correct IDE navigation.

An OpenFastTrace specification item is declared only in a specification document such as Markdown or reStructuredText when the document defines the item itself. The declaration is the item header line that starts with the full OpenFastTrace item ID, for example `req~user_login~1`. This full ID is the canonical identity of the item because it combines artifact type, name, and revision into the stable project-wide key. The precise location of the declaration is the text range of that ID in the specification item header. In OFT terms, this is the item's visible sub-title. In IDE terms, this is the declaration site and primary navigation target.

The remainder of the specification item body does not declare the item again. In particular, entries under `Covers:` are not declarations. They are coverage occurrences that point to other declared specification items. The same is true for OFT coverage tags in source files. These locations provide coverage in the OpenFastTrace domain model, but from the JetBrains IDE point of view they are usages or references of an already declared item.

This terminology matters because the IntelliJ Platform separates declarations from references. The JetBrains documentation on [Declarations and References](https://plugins.jetbrains.com/docs/intellij/declarations-and-references.html) defines declarations in PSI as `PsiSymbolDeclaration` instances and references as links that resolve from a usage to a declaration. The [PSI References](https://plugins.jetbrains.com/docs/intellij/psi-references.html) documentation likewise defines a reference as an object that links a usage to the corresponding declaration. The plugin therefore models OFT item definitions as declarations and all `Covers:` entries and coverage tags as references to those declarations, not as additional symbols of their own.

The OFT to JetBrains mapping is therefore as follows:

- An OFT specification item identified by its full ID is the semantic project entity that users search and navigate to. Conceptually this is the symbol.
- The occurrence of that full ID in the header of a specification item in `.md`, `.markdown`, or `.rst` is the declaration. In a PSI-first implementation this declaration is represented by a PSI element that carries the item's canonical name and can act as a `NavigationItem`.
- The text range of the full ID inside that header is the declaration anchor. That is the range that `Go To` and search results should open.
- An occurrence of an OFT item ID under `Covers:` in a specification document is a reference from one item to another declared item.
- An occurrence of an OFT item ID inside an OFT coverage tag in source code is also a reference from that source location to a declared item.
- A `Go to Symbol` result is produced from indexed declarations only. Coverage occurrences are not separate symbol results.

This mapping also clarifies what the index stores. The symbol-facing index stores declarations keyed by the canonical full OFT item ID. Additional lookup keys such as the name part may be stored as aliases for search convenience, but they do not replace the canonical identity. The artifact type prefix such as `req`, `dsn`, or `impl` is part of that identity and may additionally be surfaced as presentation metadata, grouping information, or a type label in result lists.

For IntelliJ's search and navigation facilities, the declaration index is the source of truth for `Go to Symbol` and `Search Everywhere`. JetBrains documents [Go to Symbol](https://plugins.jetbrains.com/docs/intellij/go-to-class-and-go-to-symbol.html) as a contributor that feeds the IDE with names and matching `NavigationItem` instances, typically PSI elements. The plugin contributes declaration elements, not synthetic wrappers around arbitrary text matches and not coverage occurrences. This keeps symbol search aligned with the IDE expectation that a search result names something that is actually declared somewhere in the project.

Coverage locations are still first-class data, but they belong in reference resolution and usage-style navigation. They are the places where the IDE should resolve from a usage to a declaration, and they are also the natural basis for any later OFT-specific "show covering items" or "show implementations" feature.

## MVP Scope And Deferred OFT Integration

The MVP focuses on IDE integration for authoring OpenFastTrace specification items and coverage tags. It covers syntax highlighting, navigation to specification items, and opening the OpenFastTrace user guide.

The OpenFastTrace library is not part of the MVP implementation path for tracing logic. Tracing, report generation, and deeper semantic validation through the OpenFastTrace library are introduced later when the product scope extends beyond editor assistance and navigation.

Deferring library integration keeps the MVP small and allows the initial implementation to validate the editor-facing workflows before adding tracing-specific behavior and the additional dependency surface that comes with it.
