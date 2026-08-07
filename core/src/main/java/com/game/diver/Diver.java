package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.manager.GameAssets;

public class Diver {

    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private final Sprite sprite;
    private float animationTime;

    private static final float GRAVITY = -360f;
    private static final float SWIM_VELOCITY = 230f;
    private static final float MAX_FALL_SPEED = -280f;
    private static final float HITBOX_PADDING = 8f;

    public static final float WIDTH = 64f;
    public static final float HEIGHT = 64f;

    public Diver() {
        position = new Vector2(110f, GameConfig.WORLD_HEIGHT / 2f - HEIGHT / 2f);
        velocity = new Vector2();
        bounds = new Rectangle();
        Texture texture = GameAssets.texture(GameAssets.DIVER);
        sprite = new Sprite(texture);
        sprite.setSize(WIDTH, HEIGHT);
        sprite.setOriginCenter();
        updateBounds();
    }

    public void update(float delta, boolean swimmingUp, float agilityMultiplier) {
        animationTime += delta;
        if (swimmingUp) {
            velocity.y = SWIM_VELOCITY * agilityMultiplier;
        } else {
            velocity.y += GRAVITY / agilityMultiplier * delta;
            velocity.y = Math.max(velocity.y, MAX_FALL_SPEED * agilityMultiplier);
        }

        position.y += velocity.y * delta;

        if (position.y < 0) {
            position.y = 0;
            velocity.y = 0;
        }

        if (position.y > GameConfig.WORLD_HEIGHT - HEIGHT) {
            position.y = GameConfig.WORLD_HEIGHT - HEIGHT;
            velocity.y = 0;
        }
        updateBounds();
    }

    public void render(SpriteBatch batch, float invulnerabilityTimer) {
        sprite.setPosition(position.x, position.y);
        sprite.setRotation(MathUtils.clamp(velocity.y * 0.045f, -12f, 10f));
        float breathing = MathUtils.sin(animationTime * 4f) * 0.025f;
        sprite.setScale(1f + breathing, 1f - breathing * 0.6f);
        boolean dimmed = invulnerabilityTimer > 0f && ((int) (invulnerabilityTimer * 14f) % 2 == 0);
        sprite.setColor(1f, 1f, 1f, dimmed ? 0.3f : 1f);
        sprite.draw(batch);
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    private void updateBounds() {
        bounds.set(position.x + HITBOX_PADDING, position.y + HITBOX_PADDING,
            WIDTH - HITBOX_PADDING * 2f, HEIGHT - HITBOX_PADDING * 2f);
    }
}
