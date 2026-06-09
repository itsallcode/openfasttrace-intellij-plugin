# GH-45 Gradle lock and version check

## Goal

Make the Gradle build deterministic enough for SonarCloud by using committed
dependency locks, and add an on-demand Gradle version check that reports newer
dependency and plugin versions. After adding the update check, refresh the
project's dependencies, Gradle plugins, IntelliJ Platform target, and Gradle
wrapper to the latest resolvable stable releases.

This is repository build maintenance only. It must not change IntelliJ plugin
runtime behavior or user-facing requirements except where a dependency update
requires keeping plugin support aligned with the upstream OpenFastTrace
contract.

## Scope

In scope:

* configure Gradle dependency locking for the project build
* generate and commit the Gradle lock files required by the chosen locking setup
* add `com.github.ben-manes.versions` as the minimal-effort Gradle plugin for
  checking available dependency, plugin, and Gradle wrapper updates
* update all project dependencies, Gradle plugins, the IntelliJ Platform target,
  and the Gradle wrapper to the latest resolvable stable releases
* refresh the committed lock metadata after intentional version changes
* keep version declarations centralized and avoid dynamic dependency versions
* update developer documentation for lock maintenance and version checks
* keep CI build, tracing, packaging, and plugin verification compatible with the
  new Gradle metadata

Out of scope:

* changing IntelliJ plugin runtime behavior or end-user IDE workflows
* changing `doc/system_requirements.md`
* broader dependency-management redesign beyond adding the Gradle Versions Plugin
* changing SonarCloud quality profiles, quality gates, or project permissions

## Design References

* [System Requirements](../system_requirements.md)
* [Design Decisions](../design/architecture_decisions.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Changeset Guidelines](README.md)

## Strategy

Treat GH-45 as a build configuration change. Prefer Gradle's built-in
dependency locking for deterministic dependency resolution, then commit only the
lock metadata that normal builds need.

Use the Gradle Versions Plugin
(`com.github.ben-manes.versions`) for the version check instead of custom Gradle
logic. The plugin provides the `dependencyUpdates` task for dependency updates
and also checks for newer Gradle releases. As of 2026-06-09, the Gradle Plugin
Portal lists `0.54.0` as the latest version.

Keep `dependencyUpdates` as an on-demand developer task, not part of the normal
`check` lifecycle, because available update results change over time and should
not make ordinary builds unstable. Configure it for the current release channel
and stable-version candidates if the default report is too noisy. Run it with
`--no-configuration-cache --no-parallel` on Gradle 9 because the plugin
documents that Gradle 9 parallel execution is incompatible with this task, and
the task is not clean with the project's default configuration-cache setting.

The user explicitly approved using the Gradle Versions Plugin as the
minimal-effort dependency-update solution. Because this still adds a third-party
build plugin, document the decision if the dependency policy requires an
architecture decision for build plugins as well as runtime libraries.

The user later requested updating all dependencies to the latest releases. Use
the `dependencyUpdates` report as the source for version candidates, update only
stable releases, and keep locked dependency resolution deterministic. If a
reported version is not reachable through the project's Gradle coordinates,
record that as an explicit unresolved update instead of forcing an invalid
coordinate.

## Task List

- [x] Create and checkout a new Git branch `refactoring/45-gradle-lock-and-version-check`

### Requirements And Design

- [x] Confirm that GH-45 does not require changes to `doc/system_requirements.md`
      because it affects repository build maintenance, not plugin behavior
- [x] Confirm that the existing build-tool design decision
      `dsn~plugin-build-uses-intellij-platform-gradle-plugin~1` still covers the
      Gradle build setup after dependency locking is enabled
- [x] Record that the user approved `com.github.ben-manes.versions` as the
      minimal-effort version-check plugin for GH-45
- [x] Add the narrow build-tool decision
      `dsn~gradle-dependency-maintenance-uses-locks-and-versions-plugin~1` to
      `doc/design/architecture_decisions.md`
- [x] Update `doc/design/quality_requirements.md` so it documents dependency
      locking and approved build-plugin scope
- [x] Proceed after the user requested plan execution without an additional
      documentation-review stop

### Build Integration

- [x] Configure Gradle dependency locking in `build.gradle.kts` or
      `settings.gradle.kts` for all relevant resolvable project configurations
- [x] Generate lock files with the Gradle-supported `--write-locks` flow and
      include the resulting lock metadata in the changeset
- [x] Verify that normal Gradle invocations use the committed locks and report
      lock drift instead of silently accepting changed dependency resolution
- [x] Add the Gradle Versions Plugin to `build.gradle.kts` with plugin ID
      `com.github.ben-manes.versions` and the current approved plugin version
      from the Gradle Plugin Portal
- [x] Configure the `dependencyUpdates` task so it reports stable available
      updates for project dependencies, test dependencies, Gradle plugins, and
      Gradle itself
- [x] Keep the `dependencyUpdates` task outside `check` and CI build gates unless
      the user later requests update checks as a scheduled/reporting workflow
- [x] Inline build-only constants in `build.gradle.kts`, keep only the release
      version in `gradle.properties`, and avoid dynamic versions
- [x] Update project dependencies, Gradle plugins, and the Gradle wrapper to the
      latest stable versions reported by `dependencyUpdates` when the
      corresponding Gradle coordinates are resolvable
