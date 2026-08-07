package com.game.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ParticleSystem {
    private static final int MAX_PARTICLES = 600;
    private static final Color CYAN = new Color(0.2f, 0.9f, 1f, 1f);
    private static final Color ORANGE = new Color(1f, 0.55f, 0.08f, 1f);
    private static final Color RED = new Color(1f, 0.16f, 0.12f, 1f);

    private final List<Particle> active = new ArrayList<>();
    private final ArrayDeque<Particle> pool = new ArrayDeque<>();

    public void update(float delta) {
        for (Iterator<Particle> iterator = active.iterator(); iterator.hasNext(); ) {
            Particle particle = iterator.next();
            particle.life -= delta;
            if (particle.life <= 0f) {
                iterator.remove();
                pool.offerFirst(particle);
                continue;
            }

            particle.velocityY += particle.gravity * delta;
            particle.x += particle.velocityX * delta;
            particle.y += particle.velocityY * delta;
        }
    }

    public void render(ShapeRenderer renderer) {
        for (Particle particle : active) {
            float progress = particle.life / particle.maxLife;
            float radius = Math.max(0.5f, particle.size * (particle.shrink ? progress : 1f));
            renderer.setColor(particle.color.r, particle.color.g, particle.color.b,
                particle.color.a * Math.min(1f, progress * 2f));
            renderer.circle(particle.x, particle.y, radius, 10);
        }
    }

    public void spawnSwimBubbles(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            spawn(x + MathUtils.random(-5f, 5f), y + MathUtils.random(-5f, 5f),
                MathUtils.random(-24f, -8f), MathUtils.random(28f, 65f),
                MathUtils.random(0.55f, 1.05f), MathUtils.random(2f, 4.5f),
                CYAN, 12f, false);
        }
    }

    public void spawnHarpoonTrail(float x, float y) {
        spawn(x, y, MathUtils.random(-35f, -15f), MathUtils.random(-5f, 5f),
            0.22f, MathUtils.random(1.2f, 2.3f), CYAN, 0f, true);
    }

    public void spawnImpact(float x, float y, boolean heavy) {
        int count = heavy ? 18 : 9;
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(70f, heavy ? 230f : 150f);
            spawn(x, y, MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
                MathUtils.random(0.2f, 0.48f), MathUtils.random(1.8f, heavy ? 5.2f : 3.7f),
                i % 3 == 0 ? ORANGE : RED, -90f, true);
        }
    }

    public void spawnExplosion(float x, float y, float scale) {
        int count = MathUtils.clamp(Math.round(22f * scale), 16, 80);
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(55f, 210f) * scale;
            Color color = i % 4 == 0 ? CYAN : (i % 2 == 0 ? ORANGE : RED);
            spawn(x, y, MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
                MathUtils.random(0.35f, 0.9f), MathUtils.random(2.5f, 6.5f) * scale,
                color, -65f, true);
        }
    }

    public void spawnOxygenBurst(float x, float y) {
        for (int i = 0; i < 24; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(35f, 115f);
            spawn(x, y, MathUtils.cos(angle) * speed, Math.abs(MathUtils.sin(angle) * speed) + 25f,
                MathUtils.random(0.6f, 1.25f), MathUtils.random(2.2f, 5.5f),
                CYAN, 15f, false);
        }
    }

    public void clear() {
        pool.addAll(active);
        active.clear();
    }

    public int activeCount() {
        return active.size();
    }

    private void spawn(float x, float y, float velocityX, float velocityY, float life,
                       float size, Color color, float gravity, boolean shrink) {
        if (active.size() >= MAX_PARTICLES) {
            return;
        }
        Particle particle = pool.pollFirst();
        if (particle == null) {
            particle = new Particle();
        }
        particle.reset(x, y, velocityX, velocityY, life, size, color, gravity, shrink);
        active.add(particle);
    }

    private static final class Particle {
        private final Color color = new Color();
        private float x;
        private float y;
        private float velocityX;
        private float velocityY;
        private float life;
        private float maxLife;
        private float size;
        private float gravity;
        private boolean shrink;

        private void reset(float x, float y, float velocityX, float velocityY, float life,
                           float size, Color color, float gravity, boolean shrink) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
            this.maxLife = life;
            this.size = size;
            this.color.set(color);
            this.gravity = gravity;
            this.shrink = shrink;
        }
    }
}
