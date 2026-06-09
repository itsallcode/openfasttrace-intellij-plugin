# GH-52 Add JetBrains Marketplace plugin icon assets

## Goal

Add JetBrains-compliant OpenFastTrace plugin icon assets and use a scaled
OpenFastTrace icon for IDE run configurations, so users can recognize the
plugin in JetBrains Plugin Manager, JetBrains Marketplace, and the run/debug
configuration UI.

## Scope

In scope:

* Validate or rework the existing `src/main/resources/META-INF/pluginIcon.svg`
  so it satisfies JetBrains plugin logo expectations for SVG format, 40x40
  display, transparent padding, recognizability, and light-background use.
* Add `src/main/resources/META-INF/pluginIcon_dark.svg` only if the default icon
  is not sufficiently visible on dark backgrounds.
* Add a scaled OpenFastTrace icon resource for the `OpenFastTrace` run
  configuration type and replace the current generic execute icon. Remove the
  two white pointy brackets on the left side of the SVG before scaling to make
  the image fit better to the small size.
* Document icon source, derivation, and usage rights when the assets derive from
  existing OpenFastTrace branding.
* Prove that `buildPlugin` packages the icon assets under `META-INF` in the
  plugin distribution.

Out of scope:

* Rebranding OpenFastTrace.
* Creating unrelated UI icons.
* Changing OpenFastTrace tracing behavior, run-configuration execution
  behavior, filters, or result views.
* Publishing the plugin to JetBrains Marketplace.
* Adding a JetBrains Marketplace badge or Marketplace link before the plugin has
  a stable public Marketplace page.

## Design References

* [System Requirements](../system_requirements.md)
* [Deployment View](../design/deployment_view.md)
* [Building Block View](../design/building_block_view.md)
* [Runtime View](../design/runtime_view.md)
* [Quality Requirements](../design/quality_requirements.md)
* [GH-37 Introduce Run Configurations](37-introduce-run-configurations.md)
* [JetBrains Plugin Logo Documentation](https://plugins.jetbrains.com/docs/intellij/plugin-icon-file.html)

## Strategy

Treat this as a user-visible presentation and packaging change, not a tracing
behavior change. Add requirements and scenarios for plugin identity assets and
the OpenFastTrace run-configuration icon before changing resources or Java code.

The current `main` branch already contains
`src/main/resources/META-INF/pluginIcon.svg`, although GH-52 originally
describes that file as missing. The implementation should therefore validate and
revise the existing asset as needed instead of adding a duplicate. Follow the
JetBrains naming convention for the plugin logo files in `META-INF`, and use a
plugin-owned SVG loaded through IntelliJ icon infrastructure for the smaller
run-configuration icon.

## Task List

- [x] Create and checkout a new Git branch `feat/52-add-jetbrains-marketplace-plugin-icon-assets`

### Requirements And Design

- [x] Update `doc/system_requirements.md` with user-visible requirement and
      scenario coverage for JetBrains plugin logo assets in plugin management
      surfaces and for the OpenFastTrace run-configuration icon
- [x] Stop and ask user for a review of the system requirements
- [x] Update `doc/design/deployment_view.md` with the packaged plugin-logo
      resource location, the default/dark icon decision, and the expectation
      that the files end up in the plugin main JAR under `META-INF`
- [x] Update `doc/design/building_block_view.md` so the trace-configuration or
      plugin-presentation building block owns the run-configuration icon
      resource
- [x] Update `doc/design/runtime_view.md` with a focused design item for the
      run-configuration type loading the plugin-owned icon resource
- [x] Keep runtime design coverage to one scenario per `dsn` item, or use OFT
      forwarding where a layer adds no new information
- [x] Stop and ask user for a review of the design

### Implementation

- [x] Validate the existing `src/main/resources/META-INF/pluginIcon.svg` against
      the JetBrains SVG, 40x40 sizing, transparent padding, visual-weight, and
      light/dark visibility expectations
- [x] Rework or replace `pluginIcon.svg` if validation shows it is not
      recognizable at 40x40 and 80x80 display sizes or lacks appropriate
      transparent padding
- [x] Add `src/main/resources/META-INF/pluginIcon_dark.svg` only if the default
      icon does not remain visible on dark backgrounds
- [x] Add a small IDE icon resource, for example
      `src/main/resources/icons/openfasttrace.svg`, derived from the approved
      plugin logo and optimized for run-configuration display
- [x] Add an icon holder class that loads the run-configuration icon through the
      IntelliJ icon API
- [x] Replace `AllIcons.Actions.Execute` in
      `OftRunConfigurationType` with the OpenFastTrace icon
- [x] Add or update OFT build and implementation coverage tags for the new design items
- [x] Avoid adding third-party runtime or build dependencies

### Verification

- [x] Add automated resource tests that load `META-INF/pluginIcon.svg` and,
      when present, `META-INF/pluginIcon_dark.svg` from the plugin classpath and
      assert that the SVG metadata matches the required 40x40 logo size
- [x] Add an automated run-configuration test proving the `OpenFastTrace`
      configuration type supplies the plugin-owned icon instead of the generic
      execute icon
- [x] Visually verify the plugin logo at 40x40 and 80x80 on light and dark
      backgrounds
- [x] Run `./gradlew test`
- [x] Run `./gradlew traceRequirements`
- [x] Run `./gradlew check buildPlugin`
- [x] Inspect the built plugin distribution and its plugin main JAR to confirm
      `META-INF/pluginIcon.svg`, optional `META-INF/pluginIcon_dark.svg`, and
      the run-configuration icon resource are packaged
- [x] Run `./gradlew verifyPlugin`
- [x] Keep path coverage at or above the documented 80 percent threshold
- [x] Keep the OpenFastTrace trace clean for `feat`, `req`, `scn`, `dsn`,
      `impl`, `utest`, `itest`, and build artifact types in scope
- [x] Keep the dependency policy unchanged
- [ ] Keep the SonarQube Cloud quality gate green after CI analysis

### Update User Documentation

- [x] Document the icon source, derivation, and usage rights in `README.md` or a
      dedicated repository documentation section
- [x] Refresh user-guide or demo screenshots only if they visibly show the old
      generic run-configuration icon and would otherwise become misleading

## Version and Changelog Update

- [x] Check if the current version mentioned in the build scripts and code
      parameters is the same as the latest GitHub release. As of 2026-06-09,
      `gradle.properties` contains `0.8.0` and the latest GitHub release is
      `0.7.0`.
- [x] Keep the project version at `0.8.0` if that version is still unreleased
      when GH-52 is implemented; otherwise raise the version to `0.9.0` because
      this is a feature-level presentation enhancement
- [x] Write the changelog entry for the version that receives GH-52
- [x] Update the release date to the actual release date
- [x] Ensure that the issue list contains `#52: Add JetBrains Marketplace plugin
      icon assets`
