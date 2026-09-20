package com.game.model;

import com.game.GameConfig;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {
    @Test
    void survivalProgressIsFrameRateIndependent() {
        GameSession oneLargeFrame = new GameSession();
        GameSession tenSmallFrames = new GameSession();

        oneLargeFrame.update(1f);
        for (int i = 0; i < 10; i++) {
            tenSmallFrames.update(0.1f);
        }

        assertEquals(oneLargeFrame.getScore(), tenSmallFrames.getScore(), 0.001f);
        assertEquals(oneLargeFrame.getOxygen(), tenSmallFrames.getOxygen(), 0.001f);
        assertEquals(60f, oneLargeFrame.getScore(), 0.001f);
        assertEquals(95f, oneLargeFrame.getOxygen(), 0.001f);
    }

    @Test
    void oxygenIsClampedAndDamageCanEndTheRun() {
        GameSession session = new GameSession();

        session.takeDamage(80f);
        session.collectOxygen(200f);
        assertEquals(GameSession.MAX_OXYGEN, session.getOxygen(), 0.001f);

        session.takeDamage(150f);
        assertEquals(0f, session.getOxygen(), 0.001f);
        assertTrue(session.isOutOfOxygen());
    }

    @Test
    void comboBuildsOnKillsAndExpiresAfterWindow() {
        GameSession session = new GameSession();
        session.takeDamage(50f);

        assertEquals(100, session.awardKill(100));
        assertEquals(125, session.awardKill(100));
        assertEquals(2, session.getCombo());
        assertEquals(1.25f, session.getComboMultiplier(), 0.001f);
        assertFalse(session.isOutOfOxygen());

        session.update(GameSession.COMBO_WINDOW + 0.01f);
        assertEquals(0, session.getCombo());
        assertEquals(1f, session.getComboMultiplier(), 0.001f);
    }

    @Test
    void takingDamageBreaksAnActiveCombo() {
        GameSession session = new GameSession();
        session.awardKill(100);
        session.awardKill(100);

        session.takeDamage(10f);

        assertEquals(0, session.getCombo());
    }

    @Test
    void permanentTankCapacityIsUsedForResetAndRatio() {
        GameSession session = new GameSession(130f);

        assertEquals(130f, session.getOxygen(), 0.001f);
        assertEquals(130f, session.getMaxOxygen(), 0.001f);
        assertEquals(1f, session.getOxygenRatio(), 0.001f);
    }

    @Test
    void oxygenEfficiencyUpgradeReducesDrainAndResetClearsIt() {
        GameSession session = new GameSession();
        session.applyUpgrade(UpgradeType.OXYGEN_EFFICIENCY);
        session.applyUpgrade(UpgradeType.OXYGEN_EFFICIENCY);
        session.applyUpgrade(UpgradeType.OXYGEN_EFFICIENCY);

        session.update(1f);

        assertEquals(97.25f, session.getOxygen(), 0.001f);
        assertEquals(3, session.getUpgradeLevel(UpgradeType.OXYGEN_EFFICIENCY));

        session.reset();

        assertEquals(0, session.getUpgradeLevel(UpgradeType.OXYGEN_EFFICIENCY));
        assertEquals(GameSession.MAX_OXYGEN, session.getOxygen(), 0.001f);
    }

    @Test
    void difficultyScalesOxygenDrainAndRunUpgradeEffects() {
        GameSession session = new GameSession(100f, 0.75f, 1.12f);
        session.applyUpgrade(UpgradeType.OXYGEN_EFFICIENCY);
        session.applyUpgrade(UpgradeType.AGILE_DIVER);
        session.applyUpgrade(UpgradeType.RAPID_FIRE);

        session.update(1f);

        assertEquals(95.03f, session.getOxygen(), 0.001f);
        assertEquals(1.09f, session.getAgilityMultiplier(), 0.001f);
        assertEquals(0.865f, session.getShootCooldownMultiplier(), 0.001f);
    }

    @Test
    void noOxygenPickupChallengeSurvivesFullRouteAndUnarmedFinaleOnEveryDifficulty() {
        float requiredSeconds = GameConfig.DEFAULT_STAGE_DURATION_SECONDS
            * GameBalance.stageCount() + GameConfig.RUN_TRANSITION_SAFETY_SECONDS
            + GameConfig.UNARMED_BOSS_SURVIVAL_SECONDS;

        for (RunDifficulty difficulty : RunDifficulty.values()) {
            RunSettings settings = new RunSettings(difficulty,
                EnumSet.of(ChallengeModifier.NO_WEAPON,
                    ChallengeModifier.NO_OXYGEN_PICKUPS,
                    ChallengeModifier.NO_UPGRADES));
            GameSession session = new GameSession(GameSession.MAX_OXYGEN,
                difficulty.upgradeEffectMultiplier(), settings.oxygenDrainMultiplier());

            session.update(requiredSeconds);

            assertFalse(session.isOutOfOxygen(), difficulty.name());
        }
    }

    @Test
    void sessionChoicesExcludeEffectsThatHaveReachedTheirDerivedCap() {
        GameSession normal = new GameSession();
        for (int level = 0; level < 3; level++) {
            assertTrue(normal.applyUpgrade(UpgradeType.RAPID_FIRE));
        }
        for (int level = 0; level < 7; level++) {
            assertTrue(normal.applyUpgrade(UpgradeType.LARGE_TANKS));
        }

        assertFalse(normal.getAvailableUpgrades().contains(UpgradeType.RAPID_FIRE));
        assertFalse(normal.getAvailableUpgrades().contains(UpgradeType.LARGE_TANKS));
        assertFalse(normal.applyUpgrade(UpgradeType.RAPID_FIRE));
        assertFalse(normal.applyUpgrade(UpgradeType.LARGE_TANKS));

        GameSession hardWithMaximumTank = new GameSession(155f, 0.75f, 1.12f);
        for (int level = 0; level < 4; level++) {
            assertTrue(hardWithMaximumTank.applyUpgrade(UpgradeType.RAPID_FIRE));
        }
        for (int level = 0; level < 17; level++) {
            assertTrue(hardWithMaximumTank.applyUpgrade(UpgradeType.LARGE_TANKS));
        }

        assertFalse(hardWithMaximumTank.getAvailableUpgrades().contains(UpgradeType.RAPID_FIRE));
        assertFalse(hardWithMaximumTank.getAvailableUpgrades().contains(UpgradeType.LARGE_TANKS));
    }

    @Test
    void everyPiercingSelectionAddsOneTargetOnEveryDifficulty() {
        for (RunDifficulty difficulty : RunDifficulty.values()) {
            GameSession session = new GameSession(100f,
                difficulty.upgradeEffectMultiplier(), difficulty.oxygenDrainMultiplier());

            for (int level = 1; level <= 8; level++) {
                assertTrue(session.applyUpgrade(UpgradeType.PIERCING_HARPOON));
                assertEquals(1 + level, session.getHarpoonHitCount(), difficulty.name());
            }
        }
    }
}
