package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.enemies.EnemyFish;
import com.game.manager.GameAssets;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class Harpoon {

    private final Texture texture;
    private final Vector2 position;
    private final Rectangle bounds;
    private final Set<EnemyFish> hitEnemies;
    private int remainingHits;
    private static final float SPEED = 620f;
    private static final float WIDTH = 38f;
    private static final float HEIGHT = 10f;

    public Harpoon(float x, float y, int hitCount) {
        texture = GameAssets.texture(GameAssets.HARPOON);
        position = new Vector2(x, y);
        bounds = new Rectangle(x, y, WIDTH, HEIGHT);
        remainingHits = Math.max(1, hitCount);
        hitEnemies = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public void update(float delta) {
        position.x += SPEED * delta;
        bounds.setPosition(position);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, WIDTH, HEIGHT);
    }

    public boolean isOutOfScreen() {
        return position.x > GameConfig.WORLD_WIDTH;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean canHit(EnemyFish enemy) {
        return !hitEnemies.contains(enemy);
    }

    public boolean registerHit(EnemyFish enemy) {
        hitEnemies.add(enemy);
        remainingHits--;
        return remainingHits <= 0;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
}
