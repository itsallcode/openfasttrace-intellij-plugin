# OpenFastTrace IntelliJ Plugin

## What is OpenFastTrace IntelliJ Plugin?
The OpenFastTrace IntelliJ Plugin adds editor and navigation support for OpenFastTrace (OFT) artifacts directly in JetBrains IDEs based on the IntelliJ Platform.

It helps users work with OFT specification items and trace links in the same environment where they already edit code and documentation.

![OFT Logo](doc/images/openfasttrace_logo.svg)

## Project Information
This repository contains the plugin implementation, tests, and OpenFastTrace-based product documentation.

[![Build](https://github.com/itsallcode/openfasttrace-intellij-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/itsallcode/openfasttrace-intellij-plugin/actions/workflows/build.yml)

SonarCloud status:

[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=bugs)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=code_smells)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Duplicated Lines](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=ncloc)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=sqale_index)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=org.itsallcode.openfasttrace%3Aopenfasttrace-intellij-plugin)

**User Guides**
* [OpenFastTrace IntelliJ Plugin User Guide](doc/user_guide.md)
* [OpenFastTrace User Guide](https://github.com/itsallcode/openfasttrace/blob/main/doc/user_guide.md)
* [OpenFastTrace in IDE Help Action](src/main/java/org/itsallcode/openfasttrace/intellijplugin/help/OpenFastTraceUserGuide.java)
* [OpenFastTrace IntelliJ Plugin Demo](doc/demo/plugin-demo.md)

**News and Discussions**
* [Changelog](doc/changes/changelog.md)
* [OpenFastTrace Project Discussions](https://github.com/itsallcode/openfasttrace/discussions)

**Information for Contributors**
* [System Requirements](doc/system_requirements.md)
* [Design Documentation](doc/design.md)
* [Architecture Decisions](doc/design/architecture_decisions.md)
* [Change Sets](doc/changesets/README.md)

## Using the Plugin
Use an IntelliJ Platform IDE and install/build this plugin to get OFT support while editing project files.

For installation and workflow instructions, see the [OpenFastTrace IntelliJ Plugin User Guide](doc/user_guide.md).

The plugin currently focuses on:
* syntax highlighting for OFT items in supported files
* navigation support (`Go To Declaration`, symbol search, and related navigation paths)
* completion for existing specification item IDs while editing `Covers:` entries in supported specification documents and coverage-tag targets in files supported by the OpenFastTrace Tag Importer
* bundled OFT live templates for common specification items, including a scenario template
* project-wide or selected-resource OFT tracing from `Tools | OpenFastTrace | Trace Project` or through custom OpenFastTrace run configurations
* clickable specification item IDs directly from the trace output window, including generated IDs created from coverage tags
* IntelliJ Test Runner UI output for OpenFastTrace traces by default, grouped by source file, specification item, and trace link
* quick access to the OpenFastTrace user guide from the IDE help menu

For expected behavior and scope details, see:
* [User Guide](doc/user_guide.md)
* [System Requirements](doc/system_requirements.md)
* [Runtime View](doc/design/runtime_view.md)
* [Plugin Demo Script](doc/demo/plugin-demo.md)

## Getting the Project
Clone this repository and build the plugin with Gradle:

```sh
./gradlew build
```

For manual IDE testing, launch a sandbox IDE with the plugin:

```sh
./gradlew manualTestIde
```

## Trace Settings

After the sandbox IDE opens a project, configure trace scope under `Settings | Tools | OpenFastTrace`. By default, `Trace Project` scans the whole opened project. If you switch to selected-resource tracing, the plugin includes IntelliJ source roots, IntelliJ test roots, and one default additional project-relative path entry, `doc/`, until you change it. Additional files or directories are entered one per line.

## Run Configurations

You can create and run a dedicated `OpenFastTrace` run configuration from the IDE's run/debug toolbar. The plugin traces the configured inputs in the background and shows the result in IntelliJ's Test Runner UI by default. The result tree groups source files, specification items, and trace links with pass/fail status and navigation back to source.

Run configurations can select plain text output instead. In that mode, the plugin shows the text report in an IDE output tab with ANSI colors preserved. You can click specification item IDs in the report to jump to their declarations, including source-side items generated from coverage tags.

## Live-Templates Bundled With the Plugin

The plugin also bundles an `OpenFastTrace` live-template group under `Settings | Editor | Live Templates`. Use abbreviations such as `feat`, `req`, `dsn`, and `scn` in a supported editing context, then press `Tab` to insert an OFT item skeleton. The `scn` template inserts a scenario stub with placeholders for `Given`, `When`, `Then`, and the covered requirement. While the caret is still in a template's covered-item field, use basic completion to select an existing specification item ID from the project index.

## Installation
### Runtime Dependencies
You need a JetBrains IDE based on the IntelliJ Platform (for example, IntelliJ IDEA Community Edition) to run the plugin.

### Development Dependencies
To build and test from source, use the project’s configured Gradle wrapper and a compatible JDK.

## Icon Assets

The plugin logo asset `src/main/resources/META-INF/pluginIcon.svg` and the run-configuration icon asset `src/main/resources/icons/openfasttrace.svg` are derived from the OpenFastTrace logo artwork in this repository and use the same Apache 2.0 license as the project.

## Development
To understand product intent and implementation scope, start with:
* [System Requirements](doc/system_requirements.md)
* [Design](doc/design.md)
* [Change Log](doc/changes/changelog.md)

To run the automated checks for the trace action and the project trace itself:

```sh
./gradlew check verifyPlugin
```

To check for newer dependency, Gradle plugin, and Gradle wrapper versions:

```sh
./gradlew --no-configuration-cache --no-parallel dependencyUpdates -Drevision=release
```

After intentionally changing dependency or Gradle plugin versions, refresh the
committed Gradle lock file:

```sh
./gradlew --write-locks dependencies
```

Example OFT files for manual testing are available under `examples/` in this project.
For a guided live demonstration, use the script in [doc/demo/plugin-demo.md](doc/demo/plugin-demo.md) with the isolated example project in [doc/demo/example](doc/demo/example).

## License
This project is licensed under the [Apache 2.0 license](LICENSE).
