package com.game.model;

import com.game.settings.DisplaySettingsStore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PersistenceIdentifierContractTest {
    @Test
    void persistedEnumNamesRemainStable() {
        assertNames(Achievement.values(),
            "FIRST_BLOOD", "HUNTER_10", "HUNTER_50", "HUNTER_250",
            "COMBO_3", "COMBO_5", "COMBO_10", "SCORE_10K", "SCORE_50K",
            "SCORE_100K", "DISTANCE_100", "DISTANCE_500", "DISTANCE_1000",
            "FIRST_OXYGEN", "OXYGEN_25", "FIRST_POWER_UP", "POWER_UPS_25",
            "REEF_CLEARED", "RUINS_CLEARED", "MAZE_CLEARED", "TRENCH_CLEARED",
            "RIFT_CLEARED", "ABYSS_CONQUERED", "BOSS_SLAYER", "FLAWLESS_STAGE",
            "FLAWLESS_RUN", "CEASEFIRE_STAGE", "EMPTY_HANDED_STAGE",
            "PURE_SKILL_STAGE", "SILENT_100", "MINIMALIST_100", "CEASEFIRE_RUN",
            "EMPTY_HANDED_RUN", "PURE_SKILL_RUN", "EASY_COMPLETE",
            "NORMAL_COMPLETE", "HARD_COMPLETE", "CHALLENGER", "LOCKDOWN_STAGE",
            "TOTAL_LOCKDOWN");
        assertNames(ChallengeModifier.values(),
            "NO_WEAPON", "NO_OXYGEN_PICKUPS", "NO_POWER_UPS", "NO_UPGRADES");
        assertNames(DiverSuit.values(),
            "TIDELINE_BLUE", "SALVAGE_GREEN", "RESCUE_RED", "ABYSS_BLACK");
        assertNames(PermanentUpgrade.values(),
            "PRESSURE_TANK", "REINFORCED_SUIT", "MAGNETIC_CLASP", "TWIN_LAUNCHER",
            "SALVAGE_MAP");
        assertNames(RunDifficulty.values(), "EASY", "NORMAL", "HARD");
        assertNames(DisplaySettingsStore.WindowMode.values(),
            "WINDOWED", "BORDERLESS", "FULLSCREEN");
        assertNames(DisplaySettingsStore.TextScale.values(), "DEFAULT", "LARGE");
    }

    private static void assertNames(Enum<?>[] values, String... expected) {
        assertArrayEquals(expected,
            Arrays.stream(values).map(Enum::name).toArray(String[]::new));
    }
}
