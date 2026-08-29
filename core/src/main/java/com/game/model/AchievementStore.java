package com.game.model;

import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persistent achievement unlocks and the small set of career counters they need. */
public final class AchievementStore {
    private static final String UNLOCKED_PREFIX = "achievement.unlocked.";
    private static final String KILLS_KEY = "achievement.stats.kills";
    private static final String OXYGEN_KEY = "achievement.stats.oxygenPickups";
    private static final String POWER_UP_KEY = "achievement.stats.powerUps";

    private final Preferences preferences;

    public AchievementStore(Preferences preferences) {
        this.preferences = preferences;
    }

    public boolean isUnlocked(Achievement achievement) {
        return preferences.getBoolean(unlockKey(achievement), false);
    }

    public int unlockedCount() {
        int count = 0;
        for (Achievement achievement : Achievement.values()) {
            if (isUnlocked(achievement)) {
                count++;
            }
        }
        return count;
    }

    public List<Achievement> recordKill(int combo, boolean boss) {
        int kills = preferences.getInteger(KILLS_KEY, 0) + 1;
        preferences.putInteger(KILLS_KEY, kills);
        List<Achievement> unlocked = new ArrayList<>();
        unlock(unlocked, Achievement.FIRST_BLOOD);
        unlockAt(unlocked, Achievement.HUNTER_10, kills, 10);
        unlockAt(unlocked, Achievement.HUNTER_50, kills, 50);
        unlockAt(unlocked, Achievement.HUNTER_250, kills, 250);
        unlockAt(unlocked, Achievement.COMBO_3, combo, 3);
        unlockAt(unlocked, Achievement.COMBO_5, combo, 5);
        unlockAt(unlocked, Achievement.COMBO_10, combo, 10);
        if (boss) {
            unlock(unlocked, Achievement.BOSS_SLAYER);
        }
        preferences.flush();
        return immutable(unlocked);
    }

    public List<Achievement> recordOxygenPickup() {
        int pickups = preferences.getInteger(OXYGEN_KEY, 0) + 1;
        preferences.putInteger(OXYGEN_KEY, pickups);
        List<Achievement> unlocked = new ArrayList<>();
        unlock(unlocked, Achievement.FIRST_OXYGEN);
        unlockAt(unlocked, Achievement.OXYGEN_25, pickups, 25);
        preferences.flush();
        return immutable(unlocked);
    }

    public List<Achievement> recordPowerUpPickup() {
        int pickups = preferences.getInteger(POWER_UP_KEY, 0) + 1;
        preferences.putInteger(POWER_UP_KEY, pickups);
        List<Achievement> unlocked = new ArrayList<>();
        unlock(unlocked, Achievement.FIRST_POWER_UP);
        unlockAt(unlocked, Achievement.POWER_UPS_25, pickups, 25);
        preferences.flush();
        return immutable(unlocked);
    }

    public List<Achievement> recordRunProgress(float distanceMeters, float score,
                                                boolean noShots, boolean noCollections) {
        List<Achievement> unlocked = new ArrayList<>();
        unlockAt(unlocked, Achievement.SCORE_10K, score, 10_000f);
        unlockAt(unlocked, Achievement.SCORE_50K, score, 50_000f);
        unlockAt(unlocked, Achievement.SCORE_100K, score, 100_000f);
        unlockAt(unlocked, Achievement.DISTANCE_100, distanceMeters, 100f);
        unlockAt(unlocked, Achievement.DISTANCE_500, distanceMeters, 500f);
        unlockAt(unlocked, Achievement.DISTANCE_1000, distanceMeters, 1_000f);
        if (distanceMeters >= 100f && noShots) {
            unlock(unlocked, Achievement.SILENT_100);
        }
        if (distanceMeters >= 100f && noCollections) {
            unlock(unlocked, Achievement.MINIMALIST_100);
        }
        flushIfNeeded(unlocked);
        return immutable(unlocked);
    }

