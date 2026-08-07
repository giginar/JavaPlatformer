package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.manager.GameAssets;

public class Shark implements EnemyFish {
    private static final float SPEED = 105f;
    private static final float DASH_SPEED = 520f;
    private static final float WIDTH = 112f;
    private static final float HEIGHT = 56f;
    private static final int MAX_HP = 2;

    private final Sprite sprite;
    private final Vector2 position;
    private final Rectangle bounds;

    private State state = State.CRUISE;
    private float stateTimer = 1.1f;
    private float speedMultiplier = 1f;
    private float hitEffectTimer;
    private float animationTime;
    private int hp = MAX_HP;

    public Shark(float y) {
        Texture texture = GameAssets.texture(GameAssets.SHARK);
        sprite = new Sprite(texture);
        sprite.setSize(WIDTH, HEIGHT);
        sprite.setOriginCenter();
        position = new Vector2(GameConfig.WORLD_WIDTH, y);
        bounds = new Rectangle(position.x + 5f, position.y + 4f, WIDTH - 10f, HEIGHT - 8f);
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        stateTimer -= delta;
        hitEffectTimer = Math.max(0f, hitEffectTimer - delta);

        switch (state) {
            case CRUISE -> {
                position.x -= SPEED * speedMultiplier * delta;
                position.y += MathUtils.sin(animationTime * 2.8f) * 10f * delta;
                if (stateTimer <= 0f && position.x < GameConfig.WORLD_WIDTH - 80f && position.x > 420f) {
                    state = State.LOCKING;
                    stateTimer = 0.85f;
                }
            }
            case LOCKING -> {
                position.x -= SPEED * 0.18f * speedMultiplier * delta;
                position.y = MathUtils.lerp(position.y, targetY - HEIGHT / 2f,
                    Math.min(1f, delta * 4.2f));
                if (stateTimer <= 0f) {
                    state = State.DASHING;
                    stateTimer = 0.72f;
                }
            }
            case DASHING -> {
                position.x -= DASH_SPEED * speedMultiplier * delta;
                if (stateTimer <= 0f) {
                    state = State.CRUISE;
                    stateTimer = MathUtils.random(1.2f, 2f);
                }
            }
        }

        position.y = MathUtils.clamp(position.y, 8f, GameConfig.WORLD_HEIGHT - HEIGHT - 8f);
        bounds.setPosition(position.x + 5f, position.y + 4f);
    }

    @Override
    public void render(SpriteBatch batch) {
        float pulse = 1f + MathUtils.sin(animationTime * 5f) * 0.035f;
        sprite.setSize(WIDTH * pulse, HEIGHT / pulse);
        sprite.setPosition(position.x - (sprite.getWidth() - WIDTH) / 2f,
            position.y - (sprite.getHeight() - HEIGHT) / 2f);
        sprite.setRotation(MathUtils.sin(animationTime * 3f) * 2.5f);

        if (hitEffectTimer > 0f) {
            sprite.setColor(Color.RED);
        } else if (state == State.LOCKING) {
            float warning = 0.65f + MathUtils.sin(animationTime * 22f) * 0.35f;
            sprite.setColor(1f, warning * 0.42f, 0.1f, 1f);
        } else {
            sprite.setColor(Color.WHITE);
        }
        sprite.draw(batch);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public boolean isOutOfScreen() {
        return position.x + WIDTH < 0f;
    }

    @Override
    public void hit() {
        hp--;
        hitEffectTimer = 0.2f;
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
        return 300;
    }

    @Override
    public float getCollisionDamage() {
        return state == State.DASHING ? 52f : 42f;
    }

    @Override
    public boolean isTelegraphing() {
        return state == State.LOCKING;
    }

    @Override
    public float getHealthRatio() {
        return Math.max(0f, hp / (float) MAX_HP);
    }

    private enum State {
        CRUISE,
        LOCKING,
        DASHING
    }
}
