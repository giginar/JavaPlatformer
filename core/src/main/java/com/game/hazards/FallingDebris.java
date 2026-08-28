package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

/** A randomly styled piece of litter that drifts down through the water. */
public final class FallingDebris implements EnvironmentalHazard {
    private static final float SIZE = 54f;
    private static final float MIN_SWAY_AMPLITUDE = 34f;
    private static final float MAX_SWAY_AMPLITUDE = 58f;
    private static final float MIN_SWAY_ANGULAR_SPEED = 2.5f;
    private static final float MAX_SWAY_ANGULAR_SPEED = 3.4f;

    private final Rectangle bounds;
    private final float scrollSpeed;
    private final float swayAmplitude;
    private final float swayAngularSpeed;
    private final float swayDirection;
    private final DebrisType type;
    private final int colorVariant;
    private final float rotationSpeed;
    private float swayCenterX;
    private float swayTimer;
    private float rotation;
    private float warningTimer = 0.9f;
    private float fallVelocity;

    public FallingDebris(float x, float scrollSpeed) {
        this(x, scrollSpeed,
            DebrisType.random(), MathUtils.random(2),
            MathUtils.random(MIN_SWAY_AMPLITUDE, MAX_SWAY_AMPLITUDE),
            MathUtils.random(MIN_SWAY_ANGULAR_SPEED, MAX_SWAY_ANGULAR_SPEED),
            MathUtils.randomBoolean() ? 1f : -1f,
            MathUtils.random(22f, 65f) * (MathUtils.randomBoolean() ? 1f : -1f),
            MathUtils.random(360f));
    }

    FallingDebris(float x, float scrollSpeed, float swayAmplitude,
                  float swayAngularSpeed, float swayDirection) {
        this(x, scrollSpeed, DebrisType.TIRE, 0, swayAmplitude,
            swayAngularSpeed, swayDirection, 0f, 0f);
    }

    private FallingDebris(float x, float scrollSpeed, DebrisType type, int colorVariant,
                          float swayAmplitude, float swayAngularSpeed, float swayDirection,
                          float rotationSpeed, float rotation) {
        bounds = new Rectangle(x, GameConfig.WORLD_HEIGHT + 8f, SIZE, SIZE);
        this.scrollSpeed = scrollSpeed;
        this.type = type;
        this.colorVariant = colorVariant;
        this.swayAmplitude = swayAmplitude;
        this.swayAngularSpeed = swayAngularSpeed;
        this.swayDirection = swayDirection;
        this.rotationSpeed = rotationSpeed;
        this.rotation = rotation;
        swayCenterX = x;
    }

    @Override
    public void update(float delta) {
        swayCenterX -= scrollSpeed * delta;
        if (warningTimer > 0f) {
            bounds.x = swayCenterX;
            warningTimer = Math.max(0f, warningTimer - delta);
            return;
        }
        fallVelocity = Math.min(GameConfig.FALLING_DEBRIS_MAX_SPEED,
            fallVelocity + GameConfig.FALLING_DEBRIS_ACCELERATION * delta);
        bounds.y -= fallVelocity * delta;
        swayTimer += delta;
        rotation = (rotation + rotationSpeed * delta) % 360f;
        bounds.x = swayCenterX + swayDirection * swayAmplitude
            * MathUtils.sin(swayTimer * swayAngularSpeed);
    }

    @Override
    public void render(ShapeRenderer renderer) {
        if (warningTimer > 0f) {
            float alpha = 0.25f + MathUtils.sin(warningTimer * 24f) * 0.15f;
            renderer.setColor(1f, 0.55f, 0.12f, alpha);
            renderer.rect(bounds.x + SIZE / 2f - 3f, 0f, 6f, GameConfig.WORLD_HEIGHT);
            return;
        }

        switch (type) {
            case TIRE -> renderTire(renderer);
            case BOTTLE -> renderBottle(renderer);
            case PAPER -> renderPaper(renderer);
        }
    }

    private void renderTire(ShapeRenderer renderer) {
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y + SIZE / 2f;
        renderer.setColor(0.055f, 0.065f, 0.075f, 1f);
        renderer.circle(cx, cy, 26f, 18);
        renderer.setColor(0.18f, 0.19f, 0.2f, 1f);
        renderer.circle(cx, cy, 20f, 18);
        renderer.setColor(0.015f, 0.055f, 0.08f, 1f);
        renderer.circle(cx, cy, 11f, 16);

        float markerAngle = rotation * MathUtils.degreesToRadians;
        renderer.setColor(0.38f, 0.39f, 0.4f, 0.85f);
        renderer.circle(cx + MathUtils.cos(markerAngle) * 22f,
            cy + MathUtils.sin(markerAngle) * 22f, 3f, 8);
    }

