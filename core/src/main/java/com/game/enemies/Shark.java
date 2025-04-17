package com.game.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Shark implements EnemyFish {

    private Texture texture;
    private Vector2 position;
    private Rectangle bounds;
    private float speed = 250f;
    private float width = 80f;
    private float height = 40f;
    private float animationTime = 0f;
    private float baseY;

    public Shark(float y) {
        this.texture = new Texture("enemy_shark.png");
        this.position = new Vector2(Gdx.graphics.getWidth(), y);
        this.baseY = y;
        this.bounds = new Rectangle(position.x, position.y, width, height);
    }

    @Override
    public void update(float delta) {
        animationTime += delta;
        position.x -= speed * delta;
        position.y = baseY + (float)Math.sin(animationTime * 2.5f) * 3f;
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
    public void dispose() {
        texture.dispose();
    }
}
