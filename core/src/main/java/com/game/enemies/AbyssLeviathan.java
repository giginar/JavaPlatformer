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

public final class AbyssLeviathan implements EnemyFish {
    private static final float WIDTH = 270f;
    private static final float HEIGHT = 135f;
    private static final int MAX_HP = 18;

    private final Sprite sprite;
    private final Vector2 position = new Vector2(GameConfig.WORLD_WIDTH + 120f, 290f);
    private final Rectangle bounds = new Rectangle();

    private State state = State.ENTERING;
    private float stateTimer = 2.4f;
    private float animationTime;
    private float hitEffectTimer;
    private int hp = MAX_HP;

    public AbyssLeviathan() {
        Texture texture = GameAssets.texture(GameAssets.SHARK);
        sprite = new Sprite(texture);
        sprite.setSize(WIDTH, HEIGHT);
        sprite.setOriginCenter();
        updateBounds();
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        hitEffectTimer = Math.max(0f, hitEffectTimer - delta);
        float rageMultiplier = hp <= MAX_HP / 2 ? 1.25f : 1f;

        switch (state) {
            case ENTERING -> {
                position.x -= 165f * delta;
                position.y += MathUtils.sin(animationTime * 2f) * 18f * delta;
                if (position.x <= 915f) {
                    position.x = 915f;
                    state = State.HUNTING;
                    stateTimer = 2.5f;
                }
            }
            case HUNTING -> {
                stateTimer -= delta;
                position.x = 915f + MathUtils.sin(animationTime * 1.5f) * 38f;
                position.y = MathUtils.lerp(position.y, targetY - HEIGHT / 2f,
                    Math.min(1f, delta * 1.25f * rageMultiplier));
                if (stateTimer <= 0f) {
                    state = State.WARNING;
                    stateTimer = 1.15f;
                }
            }
            case WARNING -> {
                stateTimer -= delta;
                position.y = MathUtils.lerp(position.y, targetY - HEIGHT / 2f,
                    Math.min(1f, delta * 4.5f));
                if (stateTimer <= 0f) {
                    state = State.RUSHING;
                }
            }
            case RUSHING -> {
                position.x -= 690f * rageMultiplier * delta;
                if (position.x + WIDTH < -40f) {
                    state = State.RECOVERING;
                }
            }
            case RECOVERING -> {
                position.x += 430f * rageMultiplier * delta;
                if (position.x >= 915f) {
                    position.x = 915f;
                    state = State.HUNTING;
                    stateTimer = hp <= MAX_HP / 2 ? 1.65f : 2.4f;
                }
            }
        }

        position.y = MathUtils.clamp(position.y, 25f, GameConfig.WORLD_HEIGHT - HEIGHT - 25f);
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch) {
        float breathe = 1f + MathUtils.sin(animationTime * 3.2f) * 0.045f;
        sprite.setSize(WIDTH * breathe, HEIGHT / breathe);
        sprite.setPosition(position.x - (sprite.getWidth() - WIDTH) / 2f,
            position.y - (sprite.getHeight() - HEIGHT) / 2f);
        sprite.setRotation(MathUtils.sin(animationTime * 2.2f) * 3f);
        sprite.setFlip(state == State.RECOVERING, false);

        if (hitEffectTimer > 0f) {
            sprite.setColor(Color.RED);
        } else if (state == State.WARNING) {
            float flash = 0.45f + MathUtils.sin(animationTime * 26f) * 0.4f;
            sprite.setColor(1f, 0.08f, flash, 1f);
        } else if (hp <= MAX_HP / 2) {
            sprite.setColor(0.82f, 0.35f, 1f, 1f);
        } else {
            sprite.setColor(0.55f, 0.65f, 1f, 1f);
        }
        sprite.draw(batch);
    }

    @Override
    public boolean isOutOfScreen() {
        return false;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void hit() {
        hp--;
        hitEffectTimer = 0.15f;
    }

    @Override
    public boolean isDead() {
        return hp <= 0;
    }

    @Override
    public void setSpeedMultiplier(float speed) {
        // Boss pacing is authored independently from the regular difficulty multiplier.
    }

    @Override
    public int getScoreValue() {
        return 2500;
    }

    @Override
    public float getCollisionDamage() {
        return state == State.RUSHING ? 62f : 48f;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public boolean isTelegraphing() {
        return state == State.WARNING;
    }

    @Override
    public boolean removeOnPlayerCollision() {
        return false;
    }

    @Override
    public float getHealthRatio() {
        return Math.max(0f, hp / (float) MAX_HP);
    }

    private void updateBounds() {
        bounds.set(position.x + 18f, position.y + 14f, WIDTH - 36f, HEIGHT - 28f);
    }

    private enum State {
        ENTERING,
        HUNTING,
        WARNING,
        RUSHING,
        RECOVERING
    }
}
