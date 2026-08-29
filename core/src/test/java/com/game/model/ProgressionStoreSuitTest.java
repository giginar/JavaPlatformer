package com.game.model;

import com.game.GameConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionStoreSuitTest {
    @Test
    void blueSuitIsTheUnlockedDefault() {
        ProgressionStore store = new ProgressionStore(new MemoryPreferences());

        assertTrue(store.isSuitUnlocked(DiverSuit.TIDELINE_BLUE));
        assertEquals(DiverSuit.TIDELINE_BLUE, store.selectedSuit());
        assertFalse(store.isSuitUnlocked(DiverSuit.RESCUE_RED));
    }

    @Test
    void purchasingSuitDeductsPearlsAndEquipsItPersistently() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("progression.pearls", 50);
        ProgressionStore store = new ProgressionStore(preferences);

        assertTrue(store.purchaseSuit(DiverSuit.RESCUE_RED));
        assertEquals(50 - GameConfig.RESCUE_RED_SUIT_COST, store.pearls());
        assertTrue(store.isSuitUnlocked(DiverSuit.RESCUE_RED));
        assertEquals(DiverSuit.RESCUE_RED, new ProgressionStore(preferences).selectedSuit());
        assertFalse(store.purchaseSuit(DiverSuit.RESCUE_RED));
    }

    @Test
    void lockedOrUnaffordableSuitCannotBeEquipped() {
        ProgressionStore store = new ProgressionStore(new MemoryPreferences());

        assertFalse(store.selectSuit(DiverSuit.ABYSS_BLACK));
        assertFalse(store.purchaseSuit(DiverSuit.ABYSS_BLACK));
        assertEquals(DiverSuit.TIDELINE_BLUE, store.selectedSuit());
    }

    @Test
    void everySuitHasOneDistinctSpecialization() {
        assertEquals(GameConfig.TIDELINE_BLUE_RELOAD_MULTIPLIER,
            DiverSuit.TIDELINE_BLUE.reloadMultiplier(), 0.0001f);
        assertEquals(GameConfig.SALVAGE_GREEN_MAGNET_BONUS,
            DiverSuit.SALVAGE_GREEN.magnetBonusRange(), 0.0001f);
        assertEquals(GameConfig.RESCUE_RED_OXYGEN_BONUS,
            DiverSuit.RESCUE_RED.oxygenBonus(), 0.0001f);
        assertEquals(GameConfig.ABYSS_BLACK_AGILITY_MULTIPLIER,
            DiverSuit.ABYSS_BLACK.agilityMultiplier(), 0.0001f);
    }

}
