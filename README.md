# HUD Visibility

A client-side Fabric / NeoForge mod that controls world overlays, name tags, text displays, and first-person hands in both F1 states.

客户端可见性控制：对每项功能分别设置「HUD 显示时」与「HUD 隐藏时（F1）」是否显示。默认正常显示，按 F1 后全部隐藏。

| Independent feature | What it controls |
| --- | --- |
| Entity hitboxes (F3+B) | Boxes, eye-height, direction lines, and the integrated-server hitbox helper |
| Chunk borders (F3+G) | Chunk border geometry only |
| Player name tags | Player names and below-name scores |
| Other entity name tags | Non-player entities except `text_display` |
| Text display name tags | The optional custom name of a `text_display` |
| Text display content | The actual text and background of a `text_display`, independently of its name |
| First-person hands and items | Both hands and held items |

Each feature has two boolean switches. **ON** allows otherwise eligible content; **OFF** suppresses it.
F3+B and F3+G still need to be enabled manually. Changing F1 never changes or resets those debug switches.
Enabling names or hands in the F1 category allows them to remain visible with the HUD hidden.
Name distance, team visibility, invisibility, first-person camera, sleep, and spectator restrictions remain in effect.
Other debug renderers, including octrees, are outside this version's scope.

## Install and configure

Install the matching JAR and **Cloth Config** for your Minecraft version and loader. Fabric also requires **Fabric API**.
Open **Options → HUD Visibility**. Fabric's optional Mod Menu and NeoForge's Mods configuration button open the same Cloth Config screen.
Changes apply when you save; cancelling leaves the active configuration unchanged. No restart is needed.

Languages: English, 简体中文, 繁體中文, 日本語. Minecraft's language selection controls the interface.
Settings are saved as UTF-8 in `config/hud_visibility.properties`. `*.with_hud` and `*.without_hud` correspond to the two categories.
Invalid settings are logged and the existing file is preserved; defaults are used until the file is corrected or explicitly saved through the UI.

## Version matrix

The initial matrix keeps every default version of the upstream template, with both loaders:

| Minecraft | Java | Fabric API | NeoForge | Cloth Config |
| --- | --- | --- | --- | --- |
| 1.21.1 | 21 | 0.116.17+1.21.1 | 21.1.262 | 15.0.140 |
| 1.21.11 | 21 | 0.141.4+1.21.11 | 21.11.44 | 21.11.160 |
| 26.2 | 25 | 0.152.2+26.2 | 26.2.0.37-beta | 26.2.155 |
| 26.3 | 25 | 0.161.0+26.3 | 26.3.0.6-beta | 26.3.159 |

Eight separate JARs are produced. Minecraft versions are deliberately exact in the metadata.
As checked on 2026-10-28, [Fabric metadata](https://meta.fabricmc.net/v2/versions/game) includes 26.4 snapshots,
but [Cloth Config's Maven metadata](https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-fabric/maven-metadata.xml)
has no 26.4 release, and [NeoForge Maven](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml)
has no published 26.4 artifact. This version stops at 26.3; snapshot support is deferred.

## Build and verify

Run Gradle with JDK 21 or newer (25 recommended); toolchains select Java 21/25 for the appropriate target.

```powershell
.\gradlew.bat testAll collectArtifacts --no-parallel
```

Use `./gradlew` on Linux/macOS. Output: `build/distributions/hud-visibility-0.1.0+<minecraft>-<loader>.jar`.
Individual tasks look like `:versions:fabric-1.21.1:build` or `:versions:neoforge-26.3:runClient`.
`verifyArtifacts` checks all eight artifacts, their dependencies, entrypoints, mixins, translations, and Java levels.
Unit tests cover policy combinations, independent feature switches, staged edits, persistence, missing settings, and malformed files.

`scripts/run-local-client-smoke.ps1` launches the selected graphical clients, checks startup logs for initialization and Mixin errors, and stops only the processes it started.
This is a startup check; see [docs/TESTING.md](docs/TESTING.md) for in-world acceptance steps.

## Implementation and compatibility

Shared source uses the template's Manifold `MC_VERSION` preprocessing. `gradle/targets.gradle` owns the dependency matrix.
Mixins cancel the individual F3+B / F3+G producer, rather than the aggregate debug renderer or final line-rendering pass.
1.21.1 uses the two hitbox helpers and `ChunkBorderRenderer.render`; newer targets use the two individual `emitGizmos` methods.
Names are suppressed during extraction (or at the eligibility query in 1.21.x); `text_display` content is suppressed before its text nodes are generated.
Hands alter only the hand-specific HUD predicate and use the frame's HUD snapshot on 26.x.
No framebuffer, shader uniform, depth state, global HUD flag, or F3 setting is changed.

These narrow hooks are intended to reduce shader conflicts. **Iris/Sodium/shader-pack compatibility has not been tested.**
Third-party mods that replace these producers or draw their own overlays may need separate integration.

Based on [billstark001/multi-version-loader-example-mod](https://github.com/billstark001/multi-version-loader-example-mod). MIT license; upstream notices retained.
