package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.game.DeepDiveDrift;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;

public class OptionsScreen implements Screen {

    private final DeepDiveDrift game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    private final String[] options = {"Toggle Music", "Toggle SFX", "Back"};
    private int selectedIndex = 0;

    public OptionsScreen(DeepDiveDrift game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = FontManager.getMediumFont();
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        font.setColor(Color.WHITE);
        layout.setText(font, "Options");
        font.draw(batch, layout, centerX(layout), Gdx.graphics.getHeight() - 100);

        float startY = Gdx.graphics.getHeight() / 2f;
        for (int i = 0; i < options.length; i++) {
            boolean selected = i == selectedIndex;
            font.setColor(selected ? Color.YELLOW : Color.LIGHT_GRAY);
            String label = options[i];

            if (label.equals("Toggle Music")) {
                label += " [" + (AudioManager.isMusicEnabled() ? "ON" : "OFF") + "]";
            } else if (label.equals("Toggle SFX")) {
                label += " [" + (AudioManager.isSfxEnabled() ? "ON" : "OFF") + "]";
            }

            layout.setText(font, label);
            font.draw(batch, layout, centerX(layout), startY - i * 40);
        }

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % options.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            AudioManager.playConfirm();
            switch (selectedIndex) {
                case 0 -> AudioManager.toggleMusic();
                case 1 -> AudioManager.toggleSfx();
                case 2 -> game.setScreen(new MainMenuScreen(game));
            }
        }
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