    private void renderBottle(ShapeRenderer renderer) {
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y + SIZE / 2f;
        setBottleColor(renderer);
        drawLocalRect(renderer, cx, cy, 0f, -8f, 18f, 28f, rotation);
        drawLocalTriangle(renderer, cx, cy,
            -9f, 6f, 9f, 6f, 5f, 12f, rotation);
        drawLocalTriangle(renderer, cx, cy,
            -9f, 6f, 5f, 12f, -5f, 12f, rotation);
        drawLocalRect(renderer, cx, cy, 0f, 16f, 10f, 9f, rotation);

        renderer.setColor(0.12f, 0.17f, 0.18f, 1f);
        drawLocalRect(renderer, cx, cy, 0f, 22f, 12f, 3f, rotation);
        renderer.setColor(0.88f, 0.78f, 0.48f, 0.9f);
        drawLocalRect(renderer, cx, cy, 0f, -9f, 16f, 9f, rotation);
        renderer.setColor(0.85f, 1f, 0.95f, 0.42f);
        drawLocalRect(renderer, cx, cy, -5f, -8f, 2f, 20f, rotation);
    }

    private void renderPaper(ShapeRenderer renderer) {
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y + SIZE / 2f;
        setPaperColor(renderer);
        drawLocalTriangle(renderer, cx, cy,
            -22f, -13f, 20f, -15f, 22f, 12f, rotation);
        drawLocalTriangle(renderer, cx, cy,
            -22f, -13f, 22f, 12f, -19f, 15f, rotation);

        renderer.setColor(0.28f, 0.32f, 0.31f, 0.7f);
        drawLocalRect(renderer, cx, cy, -3f, 4f, 28f, 2f, rotation);
        drawLocalRect(renderer, cx, cy, 1f, -3f, 32f, 2f, rotation);
        renderer.setColor(0.55f, 0.42f, 0.2f, 0.75f);
        drawLocalTriangle(renderer, cx, cy,
            22f, 12f, 11f, 12.5f, 21f, 3f, rotation);
    }

    private void setBottleColor(ShapeRenderer renderer) {
        switch (colorVariant) {
            case 0 -> renderer.setColor(0.12f, 0.52f, 0.34f, 0.86f);
            case 1 -> renderer.setColor(0.48f, 0.27f, 0.09f, 0.9f);
            default -> renderer.setColor(0.48f, 0.72f, 0.76f, 0.72f);
        }
    }

    private void setPaperColor(ShapeRenderer renderer) {
        switch (colorVariant) {
            case 0 -> renderer.setColor(0.88f, 0.84f, 0.68f, 0.96f);
            case 1 -> renderer.setColor(0.72f, 0.66f, 0.49f, 0.96f);
            default -> renderer.setColor(0.78f, 0.8f, 0.75f, 0.94f);
        }
    }

    private static void drawLocalRect(ShapeRenderer renderer, float originX, float originY,
                                      float localCenterX, float localCenterY,
                                      float width, float height, float degrees) {
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        drawLocalTriangle(renderer, originX, originY,
            localCenterX - halfWidth, localCenterY - halfHeight,
            localCenterX + halfWidth, localCenterY - halfHeight,
            localCenterX + halfWidth, localCenterY + halfHeight,
            degrees);
        drawLocalTriangle(renderer, originX, originY,
            localCenterX - halfWidth, localCenterY - halfHeight,
            localCenterX + halfWidth, localCenterY + halfHeight,
            localCenterX - halfWidth, localCenterY + halfHeight,
            degrees);
    }

    private static void drawLocalTriangle(ShapeRenderer renderer, float originX, float originY,
                                          float x1, float y1, float x2, float y2,
                                          float x3, float y3, float degrees) {
        float cosine = MathUtils.cosDeg(degrees);
        float sine = MathUtils.sinDeg(degrees);
        renderer.triangle(
            originX + x1 * cosine - y1 * sine, originY + x1 * sine + y1 * cosine,
            originX + x2 * cosine - y2 * sine, originY + x2 * sine + y2 * cosine,
            originX + x3 * cosine - y3 * sine, originY + x3 * sine + y3 * cosine);
    }

    @Override
    public boolean collides(Rectangle target) {
        return warningTimer <= 0f && bounds.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return bounds.y + SIZE < 0f || bounds.x + SIZE < 0f;
    }

    @Override
    public float collisionDamage() {
        return 34f;
    }

    float x() {
        return bounds.x;
    }

    float y() {
        return bounds.y;
    }

    private enum DebrisType {
        TIRE,
        BOTTLE,
        PAPER;

        private static final DebrisType[] VALUES = values();

        private static DebrisType random() {
            return VALUES[MathUtils.random(VALUES.length - 1)];
        }
    }
}
