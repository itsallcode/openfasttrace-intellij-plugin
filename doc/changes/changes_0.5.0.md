# OpenFastTrace IntelliJ Plugin 0.5.0, released 2026-04-30

## Summary

This feature release adds indexed auto-completion for OpenFastTrace specification item IDs while users edit `Covers:` entries in supported specification documents.

## Features

* #24: Suggest declared specification item IDs in `Covers:` sections and rank matches by full-ID prefix, name-prefix, name-substring, and artifact-type prefix

## Build

* #27: Continue local and CI builds with a warning when OSS Index returns HTTP 429 because the configured plan quota may be exceeded
