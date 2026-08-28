# OpenFastTrace IntelliJ Plugin 0.9.0, released 2026-08-07

Version 0.9.0 adds run-configuration templates, improves Test Runner defect presentation, and updates the bundled OpenFastTrace library to 4.9.0.

The plugin description and release notes now provide clearer Marketplace metadata. The release workflow verifies the packaged plugin before creating the GitHub release artifact.

The Test Runner distinguishes direct and transitive defects, optionally hides transitive defects, and counts specification items only. Source-file suites and trace-link details remain visible without inflating totals or progress.

## Bundled OpenFastTrace

OpenFastTrace 4.9.0

## Features

* #61: Replace global OFT scan with specialized run configuration templates
* #66: Mark transitive defects with a visible `↳` prefix in the Test Runner UI
* #72: Filter transitive defects in the run configuration

## Documentation

* #51: Complete Marketplace-facing plugin metadata

## Build Maintenance

* #49: Run plugin verification in the release workflow

## Bugfix

* #39: Re-enable the tag-filter test
* #74: Count specification items only in the Test Runner
