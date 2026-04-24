# GH-13 Add badges to README.md

## Goal

Add project-status badges to `README.md` for the OpenFastTrace IntelliJ Plugin repository.

The badge set should mirror the relevant parts of the main OpenFastTrace project README while staying specific to this plugin repository.

## Scope

In scope:

* add the GitHub Actions build badge for this repository
* add the SonarCloud badges that apply to this repository and Sonar project
* place the badges in the `Project Information` section of `README.md`
* verify that each badge target points to the plugin repository or its SonarCloud project
* explicitly omit the Maven Central badge because this plugin is not published there
* update the project version to `0.2.0`
* draft the `0.2.0` changelog entry for the badge work and later follow-up changes

Out of scope:

* publishing the plugin to Maven Central
* adding badges for external systems that are not configured for this repository
* redesigning the README beyond the badge section layout
* adding a JetBrains Marketplace badge before the plugin has a published Marketplace entry

## Design References

* [System Requirements](../system_requirements.md)
* [Quality Requirements](../design/quality_requirements.md)
* [Solution Strategy](../design/solution_strategy.md)

## Strategy

Use the main OpenFastTrace README as the reference set and copy only the badges that have a meaningful equivalent for the IntelliJ plugin repository.

The first badge row should contain:

* the GitHub Actions build badge for `.github/workflows/build.yml`

The SonarCloud section should contain the same family of repository-quality badges used by the main project, but configured for the IntelliJ plugin SonarCloud project:

* Quality Gate
* Bugs
* Code Smells
* Coverage
* Duplicated Lines
* Lines of Code
* Maintainability Rating
* Reliability Rating
* Security Rating
* Technical Debt
* Vulnerabilities

The README should not include a Maven Central badge because the plugin release process produces GitHub release artifacts and does not publish Maven coordinates.

The README should also not include a JetBrains Marketplace badge yet unless the plugin has a stable public Marketplace page to link to. If such a page becomes available later, it belongs in a follow-up issue.

## Task List

### Badge Selection

- [x] Determine the correct GitHub Actions build badge URL for this repository
- [x] Determine the correct SonarCloud project key and dashboard URL for this repository
- [x] Confirm that the Maven Central badge from the main OpenFastTrace README does not apply here
- [x] Confirm that no JetBrains Marketplace badge is available yet, or capture the Marketplace URL if it already exists

### README Update

- [x] Add the build badge under `Project Information` in `README.md`
- [x] Add the applicable SonarCloud badge group under `Project Information` in `README.md`
- [x] Keep the badge layout readable in GitHub Markdown rendering
- [x] Keep the surrounding README structure unchanged unless a small formatting adjustment is required for clarity

### Versioning And Changelog

- [x] Update the project version to `0.2.0`
- [x] Add a draft changelog file for `0.2.0`
- [x] Link the new `0.2.0` changelog entry from `doc/changes/changelog.md`
- [x] Keep the `0.2.0` changelog entry open for later changesets without assigning a code name yet

### Verification

- [x] Verify all badge image URLs resolve successfully
- [x] Verify all badge links open the intended workflow or SonarCloud dashboard
- [x] Verify `README.md` contains no Maven Central badge
- [x] Verify `README.md` contains no broken Markdown after the badge insertion
- [x] Keep the OpenFastTrace trace clean
