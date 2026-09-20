package com.game.settings;

import com.badlogic.gdx.math.MathUtils;

/** Shared policy for replacing rapid visual pulses with steady, readable states. */
public final class FlashEffectPolicy {
    private FlashEffectPolicy() {
    }

    public static float pulse(float base, float amplitude, float phase) {
        return pulse(DisplaySettings.flashEffectsEnabled(), base, amplitude, phase);
    }

    public static float pulse(boolean enabled, float base, float amplitude, float phase) {
        return enabled ? base + MathUtils.sin(phase) * amplitude : base;
    }

    public static boolean blink(boolean active, boolean blinkFrame) {
        return blink(DisplaySettings.flashEffectsEnabled(), active, blinkFrame);
    }

    public static boolean blink(boolean enabled, boolean active, boolean blinkFrame) {
        return enabled && active && blinkFrame;
    }

    public static boolean flash(boolean active) {
        return DisplaySettings.flashEffectsEnabled() && active;
    }
}
