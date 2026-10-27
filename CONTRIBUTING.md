# Contributing

## Validation

Run the unit suite and verify the complete build matrix:

```powershell
.\gradlew.bat testAll verifyArtifacts --no-parallel
```

Add JUnit tests for loader-neutral behavior under `tests/unit/src/test/java`.
Changes involving game or loader APIs also need the relevant development client or server run.
The optional NeoForge startup smoke script requires Windows and a graphical desktop session.

When changing supported versions, update `gradle/targets.gradle` and the README table.
Builds, artifact collection, IDE tasks, and the smoke script derive their target list from that Gradle file.
Do not check in generated worlds, IDE files, logs, screenshots, caches, or build output.

## Commit messages

Use an imperative `<type>: <summary>` subject, with motivation and compatibility details in the body where useful.

- `feat:` new mod behavior.
- `fix:` bug or compatibility correction.
- `add:` new tests, tooling, or supporting material.
- `docs:` documentation.
- `refactor:` behavior-preserving restructuring.
- `test:` corrections to tests.
- `build:` dependencies, packaging, or Gradle changes.
- `ci:` automation.
- `chore:` other maintenance.

Use `!` for breaking changes and explain migration requirements. Keep each commit focused.

## Changelog

Record user-visible and contributor-visible changes under `Unreleased` in `CHANGELOG.md`.
Use the standard Added, Changed, Deprecated, Removed, Fixed, and Security categories.
Move entries into a dated version section when releasing and add appropriate version links once a repository exists.
