package com.game.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Shark implements EnemyFish {

    private Texture texture;
    private Vector2 position;
    private Rectangle bounds;
    private float speed = 120f;
    private float width = 80f;
    private float height = 40f;

    public Shark(float y) {
        texture = new Texture("enemy_shark.png");
        position = new Vector2(Gdx.graphics.getWidth(), y); // ekranın sağı
        bounds = new Rectangle(position.x, position.y, width, height);
    }

    @Override
    public void update(float delta) {
        position.x -= speed * delta;
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
