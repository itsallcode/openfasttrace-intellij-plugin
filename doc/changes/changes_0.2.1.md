# OpenFastTrace IntelliJ Plugin 0.2.1, released 2026-04-28

## Summary

This bugfix release restores correct OpenFastTrace importer discovery when a trace is run from inside the IntelliJ plugin runtime.

## Fixes

* #17: Fix a regression where `Tools | OpenFastTrace | Trace Project` could report `ok - 0 total` for projects that contain valid OpenFastTrace specification items because the trace service did not switch to the plugin class loader for OFT `ServiceLoader` discovery
