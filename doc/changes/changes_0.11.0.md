# OpenFastTrace IntelliJ Plugin 0.11.0, released 2026-10-01

Version 0.11.0 introduces IntelliJ rename refactoring support for OpenFastTrace specification item IDs, updates the bundled OpenFastTrace library to 4.10.0, and refreshes the Gradle build dependencies and plugins to current stable versions.

Users can trigger standard IntelliJ Rename refactoring (Shift+F6) directly on specification item ID declarations in supported specification documents. The rename dialog validates canonical OpenFastTrace identifier syntax (including tildes, hyphens, and dots), and matching references in `Covers:` sections and source coverage tags across the project update automatically.

## Bundled OpenFastTrace

OpenFastTrace 4.10.0

## Features

* #60: Rename OpenFastTrace specification items with IntelliJ refactoring

## Build Maintenance

* Updated the Gradle wrapper, build plugins, JaCoCo, and test dependencies to current stable versions.
