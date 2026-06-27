# OpenFastTrace IntelliJ Plugin 0.9.0, released 2026-06-27

Version 0.9.0 introduces specialized run configuration templates and improves the metadata shown by JetBrains plugin surfaces. Before installing or updating the plugin, users can now see a clearer overview of what OpenFastTrace support does in the IDE and read the current release notes from the maintained project changelog.

Users can now quickly set up common OpenFastTrace scanning scenarios using pre-configured templates when creating a new run configuration:
* **User requirements**: Scans `doc/` for core artifact types (`feat, req, scn, bconstr`).
* **Design and above**: Scans `doc/` for all artifact types, including design and build artifacts.
* **Typical project**: Scans `doc/` and all project source directories.
* **Unfiltered**: Scans the entire project without any filters.

The `Tools | OpenFastTrace | Trace Project` menu has been removed, as the new run configuration templates provide a more flexible and discoverable way to start an OpenFastTrace scan.

## Bundled OpenFastTrace

OpenFastTrace 4.5.0

## Features

* #61: Replace global OFT scan with specialized run configuration templates

## Documentation

* #51: Complete Marketplace-facing plugin metadata
