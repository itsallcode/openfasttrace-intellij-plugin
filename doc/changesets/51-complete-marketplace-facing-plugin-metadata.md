# GH-51 Complete Marketplace-facing plugin metadata

## Goal

Complete the plugin metadata that JetBrains Marketplace and the IDE Plugin
Manager read from the packaged plugin descriptor, so users see a clear
OpenFastTrace plugin description, current change notes, and useful project
links before installing or updating the plugin.

## Scope

In scope:

* Add Marketplace-facing description text that explains the plugin value and
  main workflows clearly, with developers as the primary audience.
* Populate plugin change notes from the active release notes or another
  maintained project source.
* Configure website, vendor, and source/project links where the IntelliJ
  Platform Gradle Plugin, `plugin.xml`, and JetBrains Marketplace metadata
  support them.
* Review the plugin name, vendor, version, and compatibility metadata against
  JetBrains Marketplace listing guidance.
* Prove that `buildPlugin` produces a plugin ZIP whose patched descriptor
  contains the expected Marketplace-facing metadata.

Out of scope:

* Changing plugin runtime behavior.
* Creating Marketplace screenshots, videos, or paid listing assets.
* Uploading or publishing the plugin to JetBrains Marketplace.
* Rebranding the plugin or changing its stable plugin ID.
* Adding new runtime dependencies to render metadata.
* Marketing-oriented sales copy.

## Design References

