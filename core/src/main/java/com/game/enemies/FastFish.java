package com.game.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class FastFish implements EnemyFish {

    private Texture texture;
    private Vector2 position;
    private Rectangle bounds;
    private float speed = 500f;
    private float speedMultiplier = 1f;
    private float width = 40f;
    private float height = 28f;
    private float animationTime = 0f;
    private float baseY;

    public FastFish(float y) {
        this.texture = new Texture("enemy_fast.png");
        this.position = new Vector2(Gdx.graphics.getWidth(), y);
        this.baseY = y;
        this.bounds = new Rectangle(position.x, position.y, width, height);
    }

    @Override
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public void update(float delta) {
        animationTime += delta;
        position.x -= speed * delta * speedMultiplier;
        position.y = baseY + (float)Math.sin(animationTime * 5f) * 4f;
        bounds.setPosition(position.x, position.y);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, width, height);
    }

    @Override
    public boolean isOutOfScreen() {
        return position.x + width < 0;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void hit() {

    }

    @Override
    public boolean isDead() {
        return false;
    }


    @Override
    public void dispose() {
        texture.dispose();
    }
}
