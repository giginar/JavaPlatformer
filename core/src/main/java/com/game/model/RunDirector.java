package com.game.model;

import com.game.GameConfig;

/** Owns time-based stage pacing, scale-based travel distance, and milestone events. */
public final class RunDirector {
    public static final float BASE_SCROLL_SPEED_WORLD_UNITS = 92f;
    private static final float[] STAGE_DEPTHS_METERS = {35f, 240f, 560f, 1020f, 1680f, 2450f};

    private final float stageDuration;
    private int stage = 1;
    private float elapsed;
    private float distanceMeters;
    private int nextMilestoneMeters = 500;
    private boolean finaleReady;

    public RunDirector() {
        this(GameConfig.stageDurationSeconds());
    }

    public RunDirector(float stageDuration) {
        this.stageDuration = Float.isFinite(stageDuration)
            ? Math.max(GameConfig.MIN_STAGE_DURATION_SECONDS, stageDuration)
            : GameConfig.DEFAULT_STAGE_DURATION_SECONDS;
    }

    public UpdateResult update(float delta) {
        if (delta <= 0f || finaleReady) {
            return UpdateResult.NONE;
        }

        GameBalance.Difficulty before = difficulty();
        elapsed += delta;
        distanceMeters += worldUnitsToMeters(
            BASE_SCROLL_SPEED_WORLD_UNITS * before.scrollSpeedMultiplier() * delta);

        boolean stageChanged = false;
        int calculatedStage = Math.min(GameBalance.stageCount(),
            1 + (int) (elapsed / stageDuration));
        if (calculatedStage > stage) {
            stage = calculatedStage;
            stageChanged = true;
        }

        if (elapsed >= stageDuration * GameBalance.stageCount()) {
            finaleReady = true;
        }

        int reachedMilestone = 0;
        if (distanceMeters >= nextMilestoneMeters) {
            reachedMilestone = nextMilestoneMeters;
            nextMilestoneMeters = nextMilestoneMeters == 500 ? 1000 : nextMilestoneMeters + 1000;
        }
        return new UpdateResult(stageChanged, reachedMilestone, finaleReady);
    }

    public void advanceStageForDebug() {
        if (stage < GameBalance.stageCount()) {
            elapsed = stage * stageDuration;
        } else {
            elapsed = stageDuration * GameBalance.stageCount();
        }
    }

    public void jumpToFinaleForDebug() {
        stage = GameBalance.stageCount();
        elapsed = stageDuration * GameBalance.stageCount();
        finaleReady = true;
    }

    public GameBalance.Difficulty difficulty() {
        return GameBalance.difficultyForStage(stage);
    }

    public int stage() {
        return stage;
    }

    public float distanceMeters() {
        return distanceMeters;
    }

    public int displayMeters() {
        return Math.round(distanceMeters);
    }

    public int displayDepthMeters() {
        int index = Math.max(0, Math.min(GameBalance.stageCount() - 1, stage - 1));
        float depth = STAGE_DEPTHS_METERS[index]
            + (STAGE_DEPTHS_METERS[index + 1] - STAGE_DEPTHS_METERS[index]) * stageProgress();
        return Math.round(depth);
    }

    public int nextMilestoneMeters() {
        return nextMilestoneMeters;
    }

    public float stageProgress() {
        if (finaleReady) {
            return 1f;
        }
        return (elapsed % stageDuration) / stageDuration;
    }

    public boolean finaleReady() {
        return finaleReady;
    }

    public float stageDuration() {
        return stageDuration;
    }

    private static float worldUnitsToMeters(float worldUnits) {
        return worldUnits * GameConfig.DIVER_REFERENCE_HEIGHT_METERS
            / GameConfig.DIVER_REFERENCE_HEIGHT_WORLD_UNITS;
    }

    public record UpdateResult(boolean stageChanged, int milestoneMeters, boolean finaleReady) {
        private static final UpdateResult NONE = new UpdateResult(false, 0, false);
    }
}