* [GitHub Issue #51](https://github.com/itsallcode/openfasttrace-intellij-plugin/issues/51)
* [System Requirements](../system_requirements.md)
* [Deployment View](../design/deployment_view.md)
* [Building Block View](../design/building_block_view.md)
* [Design Decisions](../design/architecture_decisions.md)
* [Quality Requirements](../design/quality_requirements.md)
* [JetBrains Marketplace Listing Best Practices](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html)
* [IntelliJ Platform Gradle Plugin Extension](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html)
* [Plugin Configuration File](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)

## Strategy

Treat this as a user-visible packaging and listing change, not a runtime
feature change. JetBrains Marketplace extracts the plugin name, website link,
description, and change notes from `plugin.xml`, and the IntelliJ Platform
Gradle Plugin can patch descriptor values such as `description`,
`changeNotes`, `version`, `vendor`, and `ideaVersion` during the build.

Keep the descriptor metadata source-controlled and maintainable. Prefer the
existing `intellijPlatform.pluginConfiguration` block as the single build-time
patching point for metadata that the Gradle plugin supports. Keep any
descriptor-only fields, such as the root `idea-plugin` website URL, in
`src/main/resources/META-INF/plugin.xml` if no Gradle DSL property supports
them. If JetBrains only supports a source-code link through Marketplace admin
metadata rather than the packaged descriptor, document that as a release
checklist item instead of adding unsupported XML.

Render the active project release notes into the descriptor change notes with
Pandoc so this can be automated from the existing maintained Markdown release
documentation without custom conversion code.

Keep the listing tone sober and project-focused. This is an open-source plugin,
so the description should explain what users get without sales language.

## Metadata Decisions

Use the following user-confirmed metadata targets for implementation:

* Plugin name: `OpenFastTrace`
* First description sentence: "Author requirements directly in your project and
  trace them all the way down to your implementation and tests without leaving
  your IDE."
* Audience order: developers first, followed by requirement engineers, quality
  engineers, technical writers, and other OpenFastTrace users.
* Main workflows to emphasize: authoring requirements, running traces, and
  debugging broken requirement chains.
* Website URL: `https://github.com/itsallcode/openfasttrace-intellij-plugin`
* Source code URL: `https://github.com/itsallcode/openfasttrace-intellij-plugin`
* Issue tracker URL:
  `https://github.com/itsallcode/openfasttrace-intellij-plugin/issues`
* Vendor display name: `itsallcode.org`
* Vendor email: keep `opensource@itsallcode.org`
* Vendor URL: keep `https://itsallcode.org/`
* Change notes source: render `doc/changes/changes_<version>.md` to generated
  HTML with Pandoc so release automation can reuse the maintained Markdown
  changelog.
* Compatibility: keep `ideaVersion.sinceBuild = "261"` and no explicit
  `untilBuild`.
* Marketplace wording: avoid sales claims and keep the OSS tone sober.

## Task List

- [x] Create and checkout a new Git branch `documentation/51-complete-marketplace-facing-plugin-metadata`

### Requirements And Design

- [x] Add or update `doc/system_requirements.md` with a user-visible plugin
      distribution requirement for Marketplace and IDE Plugin Manager metadata,
      covering description, change notes, relevant links, and descriptor
      identity metadata
- [x] Add acceptance scenarios for displaying useful Marketplace-facing
      metadata from the packaged plugin descriptor after `buildPlugin`
- [x] Stop and ask user for a review of the system requirements
- [x] Update `doc/design/deployment_view.md` with the packaged descriptor
      metadata expected in the plugin ZIP and the maintained source for
      description and change notes
- [x] Update `doc/design/building_block_view.md` with a build/distribution
      design item that owns descriptor metadata patching through Gradle and
      static descriptor fields
- [x] Update `doc/design/architecture_decisions.md` with the Pandoc build-tool
      decision for Markdown-to-HTML change-note rendering
- [x] Keep runtime design coverage to one scenario per `dsn` item, or use OFT
      forwarding where the deployment/build layer adds no new behavior
- [x] Stop and ask user for a review of the design

### Implementation

- [x] Replace the current short source `plugin.xml` description with a
      Marketplace-ready description source that starts with a concise English
      summary and then describes authoring requirements, running traces, and
      debugging broken requirement chains for developers first, followed by
      requirement engineers, quality engineers, technical writers, and other
      OpenFastTrace users
- [x] Configure `intellijPlatform.pluginConfiguration.description` from the
      maintained description source so the patched descriptor contains the
      Marketplace-facing description
- [x] Configure `intellijPlatform.pluginConfiguration.changeNotes` from the
      active `doc/changes/changes_<version>.md` release notes so the workflow
      can be automated from the maintained changelog
- [x] Render the active Markdown release notes with Pandoc during descriptor
      patching instead of maintaining custom Markdown-to-HTML conversion logic
- [x] Configure the descriptor website URL through the supported `plugin.xml`
      or Gradle mechanism with the GitHub repository URL
- [x] Configure Marketplace source-code and issue-tracker links to the GitHub
      repository and GitHub issues page where JetBrains supports them; otherwise
      record them as Marketplace admin-panel release tasks
- [x] Set or keep the plugin name as `OpenFastTrace`
- [x] Set the vendor display name to `itsallcode.org` while keeping the current
      vendor email and URL
- [x] Review `version`, `ideaVersion.sinceBuild`, and absence of an
      unnecessary `untilBuild` against JetBrains compatibility guidance, then
      keep the current compatibility stance unless that review finds a concrete
      descriptor problem
- [x] Add or update OFT build and implementation coverage tags for the new
      design items
- [x] Avoid adding third-party runtime or build dependencies unless the design
      review explicitly approves them

### Verification

- [x] Add or update automated tests for maintained metadata source files where
      practical, including checks that required description and change-note
      files exist and are not blank
- [x] Run `./gradlew --warning-mode=all test`
- [x] Run `./gradlew --warning-mode=all traceRequirements`
- [x] Run `./gradlew --warning-mode=all check buildPlugin`
- [x] Inspect the built plugin ZIP and its main plugin JAR to confirm the
      patched `META-INF/plugin.xml` contains the expected name, version,
      vendor, website URL, description, change notes, and compatibility
      metadata
- [x] Confirm the Marketplace description follows JetBrains listing guidance:
      the confirmed first-summary text, sober OSS wording, developer-first
      audience order, useful workflow bullets, and no screenshots or media
      embedded in the descriptor
- [x] Run `./gradlew --warning-mode=all verifyPlugin`
- [x] Keep path coverage at or above the documented 80 percent threshold
- [x] Keep the OpenFastTrace trace clean for `feat`, `req`, `scn`, `dsn`,
      `impl`, `utest`, `itest`, and build artifact types in scope
- [x] Keep the dependency policy unchanged, or document and approve any
      exception before implementation
- [ ] Keep the SonarQube Cloud quality gate green after CI analysis

`./gradlew --warning-mode=all test` passes.

`./gradlew --warning-mode=all traceRequirements` passes.

`./gradlew --warning-mode=all check buildPlugin` passes and produces
`build/distributions/OpenFastTrace-0.8.1.zip`.

Inspecting `META-INF/plugin.xml` from the plugin main JAR inside the built ZIP
confirms the descriptor contains the GitHub project URL, `OpenFastTrace` name,
`0.8.1` version, `itsallcode.org` vendor, `since-build="261"` with no
`until-build`, Marketplace description, and change notes rendered from
`doc/changes/changes_0.8.1.md`.

`./gradlew --warning-mode=all verifyPlugin` passes. The verifier marks the
plugin compatible with `IU-261.25134.95` and `IU-262.7132.23` and still reports
the existing experimental `BackgroundUpdateHighlightersUtil` API usage in
`OftHighlightingPass`.

### Update User Documentation

- [x] Update `README.md` only where needed to keep the maintained metadata
      source, project links, and release documentation consistent
- [x] Update the active changelog/release notes so Marketplace change notes have
      a current source that includes GH-51 and explains that users get a clearer
      Marketplace and IDE Plugin Manager overview before installing or updating
      the plugin
- [x] Add a release checklist note for Marketplace admin-only links such as
      source code or issue tracker if JetBrains does not support those links in
      the descriptor

## Version And Changelog Update

- [x] Check if the current version mentioned in the build scripts and code
      parameters is the same as the latest GitHub release
- [x] Raise the version to `0.8.1` because this is a documentation/packaging
      patch release and `0.8.0` is already the latest GitHub release as of
      2026-06-10
- [x] Write the changelog entry for `0.8.1`
- [x] Word the GH-51 changelog entry from the user's perspective: the plugin now
      presents clearer overview metadata and release notes in JetBrains plugin
      surfaces, making it easier to understand what the plugin does before
      installing or updating it
- [x] Determine the bundled OpenFastTrace library version from the Gradle
      dependency metadata
- [x] Write the bundled OpenFastTrace version into the fixed changelog location
      for `0.8.1`
- [x] Update the release date to 2026-06-10 or the actual implementation date
- [x] Ensure that the issue list contains `#51: Complete Marketplace-facing
      plugin metadata`

Current `gradle.properties` version is `0.8.1`; latest GitHub release is
`0.8.0`, published on 2026-06-09. This is a patch release.

`./gradlew dependencyInsight --dependency org.itsallcode.openfasttrace:openfasttrace --configuration runtimeClasspath`
reports `org.itsallcode.openfasttrace:openfasttrace:4.5.0`, selected by
dependency locking. `doc/changes/changes_0.8.1.md` contains
`## Bundled OpenFastTrace` followed by `OpenFastTrace 4.5.0`.
