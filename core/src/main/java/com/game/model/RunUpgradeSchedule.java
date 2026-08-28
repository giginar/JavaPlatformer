package com.game.model;

/** Score thresholds for temporary upgrades earned during a single dive. */
public final class RunUpgradeSchedule {
    private static final float[] OPENING_THRESHOLDS = {1000f, 2500f, 5000f};
    private static final float REPEATING_SCORE_INTERVAL = 2500f;

    private RunUpgradeSchedule() {
    }

    public static float scoreForSelection(int selectionIndex) {
        if (selectionIndex < 0) {
            throw new IllegalArgumentException("Selection index cannot be negative");
        }
        if (selectionIndex < OPENING_THRESHOLDS.length) {
            return OPENING_THRESHOLDS[selectionIndex];
        }

        int selectionsAfterOpening = selectionIndex - OPENING_THRESHOLDS.length + 1;
        return OPENING_THRESHOLDS[OPENING_THRESHOLDS.length - 1]
            + selectionsAfterOpening * REPEATING_SCORE_INTERVAL;
    }
}
