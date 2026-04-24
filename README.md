# OpenFastTrace IntelliJ Plugin

## What is OpenFastTrace IntelliJ Plugin?
The OpenFastTrace IntelliJ Plugin adds editor and navigation support for OpenFastTrace (OFT) artifacts directly in JetBrains IDEs based on the IntelliJ Platform.

It helps users work with OFT specification items and trace links in the same environment where they already edit code and documentation.

![OFT Logo](doc/images/openfasttrace_logo.svg)

## Project Information
This repository contains the plugin implementation, tests, and OpenFastTrace-based product documentation.

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

Example OFT files for manual testing are available under `examples\` in this project.

## License
This project is licensed under the [Apache 2.0 license](LICENSE).
