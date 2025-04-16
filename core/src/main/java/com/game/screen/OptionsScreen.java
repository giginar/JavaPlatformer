package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;

public class OptionsScreen implements Screen {
    private final DeepDiveDrift game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    private Rectangle musicToggleBounds;
    private Rectangle sfxToggleBounds;
    private Rectangle backBounds;

    private boolean musicOn;
    private boolean sfxOn;
    private Preferences prefs;

    public OptionsScreen(DeepDiveDrift game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        layout = new GlyphLayout();

        prefs = Gdx.app.getPreferences("DeepDiveDriftPrefs");
        musicOn = prefs.getBoolean("musicOn", true);
        sfxOn = prefs.getBoolean("sfxOn", true);

        layout.setText(font, "Toggle Music: " + (musicOn ? "ON" : "OFF"));
        musicToggleBounds = new Rectangle(centerX(layout), 300, layout.width, layout.height);

        layout.setText(font, "Toggle SFX: " + (sfxOn ? "ON" : "OFF"));
        sfxToggleBounds = new Rectangle(centerX(layout), 250, layout.width, layout.height);

        layout.setText(font, "[BACK]");
        backBounds = new Rectangle(centerX(layout), 180, layout.width, layout.height);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                screenY = Gdx.graphics.getHeight() - screenY;

                if (musicToggleBounds.contains(screenX, screenY)) {
                    musicOn = !musicOn;
                    prefs.putBoolean("musicOn", musicOn);
                    prefs.flush();
                } else if (sfxToggleBounds.contains(screenX, screenY)) {
                    sfxOn = !sfxOn;
                    prefs.putBoolean("sfxOn", sfxOn);
                    prefs.flush();
                } else if (backBounds.contains(screenX, screenY)) {
                    game.setScreen(new MainMenuScreen(game));
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        layout.setText(font, "Options");
        font.setColor(Color.CYAN);
        font.draw(batch, layout, centerX(layout), 400);

        drawButton("Toggle Music: " + (musicOn ? "ON" : "OFF"), 300, musicToggleBounds);
        drawButton("Toggle SFX: " + (sfxOn ? "ON" : "OFF"), 250, sfxToggleBounds);
        drawButton("[BACK]", 180, backBounds);

        batch.end();
    }

    private void drawButton(String text, float y, Rectangle bounds) {
        layout.setText(font, text);
        float x = centerX(layout);

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (bounds.contains(mouseX, mouseY)) {
            font.setColor(Color.YELLOW);
        } else {
            font.setColor(Color.WHITE);
        }

        font.draw(batch, layout, x, y);
    }

    private float centerX(GlyphLayout layout) {
        return (Gdx.graphics.getWidth() - layout.width) / 2f;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
