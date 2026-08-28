package com.game.model;

import org.junit.jupiter.api.Test;

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
}