- [x] Update the IntelliJ Platform target to the latest resolvable IDEA release
      available through the IntelliJ Platform Gradle Plugin repositories
- [x] Keep coverage-tag file-extension support aligned with the default
      extensions of the updated OpenFastTrace Tag Importer, including completion
      registration for newly supported XML coverage-tag files
- [x] Keep generated version-report output under `build/` or another ignored
      location so only source metadata and lock files are committed
- [x] Keep `.github/workflows/build.yml` and `.github/workflows/release.yml`
      compatible with dependency locking and the standard build lifecycle

### Update Developer Documentation

- [x] Update the README development section with the command that checks latest
      dependency and plugin versions:
      `./gradlew --no-configuration-cache --no-parallel dependencyUpdates -Drevision=release`
- [x] Update the README development section with the command for refreshing
      Gradle lock files after an intentional dependency or plugin version change

### Verification

- [x] Run the chosen Gradle lock generation command and confirm no unexpected
      files outside Gradle lock metadata are produced
- [x] Run `./gradlew --no-configuration-cache --no-parallel dependencyUpdates -Drevision=release`
      and confirm it reports dependency, plugin, and Gradle update information
      without failing the normal build
- [x] Run `./gradlew --warning-mode=all traceRequirements`
- [x] Run `./gradlew --warning-mode=all check buildPlugin`
- [x] Run `./gradlew --warning-mode=all verifyPlugin` and record any remaining
      pre-existing verifier findings if it is not green
- [x] Run `./gradlew --no-configuration-cache sonar -Dsonar.skip=true` locally
      to verify Sonar task wiring, and rely on CI for a real SonarCloud upload
      when no `SONAR_TOKEN` is available
- [x] Confirm no third-party runtime dependencies were added and that the only
      added third-party artifact is the approved build plugin
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifact
      types in scope
- [x] After the dependency refresh, rerun `./gradlew --write-locks dependencies`
      and confirm the lock file matches the updated dependency graph
- [x] After the dependency refresh, rerun `./gradlew --no-configuration-cache --no-parallel dependencyUpdates -Drevision=release`
      and record any remaining unavailable or unresolved update candidates
- [x] After the dependency refresh, rerun `./gradlew --warning-mode=all check buildPlugin`
- [x] After the dependency refresh, rerun `./gradlew --warning-mode=all verifyPlugin`
- [x] After the dependency refresh, rerun `./gradlew --no-configuration-cache sonar -Dsonar.skip=true`

`./gradlew --write-locks dependencies` generated and refreshed `gradle.lockfile`
as source-controlled lock metadata. After the dependency refresh, the lock file
records OpenFastTrace `4.5.0`, IntelliJ IDEA `2026.1.3` / `IU-261.25134.95`,
JUnit `6.1.0`, and the updated Gradle plugin dependency graph.

`./gradlew wrapper --gradle-version 9.5.1` updated the Gradle wrapper
distribution URL, wrapper jar, and wrapper scripts.

`./gradlew dependencyInsight --dependency org.itsallcode.openfasttrace:openfasttrace --configuration runtimeClasspath`
shows Gradle selecting `4.5.0` with the reason `Dependency version enforced by
Dependency Locking`.

`./gradlew --no-configuration-cache --no-parallel dependencyUpdates -Drevision=release`
passes after the refresh. It reports the declared project dependencies, Gradle
plugins, and Gradle `9.5.1` wrapper as up to date, except for the IntelliJ
Platform `test-framework` artifact where it reports `261.25134.95` to
`262.7132.36`. Attempting to move the project target to `intellijIdea("2026.2")`
failed because `idea:idea:2026.2` is not resolvable from the configured
IntelliJ Platform repositories, so the build remains on the latest resolvable
IDEA product coordinate `2026.1.3`. The task still cannot determine latest
versions for the
IntelliJ Platform pseudo-dependencies `bundledModule:intellij-platform-test-runtime`,
`bundledPlugin:com.intellij.java`, and `idea:idea`.

Updating OpenFastTrace to `4.5.0` added `xml` and `fxml` to the upstream Tag
Importer's default extensions. The plugin's supported coverage-tag file
extension list and its upstream-extension alignment test now include those
extensions. XML coverage-tag completion is also registered in `plugin.xml` and
covered by `OftCoverageTagCompletionTest`.

`./gradlew --warning-mode=all traceRequirements` passes.

`./gradlew --warning-mode=all check buildPlugin` passes.

`./gradlew --warning-mode=all verifyPlugin` passes. The verifier reports the
existing experimental `BackgroundUpdateHighlightersUtil` class and method usage
in `OftHighlightingPass` for the checked IDEs, but reports the plugin as
compatible.

`./gradlew --no-configuration-cache sonar -Dsonar.skip=true` passes and confirms
Sonar task wiring without requiring a `SONAR_TOKEN`.

## Version And Changelog Update

- [x] Check the current version mentioned in `gradle.properties` against the
      latest GitHub release before making a version decision
- [x] Keep the plugin version unchanged unless the project release policy
      requires a patch release for build-maintenance changes
- [x] If a changelog entry is needed, add GH-45 to the active release changelog
      as a build/refactoring entry and update the release date to the
      implementation date

Current `gradle.properties` version is `0.8.0`; latest GitHub release is
`0.7.0` as of 2026-06-09. The version remains `0.8.0`, and GH-45 is added to
the active `0.8.0` changelog as build maintenance.
