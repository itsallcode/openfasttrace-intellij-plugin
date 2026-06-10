# OpenFastTrace IntelliJ Plugin 0.8.1, released 2026-06-10

Version 0.8.1 improves the metadata shown by JetBrains plugin surfaces. Before installing or updating the plugin, users can now see a clearer overview of what OpenFastTrace support does in the IDE and read the current release notes from the maintained project changelog.

The plugin description now focuses on the everyday OpenFastTrace workflows in JetBrains IDEs: authoring requirements in project files, tracing requirement chains down to implementation and tests, and debugging broken chains from the IDE.

The packaged plugin descriptor also carries the project website, `itsallcode.org` vendor metadata, compatibility baseline, and change notes rendered from the active release notes.

The release workflow now verifies the packaged plugin with IntelliJ Plugin Verifier before creating the GitHub release artifact.

## Bundled OpenFastTrace

OpenFastTrace 4.5.0

## Documentation

* #51: Complete Marketplace-facing plugin metadata

## Build Maintenance

* #49: Run plugin verification in the release workflow
