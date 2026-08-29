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
}
