# GH-27 Skip OSSIndex on HTTP 429

## Goal

Keep OSS Index dependency security scanning as a build breaker for detected vulnerabilities, but let local and CI builds continue when OSS Index rejects the audit with HTTP 429 because the configured plan quota may be exceeded.

The build must print a clear warning that OSS Index returned HTTP 429, that the quota or plan may need checking, and that the dependency security result is unavailable for this run.

## Scope

In scope:

* handle HTTP 429 responses from the OSS Index audit in local Gradle runs and the GitHub Actions build workflow
* keep the OSS Index audit fatal for detected vulnerabilities and non-429 failures
* keep the existing OSS Index credentials, cache, and vulnerability-detection configuration
* update the build quality documentation so the documented security-gate policy matches the quota exception
* avoid new third-party dependencies

Out of scope:

* ignoring vulnerability findings from OSS Index
* treating authentication failures, network failures, server errors other than 429, or malformed responses as successful audits
* changing SonarQube, OpenFastTrace tracing, coverage, plugin verifier, or packaging gates
* adding OSS Index auditing to workflows that do not currently run it
* changing the plugin runtime behavior or end-user IDE features

## Design References

* [System Requirements](../system_requirements.md)
* [Design Decisions](../design/architecture_decisions.md)
* [Quality Requirements](../design/quality_requirements.md)

## Strategy

GH-27 changes repository build behavior, not the IntelliJ plugin's user-facing feature set. The product requirements should therefore stay unchanged unless implementation work reveals an unexpected user-visible effect.

The preferred implementation is to keep the existing `ossIndexAudit` Gradle task name as the local and CI entrypoint, but make the task quota-aware. The build should catch only OSS Index failures that clearly report HTTP 429, log a Gradle warning with the quota/plan guidance from the issue, and then continue. All other audit failures must be rethrown so credentials problems, service failures, and vulnerable dependencies remain build failures.

The current Sonatype Gradle plugin reports non-200 OSS Index responses through a wrapped Gradle failure whose message includes the HTTP status. The implementation should isolate the 429 detector in a small helper and test it against representative nested exceptions instead of matching a broad human-readable sentence inline.

If Gradle does not allow adapting the existing `ossIndexAudit` task safely, introduce a clearly named quota-aware wrapper task and update the CI workflow and README development instructions to use that wrapper. Prefer preserving `ossIndexAudit` only if the implementation remains readable and verifiable.

## Task List

### Requirements And Design

- [x] Confirm that GH-27 does not require changes to `doc/system_requirements.md` because it affects repository build policy, not plugin behavior
- [x] Update `doc/design/quality_requirements.md` so OSS Index remains a build breaker for vulnerabilities while documenting HTTP 429 as a quota-related warning exception
- [x] Add or extend a build design item in `doc/design/architecture_decisions.md` for quota-aware OSS Index audit behavior
- [x] Add the corresponding `bld` coverage tag in `build.gradle.kts`
- [x] Proceed after the user reviewed the changeset plan and requested execution without an additional documentation-review stop

### Build Integration

- [x] Add a focused helper that recognizes OSS Index HTTP 429 failures from the Gradle/Sonatype exception chain
- [x] Make the OSS Index audit log a warning and continue only when the helper identifies HTTP 429
- [x] Keep the audit failing when vulnerabilities are detected and when any non-429 audit error occurs
- [x] Preserve the current OSS Index credentials, cache, banner, color, and fail-on-detection settings
- [x] Keep the GitHub Actions build workflow using the same quota-aware audit entrypoint as local builds
- [x] Update README development instructions only if the local audit command changes from `ossIndexAudit`

### Automated Verification

- [x] Add tests for the 429 detector using a representative nested exception message from the Sonatype Gradle plugin
- [x] Add negative tests proving the detector does not accept vulnerability failures, authentication failures, ordinary connection failures, or non-429 HTTP status failures
- [x] Verify the quota-aware audit path prints the warning text required by GH-27
- [x] Keep the OpenFastTrace trace clean for the requirement and design artifacts in scope
- [x] Keep path coverage at or above the documented threshold
- [x] Keep dependency policy unchanged and avoid adding new third-party libraries
- [ ] Keep required Gradle test, trace, packaging, and plugin verification tasks green
- [ ] Keep SonarQube Cloud quality-gate checks green
- [ ] Keep OSS Index audit results clean when the service responds normally

`./gradlew --warning-mode=all spotlessCheck check buildPlugin` passes.

`./gradlew --warning-mode=all buildSrc:test` passes.

`./gradlew --warning-mode=all verifyPlugin` still fails because of pre-existing IntelliJ experimental API usages in `OftHighlightingPass` and internal API usages in `OftTraceRunContentOutputPresenter` that are unrelated to GH-27.

`./gradlew --warning-mode=all ossIndexAudit --info` fails locally with HTTP 401 when run anonymously, proving non-429 OSS Index failures still fail the build. A clean authenticated OSS Index result was not available in the local environment.

## Version And Changelog Update

- [x] Update the changelog for the current version. Don't increase the version, because it has not yet been released.
