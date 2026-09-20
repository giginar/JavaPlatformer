package com.game.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementStoreTest {
    @Test
    void catalogContainsExactlyFortyAchievements() {
        assertEquals(40, Achievement.values().length);
    }

    @Test
    void combatEventsUnlockAndPersistCareerAchievements() {
        MemoryPreferences preferences = new MemoryPreferences();
        AchievementStore store = new AchievementStore(preferences);

        List<Achievement> firstKill = store.recordKill(1, false);
        assertTrue(firstKill.contains(Achievement.FIRST_BLOOD));

        for (int i = 0; i < 8; i++) {
            store.recordKill(1, false);
        }
        List<Achievement> tenthKill = store.recordKill(3, false);

        assertTrue(tenthKill.contains(Achievement.HUNTER_10));
        assertTrue(tenthKill.contains(Achievement.COMBO_3));
        assertTrue(new AchievementStore(preferences).isUnlocked(Achievement.HUNTER_10));
        assertEquals("10 / 10", store.progressText(Achievement.HUNTER_10));
    }

    @Test
    void stageAndVictoryRulesRecognizeTotalLockdown() {
        AchievementStore store = new AchievementStore(new MemoryPreferences());
        RunSettings settings = new RunSettings(RunDifficulty.HARD,
            EnumSet.allOf(ChallengeModifier.class));

        List<Achievement> stage = store.recordStageComplete(1, settings,
            true, true, true, true);
        assertTrue(stage.contains(Achievement.REEF_CLEARED));
        assertTrue(stage.contains(Achievement.CEASEFIRE_STAGE));
        assertTrue(stage.contains(Achievement.EMPTY_HANDED_STAGE));
        assertTrue(stage.contains(Achievement.PURE_SKILL_STAGE));
        assertTrue(stage.contains(Achievement.FLAWLESS_STAGE));
        assertTrue(stage.contains(Achievement.CHALLENGER));
        assertTrue(stage.contains(Achievement.LOCKDOWN_STAGE));

        List<Achievement> victory = store.recordVictory(settings,
            true, true, true, true);
        assertTrue(victory.contains(Achievement.ABYSS_CONQUERED));
        assertTrue(victory.contains(Achievement.HARD_COMPLETE));
        assertTrue(victory.contains(Achievement.TOTAL_LOCKDOWN));
        assertTrue(victory.contains(Achievement.FLAWLESS_RUN));
    }

    @Test
    void distanceAndScoreChallengesUnlockFromLiveRunProgress() {
        AchievementStore store = new AchievementStore(new MemoryPreferences());

        List<Achievement> unlocked = store.recordRunProgress(1_000f, 50_000f,
            true, true);

        assertTrue(unlocked.contains(Achievement.DISTANCE_100));
        assertTrue(unlocked.contains(Achievement.DISTANCE_500));
        assertTrue(unlocked.contains(Achievement.DISTANCE_1000));
        assertTrue(unlocked.contains(Achievement.SCORE_10K));
        assertTrue(unlocked.contains(Achievement.SCORE_50K));
        assertTrue(unlocked.contains(Achievement.SILENT_100));
        assertTrue(unlocked.contains(Achievement.MINIMALIST_100));
    }

    @Test
    void repeatedCompletionEventsDoNotDuplicateAchievementUnlocks() {
        AchievementStore store = new AchievementStore(new MemoryPreferences());
        RunSettings settings = RunSettings.standard();

        store.recordStageComplete(1, settings, false, false, false, false);
        store.recordVictory(settings, false, false, false, false);

        assertTrue(store.recordStageComplete(1, settings,
            false, false, false, false).isEmpty());
        assertTrue(store.recordVictory(settings,
            false, false, false, false).isEmpty());
    }

    @Test
    void everyCatalogAchievementHasADeterministicReachableTrigger() {
        AchievementStore store = new AchievementStore(new MemoryPreferences());
        for (int kill = 1; kill <= 250; kill++) {
            store.recordKill(kill == 250 ? 10 : 1, kill == 250);
        }
        for (int pickup = 0; pickup < 25; pickup++) {
            store.recordOxygenPickup();
            store.recordPowerUpPickup();
        }
        store.recordRunProgress(1_000f, 100_000f, true, true);

        RunSettings lockdown = new RunSettings(RunDifficulty.HARD,
            EnumSet.allOf(ChallengeModifier.class));
        for (int stage = 1; stage <= 5; stage++) {
            store.recordStageComplete(stage, lockdown, true, true, true, true);
        }
        store.recordVictory(new RunSettings(RunDifficulty.EASY, EnumSet.noneOf(ChallengeModifier.class)),
            true, true, true, true);
        store.recordVictory(RunSettings.standard(), true, true, true, true);
        store.recordVictory(lockdown, true, true, true, true);

        assertEquals(Achievement.values().length, store.unlockedCount());
    }

    @Test
    void careerCountersSaturateInsteadOfWrappingNegative() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("achievement.stats.kills", Integer.MAX_VALUE);
        preferences.putInteger("achievement.stats.oxygenPickups", Integer.MAX_VALUE);
        preferences.putInteger("achievement.stats.powerUps", Integer.MAX_VALUE);
        AchievementStore store = new AchievementStore(preferences);

        store.recordKill(1, false);
        store.recordOxygenPickup();
        store.recordPowerUpPickup();

        assertEquals(Integer.MAX_VALUE, preferences.getInteger("achievement.stats.kills"));
        assertEquals(Integer.MAX_VALUE, preferences.getInteger("achievement.stats.oxygenPickups"));
        assertEquals(Integer.MAX_VALUE, preferences.getInteger("achievement.stats.powerUps"));
    }
}
