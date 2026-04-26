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
* [OpenFastTrace User Guide](https://github.com/itsallcode/openfasttrace/blob/main/doc/user_guide.md)
* [OpenFastTrace in IDE Help Action](src/main/java/org/itsallcode/openfasttrace/intellijplugin/help/OpenFastTraceUserGuide.java)

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

The plugin currently focuses on:
* syntax highlighting for OFT items in supported files
* navigation support (`Go To Declaration`, symbol search, and related navigation paths)
* project-wide OFT tracing from `Tools | OpenFastTrace | Trace Project`
* clickable specification item IDs directly from the trace output window, including generated IDs created from coverage tags
* quick access to the OpenFastTrace user guide from the IDE help menu

For expected behavior and scope details, see:
* [System Requirements](doc/system_requirements.md)
* [Runtime View](doc/design/runtime_view.md)

## Getting the Project
Clone this repository and build the plugin with Gradle:

```sh
./gradlew build
```

For manual IDE testing, launch a sandbox IDE with the plugin:

```sh
./gradlew manualTestIde
```

After the sandbox IDE opens a project, run an OFT trace from `Tools | OpenFastTrace | Trace Project` or use the default shortcut `Ctrl+Alt+Shift+O`. The plugin traces the opened project directory in the background and shows the plain text result in an IDE output tab with ANSI colors preserved. You can click specification item IDs in the report to jump to their declarations, including source-side items generated from coverage tags.

## Installation
### Runtime Dependencies
You need a JetBrains IDE based on the IntelliJ Platform (for example, IntelliJ IDEA Community Edition) to run the plugin.

### Development Dependencies
To build and test from source, use the project’s configured Gradle wrapper and a compatible JDK.

## Development
To understand product intent and implementation scope, start with:
* [System Requirements](doc/system_requirements.md)
* [Design](doc/design.md)
* [Change Log](doc/changes/changelog.md)

To run the automated checks for the trace action and the project trace itself:

```sh
./gradlew check verifyPlugin
```

Example OFT files for manual testing are available under `examples\` in this project.

## License
This project is licensed under the [Apache 2.0 license](LICENSE).
