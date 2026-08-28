package com.game.diver;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.game.GameConfig;
import com.game.manager.GameAssets;

public class Background {

    private final Texture texture;
    private float x1, x2;
    private final float scrollSpeed;
    private final float drawHeight;
    private final float drawY;
    private final Color tint = new Color(Color.WHITE);
    private final Color targetTint = new Color(Color.WHITE);
    private float speedMultiplier = 1f;
    private float verticalOffset;
    private float targetVerticalOffset;

    public Background() {
        this(45f);
    }

    public Background(float scrollSpeed) {
        texture = GameAssets.texture(GameAssets.BACKGROUND);
        this.scrollSpeed = scrollSpeed;
        drawHeight = GameConfig.WORLD_WIDTH * texture.getHeight() / texture.getWidth() + 80f;
        drawY = (GameConfig.WORLD_HEIGHT - drawHeight) / 2f;
        x1 = 0;
        x2 = GameConfig.WORLD_WIDTH;
    }

    public void update(float delta) {
        x1 -= scrollSpeed * speedMultiplier * delta;
        x2 -= scrollSpeed * speedMultiplier * delta;
        tint.lerp(targetTint, Math.min(1f, delta * 0.65f));
        verticalOffset = MathUtils.lerp(verticalOffset, targetVerticalOffset,
            Math.min(1f, delta * 0.42f));

        if (x1 + GameConfig.WORLD_WIDTH <= 0) {
            x1 = x2 + GameConfig.WORLD_WIDTH;
        }
        if (x2 + GameConfig.WORLD_WIDTH <= 0) {
            x2 = x1 + GameConfig.WORLD_WIDTH;
        }
    }

    public void render(SpriteBatch batch) {
        batch.setColor(tint);
        batch.draw(texture, x1, drawY + verticalOffset, GameConfig.WORLD_WIDTH, drawHeight);
        batch.draw(texture, x2, drawY + verticalOffset, GameConfig.WORLD_WIDTH, drawHeight);
        batch.setColor(Color.WHITE);
    }

    public void setDepthStage(int stage, float speedMultiplier) {
        this.speedMultiplier = MathUtils.clamp(speedMultiplier, 0.5f, 2f);
        targetVerticalOffset = MathUtils.clamp((stage - 1) * 8f, 0f, 32f);
        targetTint.set(switch (stage) {
            case 1 -> new Color(0.86f, 1f, 0.98f, 1f);
            case 2 -> new Color(0.54f, 0.8f, 0.92f, 1f);
            case 3 -> new Color(0.34f, 0.57f, 0.78f, 1f);
            case 4 -> new Color(0.21f, 0.33f, 0.54f, 1f);
            default -> new Color(0.14f, 0.14f, 0.28f, 1f);
        });
    }
}
