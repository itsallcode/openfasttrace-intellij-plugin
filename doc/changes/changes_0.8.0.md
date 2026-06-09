# OpenFastTrace IntelliJ Plugin 0.8.0, released 2026-06-09

Version 0.7.0 made OpenFastTrace runs configurable, so you could trace only the files, artifact types, or tags that matter for a specific task. Version 0.8.0 makes those trace results much easier to understand and act on.

OpenFastTrace traces now open in IntelliJ's built-in Test Runner UI by default. Instead of scanning a plain text report, you get a structured result tree that groups findings by source file, specification item, and trace link. Clean items show as passed tests, trace defects show as failed tests, and failed source files and the top-level trace result are marked automatically.

This gives you a faster trace review workflow:

* see immediately whether the trace passed or where defects remain
* expand a source file to inspect the affected specification items
* inspect incoming and outgoing trace links as sub-tests
* read item IDs, link status, and defect explanations in the test details
* navigate from result nodes back to source files, specification declarations, and source-side coverage tags

The global `Tools | OpenFastTrace | Trace Project` action and new or previously unconfigured run configurations use the Test Runner UI. Run configurations can still opt into the existing plain text output when you want the raw OpenFastTrace report.

## Features

#40: Integrate OFT Trace into Test Runner UI

## Documentation

#43: Add a use-case-centric plugin user guide

## Build Maintenance

#45: Add Gradle dependency locks and dependency version checks
