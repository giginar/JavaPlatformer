package com.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.game.DeepDiveDrift;
import com.game.GameConfig;

public abstract class BaseScreen implements Screen {
    private static final Color BUTTON_COLOR = new Color(0.02f, 0.13f, 0.2f, 0.96f);
    private static final Color BUTTON_HIGHLIGHT = new Color(0.12f, 0.58f, 0.7f, 1f);
    protected final DeepDiveDrift game;
    protected final SpriteBatch batch;
    protected final ShapeRenderer shapeRenderer;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;

    private boolean disposed;
    private final Color buttonFill = new Color();
    private final GlyphLayout buttonLabel = new GlyphLayout();

    protected BaseScreen(DeepDiveDrift game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        viewport.apply(true);
        updateProjectionMatrices();
    }

    protected void prepareFrame(float red, float green, float blue) {
        Gdx.gl.glClearColor(red, green, blue, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        updateProjectionMatrices();
    }

    protected void updateInput() {
        game.input().update(viewport);
    }

    protected void beginFilledShapes() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    protected void endShapes() {
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Draw inside a filled-shape pass; visual feedback never changes the hit area. */
    protected void drawUiButton(Rectangle bounds, boolean selected) {
        drawUiButton(bounds, BUTTON_COLOR, selected);
    }

    protected void drawUiButton(Rectangle bounds, Color baseColor, boolean selected) {
        buttonFill.set(baseColor);
        boolean hovered = game.input().pointerOver(bounds);
        boolean pressed = game.input().buttonPressed(bounds);
        if (hovered || selected) {
            float padding = GameConfig.BUTTON_GLOW_PADDING;
            shapeRenderer.setColor(0.2f, 0.85f, 1f, hovered ? 0.22f : 0.08f);
            shapeRenderer.rect(bounds.x - padding, bounds.y - padding,
                bounds.width + padding * 2f, bounds.height + padding * 2f);
        }
        float inset = pressed ? 1f : hovered ? -1f : 0f;
        float x = bounds.x + inset;
        float y = bounds.y + inset - (pressed ? GameConfig.BUTTON_PRESS_OFFSET : 0f);
        float width = bounds.width - inset * 2f;
        float height = bounds.height - inset * 2f;
        if (!pressed) {
            shapeRenderer.setColor(0f, 0.015f, 0.04f, 0.7f);
            shapeRenderer.rect(x, y - 3f, width, height);
        }
        shapeRenderer.setColor(0.2f, 0.85f, 1f, hovered || selected ? 0.9f : 0.25f);
        shapeRenderer.rect(x, y, width, height);
        buttonFill.lerp(BUTTON_HIGHLIGHT, pressed ? 0.65f : hovered ? 0.4f : selected ? 0.18f : 0f);
        shapeRenderer.setColor(buttonFill);
        shapeRenderer.rect(x + 1.5f, y + 1.5f, width - 3f, height - 3f);
    }

    /** Draw inside a batch pass, with the same hover/press state as the button background. */
    protected void drawUiButtonLabel(BitmapFont font, String text, Rectangle bounds, Color color) {
        boolean pressed = game.input().buttonPressed(bounds);
        boolean hovered = game.input().pointerOver(bounds);
        float scaleX = font.getData().scaleX;
        float scaleY = font.getData().scaleY;
        float scale = pressed ? GameConfig.BUTTON_PRESSED_SCALE
            : hovered ? GameConfig.BUTTON_HOVER_SCALE : 1f;
        font.getData().setScale(scaleX * scale, scaleY * scale);
        font.setColor(hovered ? Color.WHITE : color);
        buttonLabel.setText(font, text);
        if (buttonLabel.width > bounds.width - 20f) {
            float fit = (bounds.width - 20f) / buttonLabel.width;
            font.getData().setScale(scaleX * scale * fit, scaleY * scale * fit);
            buttonLabel.setText(font, text);
        }
        font.draw(batch, buttonLabel, bounds.x + (bounds.width - buttonLabel.width) / 2f,
            bounds.y + (bounds.height + buttonLabel.height) / 2f
                - (pressed ? GameConfig.BUTTON_PRESS_OFFSET : 0f));
        font.getData().setScale(scaleX, scaleY);
    }

    protected void updateProjectionMatrices() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    protected void centerCamera() {
        camera.position.set(GameConfig.WORLD_WIDTH / 2f, GameConfig.WORLD_HEIGHT / 2f, 0f);
        updateProjectionMatrices();
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            viewport.update(width, height, true);
            updateProjectionMatrices();
        }
    }

    @Override public void show() {
    }

    @Override public void pause() {
    }

    @Override public void resume() {
    }

    @Override public void hide() {
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        batch.dispose();
        shapeRenderer.dispose();
    }
}
