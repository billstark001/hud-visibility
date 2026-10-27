package io.github.billstark001.hudvisibility;

/** Independent render producers; never changes the game's debug toggle state. */
public enum Feature {
    HITBOXES("hitboxes"), CHUNK_BORDERS("chunk_borders"), PLAYER_NAMES("player_names"),
    ENTITY_NAMES("entity_names"), TEXT_DISPLAY_NAMES("text_display_names"),
    TEXT_DISPLAYS("text_displays"), HANDS("hands");

    private final String key;
    Feature(String key) { this.key = key; }
    public String key() { return key; }
}
