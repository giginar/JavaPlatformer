package com.game.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleSystemTest {
    @Test
    void expiredParticlesReturnToThePool() {
        ParticleSystem particles = new ParticleSystem();
        particles.spawnOxygenBurst(100f, 100f);

        assertTrue(particles.activeCount() > 0);

        particles.update(2f);

        assertEquals(0, particles.activeCount());
    }

    @Test
    void particleCountIsCapped() {
        ParticleSystem particles = new ParticleSystem();

        for (int i = 0; i < 1_000; i++) {
            particles.spawnHarpoonTrail(i, i);
        }

        assertEquals(600, particles.activeCount());
    }
}
