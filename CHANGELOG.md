# Changelog

All notable changes to the Analyse addons will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] &mdash; 2026-04-18

First public release of the addon source repository under the Analyse brand.

### Added

- Source repository published on GitHub under the [Analyse Proprietary License](LICENSE).
- `CONTRIBUTING.md`, `SECURITY.md`, and GitHub issue / PR templates.
- CI workflow that builds every addon on push and pull request.
- Release workflow that bundles every addon jar into a GitHub Release on `gradle.properties` version bumps.
- Dependabot updates for Gradle and GitHub Actions.
- Cursor rules for the Analyse Java style guide and Conventional Commits.

### Changed

- **BREAKING:** Renamed from "ServerStats Addons" to "Analyse Addons" following the upstream plugin rename.
- **BREAKING:** Package moved from `com.serverstats.addon.*` to `net.analyse.addon.*`.
- **BREAKING:** Gradle group moved from `com.serverstats.addon` to `net.analyse.addon`.
- **BREAKING:** Jar artefacts renamed from `serverstats-addon-<name>-<version>.jar` to `analyse-addon-<name>-<version>.jar`.
- **BREAKING:** Install path moved from `plugins/ServerStats/addons/` to `plugins/Analyse/addons/`.
- **BREAKING:** Compile-only dependency swapped from `com.serverstats:serverstats-api` to `net.analyse:analyse-api`.
- Addons now look up the host plugin under the name `Analyse` when registering Bukkit listeners.
- Static event API updated from `ServerStats.trackEvent(...)` to `Analyse.trackEvent(...)`.

## Versioning

- **MAJOR** version bumps are reserved for breaking changes (package moves, addon removals, `analyse-api` major bumps).
- **MINOR** bumps add new addons or tracked events in a backwards-compatible way.
- **PATCH** bumps are bug fixes and internal changes that don't affect config or tracked event names.
