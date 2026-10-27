package io.github.billstark001.hudvisibility;

/** True permits otherwise eligible content. It does not turn F3 switches on. */
public record VisibilityRule(boolean withHud, boolean withoutHud) {
    public static final VisibilityRule DEFAULT = new VisibilityRule(true, false);
    public boolean isVisible(boolean hudHidden) { return hudHidden ? withoutHud : withHud; }
}
