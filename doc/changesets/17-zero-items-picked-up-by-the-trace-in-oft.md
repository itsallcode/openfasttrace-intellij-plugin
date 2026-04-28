# GH-17 Regression: zero items picked up by the trace in OFT

## Goal

Restore project tracing so the plugin finds and traces OpenFastTrace specification items from the opened IntelliJ project instead of reporting `ok - 0 total` for a project that contains valid OFT artifacts.

The fix should preserve the existing `Trace Project` user flow while removing the regression in importer discovery that likely came from later refactoring on the `0.2.0` line.

## Scope

In scope:

* reproduce the regression described in GH-17 with an automated test
* fix OFT importer and reporter discovery so tracing works from inside the IntelliJ plugin class-loading environment
* keep the existing project-root trace input behavior and output-window flow intact
* update the technical design where the current class-loader requirement is not documented clearly enough

Out of scope:

* new trace settings, path selectors, or custom input configuration
* switching from in-process OFT execution to an external CLI process
* redesigning trace output presentation beyond what is needed to verify the regression fix
* broader tracing features such as structured problem views, automatic tracing, or inline inspections

## Design References

* [System Requirements](../system_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)

## Strategy

The existing user-visible requirements for project tracing already describe the intended behavior, so this issue should be treated primarily as a regression against the current specification rather than as a new feature request.

The likely fault line is the OpenFastTrace library's use of `ServiceLoader` for importer and reporter discovery inside the IntelliJ plugin environment. The implementation should make that class-loader contract explicit in the trace service and protect it with a regression test that runs tracing under a foreign or otherwise unsuitable thread context class loader.

## Task List

### Requirements And Design

- [x] Confirm that GH-17 is fully covered by the existing `feat~run-oft-trace~1`, `req~trace-open-project-from-project-root~1`, `req~show-trace-output-in-ide-output-window~1`, and related trace scenarios, so no user-facing requirement changes are needed
- [x] Update the design documentation to record the OFT importer/reporter discovery constraint in the plugin runtime, including that trace execution must use the plugin class loader for `ServiceLoader`-based OFT extension lookup
- [x] Link the design clarification to the existing trace-execution design items instead of introducing redundant new runtime requirements

### Regression Reproduction

- [x] Add an automated regression test that fails with the current implementation when the trace runs under a thread context class loader that does not expose OFT importer plugins
- [x] Make the regression test assert the real failure mode from GH-17: a project with valid OFT specs must not produce a zero-item success result

### Trace Execution Fix

- [x] Fix `OftTraceService` so OFT import and report rendering run with the correct plugin class loader instead of inheriting or reusing an unsuitable thread context class loader
- [x] Restore the previous thread context class loader after trace execution so the plugin does not leak class-loader changes outside the OFT call boundary
- [x] Keep cancellation, error handling, and output rendering behavior unchanged except where required by the regression fix

### Verification

- [x] Keep existing trace-service and trace-background tests green after the class-loader fix
- [x] Add or update tests so successful tracing proves that real specification items and coverage tags are imported and linked under plugin-like class-loader conditions
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [x] Keep path coverage at or above the documented threshold
- [x] Keep dependency policy unchanged and avoid adding new third-party libraries
- [x] Keep plugin verification and packaging checks green through the standard Gradle verification flow
- [x] Keep SonarQube Cloud quality-gate checks green
- [x] Keep OSS Index audit results clean

## Version and Changelog Update

- [x] Raise the version to 0.2.1 (this is a bugfix release)
- [x] Write the changelog entry for 0.2.1

## Lessons Learned

### Regression Origin

On the visible `main` branch history, the regression was introduced in commit `a3e909c` (`Feature/12 run oft trace from the plugin (#15)`), which added `OftTraceService`.

That commit already contained the faulty class-loader handoff:

- it read the current thread context class loader into `previousClassLoader`
- it then called `currentThread.setContextClassLoader(previousClassLoader)`

So the code never actually switched to the plugin class loader before calling OpenFastTrace. In the IntelliJ runtime this breaks OFT importer and reporter discovery because OFT uses Java `ServiceLoader` against the thread context class loader.

### Why Existing Tests Missed It

The tests added in `a3e909c` exercised trace behavior only under the default JUnit/Gradle test runtime. In that environment the active thread context class loader already exposed the OFT service registrations, so tracing still imported items and the bug stayed hidden.

The tests at that time also focused on:

- success vs. failure result status
- output-window text rendering
- defect-count preservation
- error handling

They did not simulate the IntelliJ-specific class-loader boundary by replacing the thread context class loader with a foreign loader that lacks OFT service registrations.

### Preventive Takeaway

For integrations that depend on `ServiceLoader`, reflection, or other class-loader-sensitive discovery mechanisms, unit tests must include at least one hostile-runtime case that runs under a deliberately unsuitable thread context class loader and verifies both:

- the functional outcome still works
- the previous thread context class loader is restored afterward
