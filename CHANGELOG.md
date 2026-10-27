# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This changelog records changes to the mod template itself. The example mod's version always remains **0.1.0**.

## [0.1.0] - 2026-10-18

### Added

- A main-menu Template Info button exercising conditional compilation and version-specific Minecraft screen APIs.
- A graphical client verification script that checks the demo on every Minecraft/loader target.
- Local Modrinth bundle packaging via `scripts/build-modrinth.ps1`.
- Minimal Fabric and NeoForge mod entrypoints under `com.example.mod`.
- Minecraft 1.21.1, 1.21.11, 26.2, and 26.3 build targets.
- Shared sources, Manifold preprocessing, JUnit scaffolding, IDE runs, artifact checks, and local distribution packaging.
