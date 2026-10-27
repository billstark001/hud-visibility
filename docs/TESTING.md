# Acceptance checklist

Run each supported Minecraft/loader pair with its matching Cloth Config (and Fabric API on Fabric).
Build with `testAll collectArtifacts --no-parallel` before starting runtime checks.

1. Open Options → HUD Visibility. Check both categories and seven toggles each. Change one toggle, cancel, reopen: unchanged. Save, restart, reopen: persisted.
2. Repeat opening the screen in `en_us`, `zh_cn`, `zh_tw`, and `ja_jp`; check translated labels/tooltips and long Japanese text.
3. In a creative test world, enable F3+B and F3+G. Verify both display with HUD visible, both disappear with F1, and both return after F1 without retoggling either shortcut.
4. Hide only hitboxes in the HUD-visible category: borders stay visible. Reverse the settings: hitboxes stay visible. Toggle F3+B/G while F1 is active, then restore the HUD: the latest vanilla states are respected.
5. Use a second player (or LAN test client), a named mob, a named armor stand, and a `text_display` with both text and a visible custom name. Verify each of the four name/content switches independently in both HUD states. Include below-name scores and team visibility rules.
6. Enable hands in the F1 category: hands/items remain visible while HUD is hidden. Disable hands in the HUD-visible category: both disappear with the HUD still visible. Verify maps, both hands, first/third person, sleeping, and spectator mode.
7. Change unrelated debug settings (such as the octree) and verify that this mod does not suppress them.
8. Optional future compatibility pass: repeat with version-matched Iris/Sodium and a shader pack. This is not part of the initial verified claims.

Example text display commands (adapt NBT component syntax to the game's version):
`/summon minecraft:text_display ~ ~1 ~ {text:'"HUD Visibility test"',CustomName:'"Display name"',CustomNameVisible:1b}`

Startup smoke checks validate initialization and Mixin application errors; they do not by themselves prove in-world visual behavior.
