package com.game.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SmallFish implements EnemyFish {

    private Texture texture;
    private Vector2 position;
    private float speed = 100f;
    private float width = 48f;
    private float height = 32f;

    public SmallFish(float y) {
        this.texture = new Texture("enemy_small.png"); // Görselin adını bu şekilde kaydedin
        this.position = new Vector2(800, y); // ekranın sağından başla
    }

    @Override
    public void update(float delta) {
        position.x -= speed * delta;
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
        return new Rectangle(position.x, position.y, width, height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
