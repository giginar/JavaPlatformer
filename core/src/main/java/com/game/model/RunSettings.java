package com.game.model;

import com.game.GameConfig;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record RunSettings(RunDifficulty difficulty, Set<ChallengeModifier> modifiers) {
    public RunSettings {
        difficulty = difficulty == null ? RunDifficulty.NORMAL : difficulty;
        modifiers = modifiers == null || modifiers.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(modifiers));
    }

    public static RunSettings standard() {
        return new RunSettings(RunDifficulty.NORMAL, Collections.emptySet());
    }

    public boolean has(ChallengeModifier modifier) {
        return modifiers.contains(modifier);
    }

    public boolean isChallengeRun() {
        return !modifiers.isEmpty();
    }

    public boolean hasAllChallenges() {
        return modifiers.size() == ChallengeModifier.values().length;
    }

    public float rewardMultiplier() {
        return difficulty.rewardMultiplier()
            + modifiers.size() * GameConfig.CHALLENGE_REWARD_BONUS_PER_MODIFIER;
    }

    public float oxygenDrainMultiplier() {
        float challengeMultiplier = has(ChallengeModifier.NO_OXYGEN_PICKUPS)
            ? GameConfig.NO_OXYGEN_PICKUPS_DRAIN_MULTIPLIER : 1f;
        return difficulty.oxygenDrainMultiplier() * challengeMultiplier;
    }

    public boolean allowsWeapon() {
        return !has(ChallengeModifier.NO_WEAPON);
    }

    public boolean allowsOxygenPickups() {
        return !has(ChallengeModifier.NO_OXYGEN_PICKUPS);
    }

    public boolean allowsPowerUpPickups() {
        return !has(ChallengeModifier.NO_POWER_UPS);
    }

    public boolean loadoutBonusesEnabled() {
        return !has(ChallengeModifier.NO_UPGRADES);
    }

    public float suitAgilityMultiplier(DiverSuit suit) {
        return loadoutBonusesEnabled() && suit != null ? suit.agilityMultiplier() : 1f;
    }

    /** Excludes choices whose mechanic is disabled by the active challenge set. */
    public boolean isRunUpgradeUseful(UpgradeType type) {
        if (type == null || !loadoutBonusesEnabled()) {
            return false;
        }
        if (!allowsWeapon()
            && (type == UpgradeType.RAPID_FIRE || type == UpgradeType.PIERCING_HARPOON)) {
            return false;
        }
        return allowsOxygenPickups() || type != UpgradeType.LARGE_TANKS;
    }
}
