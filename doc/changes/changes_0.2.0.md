# OpenFastTrace IntelliJ Plugin 0.2.0

## Summary

In this release we added support for running a trace directly from within the IDE. The specification item IDs in the resulting trace output are navigable to make debugging easy.

## Features

* #12: Run an OpenFastTrace trace for the opened IntelliJ project from `Tools | OpenFastTrace | Trace Project` and show the plain text result in an IDE output tab
* #12: Preserve ANSI-colored OpenFastTrace report output for long traces without truncating the beginning of the report
* #12: Make specification item IDs in the OpenFastTrace trace output clickable so users can jump directly to declarations, including generated IDs from coverage tags
* #13: Add the GitHub Actions and SonarCloud badges to `README.md`
