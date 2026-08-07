package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.manager.GameAssets;

import java.util.ArrayList;
import java.util.List;

public class PiranhaSwarm implements EnemyFish {
    private static final float SPEED = 205f;
    private static final float WIDTH = 36f;
    private static final float HEIGHT = 27f;
    private static final int MAX_HP = 3;

    private final Texture texture;
    private final Vector2 center;
    private final List<Vector2> positions;
    private final Rectangle bounds;

    private State state = State.APPROACH;
    private float stateTimer;
    private float speedMultiplier = 1f;
    private float hitEffectTimer;
    private float animationTime;
    private int hp = MAX_HP;

    public PiranhaSwarm(float baseY) {
        texture = GameAssets.texture(GameAssets.PIRANHA);
        center = new Vector2(GameConfig.WORLD_WIDTH + 25f, baseY);
        positions = new ArrayList<>();
        int count = 3 + MathUtils.random(2);
        for (int i = 0; i < count; i++) {
            positions.add(new Vector2());
        }
        bounds = new Rectangle();
        updateFormation();
        updateBounds();
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        hitEffectTimer = Math.max(0f, hitEffectTimer - delta);

        switch (state) {
            case APPROACH -> {
                center.x -= SPEED * speedMultiplier * delta;
                center.y = MathUtils.lerp(center.y, targetY, Math.min(1f, delta * 1.8f));
                if (center.x <= targetX + 310f) {
                    state = State.FORMATION;
                    stateTimer = 1.35f;
                }
            }
            case FORMATION -> {
                stateTimer -= delta;
                center.x = MathUtils.lerp(center.x, targetX + 185f, Math.min(1f, delta * 3f));
                center.y = MathUtils.lerp(center.y, targetY, Math.min(1f, delta * 3.4f));
                if (stateTimer <= 0f) {
                    state = State.CHARGE;
                }
            }
            case CHARGE -> {
                center.x -= SPEED * 1.85f * speedMultiplier * delta;
                center.y += MathUtils.sin(animationTime * 9f) * 22f * delta;
            }
        }

        updateFormation();
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitEffectTimer > 0f) {
            batch.setColor(Color.RED);
        } else if (state == State.FORMATION) {
            batch.setColor(1f, 0.55f + MathUtils.sin(animationTime * 18f) * 0.25f, 0.2f, 1f);
        }

        for (int i = 0; i < positions.size(); i++) {
            Vector2 position = positions.get(i);
            float pulse = 1f + MathUtils.sin(animationTime * 9f + i) * 0.08f;
            batch.draw(texture, position.x, position.y, WIDTH * pulse, HEIGHT / pulse);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public boolean isOutOfScreen() {
        return bounds.x + bounds.width < 0f;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void hit() {
        hp--;
        hitEffectTimer = 0.2f;
        if (positions.size() > 1) {
            positions.remove(positions.size() - 1);
            updateFormation();
            updateBounds();
        }
    }

    @Override
    public boolean isDead() {
        return hp <= 0;
    }

    @Override
    public void setSpeedMultiplier(float multiplier) {
        speedMultiplier = multiplier;
    }

    @Override
    public int getScoreValue() {
        return 220;
    }

    @Override
    public float getCollisionDamage() {
        return state == State.CHARGE ? 38f : 32f;
    }

    @Override
    public boolean isTelegraphing() {
        return state == State.FORMATION;
    }

    @Override
    public float getHealthRatio() {
        return Math.max(0f, hp / (float) MAX_HP);
    }

    private void updateFormation() {
        int count = positions.size();
        for (int i = 0; i < count; i++) {
            Vector2 position = positions.get(i);
            if (state == State.FORMATION) {
                float angle = animationTime * 3.4f + MathUtils.PI2 * i / count;
                position.set(center.x + MathUtils.cos(angle) * 67f - WIDTH / 2f,
                    center.y + MathUtils.sin(angle) * 43f - HEIGHT / 2f);
            } else if (state == State.CHARGE) {
                float offset = i - (count - 1) / 2f;
                position.set(center.x + Math.abs(offset) * 16f,
                    center.y + offset * 22f - HEIGHT / 2f);
            } else {
                position.set(center.x + i * 15f,
                    center.y + MathUtils.sin(animationTime * 5f + i * 1.3f) * 18f - HEIGHT / 2f);
            }
        }
    }

    private void updateBounds() {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (Vector2 position : positions) {
            minX = Math.min(minX, position.x);
            minY = Math.min(minY, position.y);
            maxX = Math.max(maxX, position.x + WIDTH);
            maxY = Math.max(maxY, position.y + HEIGHT);
        }
        bounds.set(minX, minY, maxX - minX, maxY - minY);
    }

    private enum State {
        APPROACH,
        FORMATION,
        CHARGE
    }
}
