# Contributing

Keep client game code under `src/client/java`, loader adapters under `src/fabric` / `src/neoforge`, and loader-neutral policy/persistence under `src/main/java`.
Add version-dependent branches with Manifold; keep `gradle/targets.gradle` as the version matrix.
Preserve independent feature policies and the game's original debug toggle state. Prefer local generation hooks over changes to global render state.

Run `.\gradlew.bat testAll collectArtifacts --no-parallel` before submitting a change.
Update all four language files when changing UI text. Follow `docs/TESTING.md` for changes to render hooks.
