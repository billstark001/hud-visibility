# Releasing

CI validates the Gradle wrapper and GitHub Actions syntax, runs the Java and release-packaging tests,
builds all eight targets, and checks their injection points, metadata, dependencies and translations.
Only distributable JARs are included in the release bundle; source/development JARs are excluded.

## Prepare a version

1. Set `mod_version` in `gradle.properties` and add its nonempty section at the top of `CHANGELOG.md`.
2. Keep `gradle/targets.gradle` current. `writeTargetManifest` supplies the Minecraft matrix to release packaging and Modrinth automatically.
3. Run the checks below, commit, push `main`, and wait for CI to pass.

```powershell
python -m unittest discover -s tests/release -v
.\gradlew.bat testAll collectArtifacts writeTargetManifest --no-parallel
python scripts/prepare-release.py --tag v0.1.0
```

Replace the example tag with the version being released. Packaging rejects a mismatched tag/changelog,
a stale target manifest, missing JARs, or unexpected JARs. Output is in `build/release/`:
eight runtime JARs, release notes, `SHA256SUMS`, and `manifest.json` containing the source commit and per-file hashes.

## GitHub first

```powershell
git tag -a v0.1.0 -m "HUD Visibility v0.1.0"
git push origin v0.1.0
```

The **Release** workflow checks out the exact tag, validates it against the version and changelog, tests/builds once,
then publishes the eight JARs, checksums and manifest to GitHub. A tag push publishes only to GitHub.
Versions with a suffix become GitHub prereleases; stable versions become regular releases.
The publishing job alone has `contents: write`; the build receives no publishing secrets.

To retry an existing tag, run **Release → Run workflow**, enter its tag and leave `publish_modrinth` disabled.
This updates that tag's GitHub Release. Never move an already published tag to different code.

## Optional Modrinth publication

Create the Modrinth project separately. Configure these repository settings:

- Variable `MODRINTH_PROJECT_ID`: the project's ID or slug.
- Secret `MODRINTH_TOKEN`: a token permitted to create project versions.

Run **Release** manually for the existing tag with `publish_modrinth` enabled. For example:

```powershell
gh workflow run release.yml -f tag=v0.1.0 -f publish_modrinth=true
```

GitHub must publish successfully before the Modrinth jobs run. The jobs use the same verified build bundle,
with one version per Minecraft/loader pair, an exact game version, and client-side metadata.
Both loaders declare Cloth Config as required. Fabric additionally declares Fabric API as required and Mod Menu as optional.
An explicitly requested Modrinth run fails clearly if its project ID or token is missing; ordinary GitHub releases need neither.

Use `README_MODRINTH.md` for the project description. The local `scripts/build-modrinth.ps1` command only collects JARs; it never uploads them.
