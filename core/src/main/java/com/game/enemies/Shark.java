package com.game.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Shark implements EnemyFish {

    private final Texture texture;
    private final Sprite sprite;
    private final Vector2 position;
    private final Rectangle bounds;
    private final float speed = 100f;

    private int hp = 2;
    private float hitEffectTimer = 0f;

    private final float WIDTH = 96f;
    private final float HEIGHT = 48f;

    public Shark(float y) {
        this.texture = new Texture("enemy_shark.png");
        this.sprite = new Sprite(texture);
        this.sprite.setSize(WIDTH, HEIGHT);
        this.position = new Vector2(Gdx.graphics.getWidth(), y);
        this.bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
        this.sprite.setPosition(position.x, position.y);
    }

    @Override
    public void update(float delta) {
        position.x -= speed * delta;

        if (hitEffectTimer > 0) {
            hitEffectTimer -= delta;
        }

        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitEffectTimer > 0) {
            sprite.setColor(Color.RED);
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
        return position.x + sprite.getWidth() < 0;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    @Override
    public void hit() {
        hp--;
        hitEffectTimer = 0.2f; // flash red for 0.2s
    }

    public boolean isDead() {
        return hp <= 0;
    }
}
