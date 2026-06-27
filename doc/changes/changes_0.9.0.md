# OpenFastTrace IntelliJ Plugin 0.9.0, released 2026-06-27

Version 0.9.0 introduces specialized run configuration templates and simplifies the plugin UI by removing the redundant global trace menu.

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