    public List<Achievement> recordStageComplete(int completedStage, RunSettings settings,
                                                  boolean noShots, boolean noCollections,
                                                  boolean noPowerUses, boolean noDamage) {
        List<Achievement> unlocked = new ArrayList<>();
        Achievement stageAchievement = switch (completedStage) {
            case 1 -> Achievement.REEF_CLEARED;
            case 2 -> Achievement.RUINS_CLEARED;
            case 3 -> Achievement.MAZE_CLEARED;
            case 4 -> Achievement.TRENCH_CLEARED;
            case 5 -> Achievement.RIFT_CLEARED;
            default -> null;
        };
        if (stageAchievement != null) {
            unlock(unlocked, stageAchievement);
        }
        if (noShots) {
            unlock(unlocked, Achievement.CEASEFIRE_STAGE);
        }
        if (noCollections) {
            unlock(unlocked, Achievement.EMPTY_HANDED_STAGE);
        }
        if (noPowerUses) {
            unlock(unlocked, Achievement.PURE_SKILL_STAGE);
        }
        if (noDamage) {
            unlock(unlocked, Achievement.FLAWLESS_STAGE);
        }
        if (settings.isChallengeRun()) {
            unlock(unlocked, Achievement.CHALLENGER);
        }
        if (settings.hasAllChallenges()) {
            unlock(unlocked, Achievement.LOCKDOWN_STAGE);
        }
        flushIfNeeded(unlocked);
        return immutable(unlocked);
    }

    public List<Achievement> recordVictory(RunSettings settings, boolean noShots,
                                            boolean noCollections, boolean noPowerUses,
                                            boolean noDamage) {
        List<Achievement> unlocked = new ArrayList<>();
        unlock(unlocked, Achievement.ABYSS_CONQUERED);
        if (noShots) {
            unlock(unlocked, Achievement.CEASEFIRE_RUN);
        }
        if (noCollections) {
            unlock(unlocked, Achievement.EMPTY_HANDED_RUN);
        }
        if (noPowerUses) {
            unlock(unlocked, Achievement.PURE_SKILL_RUN);
        }
        if (noDamage) {
            unlock(unlocked, Achievement.FLAWLESS_RUN);
        }
        switch (settings.difficulty()) {
            case EASY -> unlock(unlocked, Achievement.EASY_COMPLETE);
            case NORMAL -> unlock(unlocked, Achievement.NORMAL_COMPLETE);
            case HARD -> unlock(unlocked, Achievement.HARD_COMPLETE);
        }
        if (settings.hasAllChallenges()) {
            unlock(unlocked, Achievement.TOTAL_LOCKDOWN);
        }
        flushIfNeeded(unlocked);
        return immutable(unlocked);
    }

    public String progressText(Achievement achievement) {
        return switch (achievement) {
            case HUNTER_10 -> boundedProgress(KILLS_KEY, 10);
            case HUNTER_50 -> boundedProgress(KILLS_KEY, 50);
            case HUNTER_250 -> boundedProgress(KILLS_KEY, 250);
            case OXYGEN_25 -> boundedProgress(OXYGEN_KEY, 25);
            case POWER_UPS_25 -> boundedProgress(POWER_UP_KEY, 25);
            default -> isUnlocked(achievement) ? "UNLOCKED" : "LOCKED";
        };
    }

    private String boundedProgress(String key, int target) {
        return Math.min(target, preferences.getInteger(key, 0)) + " / " + target;
    }

    private void unlockAt(List<Achievement> unlocked, Achievement achievement,
                          float current, float target) {
        if (current >= target) {
            unlock(unlocked, achievement);
        }
    }

    private void unlock(List<Achievement> unlocked, Achievement achievement) {
        if (isUnlocked(achievement)) {
            return;
        }
        preferences.putBoolean(unlockKey(achievement), true);
        unlocked.add(achievement);
    }

    private void flushIfNeeded(List<Achievement> unlocked) {
        if (!unlocked.isEmpty()) {
            preferences.flush();
        }
    }

    private static List<Achievement> immutable(List<Achievement> achievements) {
        return achievements.isEmpty()
            ? Collections.emptyList()
            : Collections.unmodifiableList(achievements);
    }

    private static String unlockKey(Achievement achievement) {
        return UNLOCKED_PREFIX + achievement.name();
    }
}
