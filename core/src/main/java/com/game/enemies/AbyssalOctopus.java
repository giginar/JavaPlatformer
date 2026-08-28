package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.hazards.EnvironmentalHazard;
import com.game.hazards.TentacleStrike;
import com.game.manager.GameAssets;

import java.util.ArrayDeque;
import java.util.Queue;

/** Full-height final boss alternating ink, broodling, and lane-sweep attacks. */
public final class AbyssalOctopus implements EnemyFish {
    private static final float WIDTH = 650f;
    private static final float HEIGHT = 710f;
    private static final float REST_X = 700f;
    private static final int MAX_HP = 60;

    private final Sprite sprite;
    private final Vector2 position = new Vector2(GameConfig.WORLD_WIDTH + 30f, 5f);
    private final Rectangle bounds = new Rectangle();
    private final Queue<EnemyProjectile> projectiles = new ArrayDeque<>();
    private final Queue<EnemyFish> minions = new ArrayDeque<>();
    private final Queue<EnvironmentalHazard> hazards = new ArrayDeque<>();

    private State state = State.ENTERING;
    private float stateTimer = 1.7f;
    private float animationTime;
    private float hitEffectTimer;
    private float lockedTargetY = GameConfig.WORLD_HEIGHT / 2f;
    private int nextAttack;
    private int hp = MAX_HP;

    public AbyssalOctopus() {
        Texture texture = GameAssets.texture(GameAssets.OCTOPUS_BOSS);
        sprite = new Sprite(texture);
        sprite.setSize(WIDTH, HEIGHT);
        sprite.setOriginCenter();
        updateBounds();
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        hitEffectTimer = Math.max(0f, hitEffectTimer - delta);
        float rage = hp <= MAX_HP / 2 ? 1.28f : 1f;

        if (state == State.ENTERING) {
            position.x = Math.max(REST_X, position.x - 185f * delta);
            if (position.x <= REST_X) {
                beginRest(1.35f);
            }
            updateBounds();
            return;
        }

        stateTimer -= delta * rage;
        position.y = 5f + MathUtils.sin(animationTime * 1.25f) * 7f;
        if (state == State.RESTING && stateTimer <= 0f) {
            lockedTargetY = targetY;
            state = switch (nextAttack % 3) {
                case 0 -> State.INK_WARNING;
                case 1 -> State.BROOD_WARNING;
                default -> State.TENTACLE_WARNING;
            };
            nextAttack++;
            stateTimer = 0.9f;
        } else if (isWarningState() && stateTimer <= 0f) {
            releaseAttack(targetX);
            beginRest(hp <= MAX_HP / 2 ? 0.9f : 1.25f);
        }
        updateBounds();
    }

    private void releaseAttack(float targetX) {
        if (state == State.INK_WARNING) {
            projectiles.add(EnemyProjectile.ink(1000f, 230f, targetX, lockedTargetY - 95f));
            projectiles.add(EnemyProjectile.ink(1010f, 360f, targetX, lockedTargetY));
            projectiles.add(EnemyProjectile.ink(1000f, 490f, targetX, lockedTargetY + 95f));
        } else if (state == State.BROOD_WARNING) {
            minions.add(new OctopusMinion(lockedTargetY - 155f));
            minions.add(new OctopusMinion(lockedTargetY));
            minions.add(new OctopusMinion(lockedTargetY + 155f));
        } else if (state == State.TENTACLE_WARNING) {
            hazards.add(new TentacleStrike(lockedTargetY));
        }
    }

    private void beginRest(float duration) {
        state = State.RESTING;
        stateTimer = duration;
    }

    @Override
    public void render(SpriteBatch batch) {
        float breathe = 1f + MathUtils.sin(animationTime * 2.8f) * 0.018f;
        sprite.setSize(WIDTH * breathe, HEIGHT / breathe);
        sprite.setPosition(position.x - (sprite.getWidth() - WIDTH) / 2f,
            position.y - (sprite.getHeight() - HEIGHT) / 2f);
        sprite.setRotation(MathUtils.sin(animationTime * 1.6f) * 0.8f);
        if (hitEffectTimer > 0f) {
            sprite.setColor(Color.RED);
        } else if (isWarningState()) {
            float flash = 0.55f + MathUtils.sin(animationTime * 24f) * 0.4f;
            sprite.setColor(1f, flash * 0.35f, 0.82f, 1f);
        } else if (hp <= MAX_HP / 2) {
            sprite.setColor(0.9f, 0.58f, 1f, 1f);
        } else {
            sprite.setColor(Color.WHITE);
        }
        sprite.draw(batch);
    }

    @Override
    public EnemyProjectile pollProjectile(float targetX, float targetY) {
        return projectiles.poll();
    }

    @Override
    public EnemyFish pollSpawnedEnemy() {
        return minions.poll();
    }

    @Override
    public EnvironmentalHazard pollHazard() {
        return hazards.poll();
    }

    private boolean isWarningState() {
        return state == State.INK_WARNING || state == State.BROOD_WARNING
            || state == State.TENTACLE_WARNING;
    }

    private void updateBounds() {
        bounds.set(position.x + 225f, position.y + 55f, 380f, HEIGHT - 110f);
    }

    @Override public boolean isOutOfScreen() { return false; }
    @Override public Rectangle getBounds() { return bounds; }
    @Override public void hit() { hp--; hitEffectTimer = 0.13f; }
    @Override public boolean isDead() { return hp <= 0; }
    @Override public void setSpeedMultiplier(float speed) { }
    @Override public int getScoreValue() { return 5000; }
    @Override public float getCollisionDamage() { return 100f; }
    @Override public boolean isBoss() { return true; }
    @Override public boolean isTelegraphing() { return isWarningState(); }
    @Override public boolean removeOnPlayerCollision() { return false; }
    @Override public float getHealthRatio() { return Math.max(0f, hp / (float) MAX_HP); }

    private enum State {
        ENTERING,
        RESTING,
        INK_WARNING,
        BROOD_WARNING,
        TENTACLE_WARNING
    }
}
