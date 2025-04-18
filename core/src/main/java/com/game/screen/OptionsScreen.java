package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.game.DeepDiveDrift;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;

public class OptionsScreen implements Screen {

    private final DeepDiveDrift game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final ShapeRenderer shapeRenderer;

    private final String[] options = {"Music: ", "Sound Effects: ", "Back"};
    private int selectedIndex = 0;
    private boolean musicOn = true;
    private boolean sfxOn = true;

    private final Screen previousScreen;
    private boolean returnToPause = false;

    public OptionsScreen(DeepDiveDrift game) {
        this(game, null);
    }

    public OptionsScreen(DeepDiveDrift game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.batch = new SpriteBatch();
        this.font = FontManager.getMediumFont();
        this.layout = new GlyphLayout();
        this.shapeRenderer = new ShapeRenderer();

        this.musicOn = AudioManager.isMusicEnabled();
        this.sfxOn = AudioManager.isSfxEnabled();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();

        batch.begin();
        drawOptions();
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
            switch ((selectedIndex + 1) % options.length) {
                case 0 -> {
                    musicOn = !musicOn;
                    AudioManager.updateMusicState(musicOn);
                }
                case 1 -> {
                    sfxOn = !sfxOn;
                    AudioManager.updateSfxState(sfxOn);
                }
                case 2 -> {
                    if (previousScreen instanceof GameScreen gameScreen && returnToPause) {
                        gameScreen.setPaused(true);
                    }
                    game.setScreen(previousScreen != null ? previousScreen : new MainMenuScreen(game));
                }
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            AudioManager.playSelect();
            if (previousScreen instanceof GameScreen gameScreen && returnToPause) {
                gameScreen.setPaused(true);
            }
            game.setScreen(previousScreen != null ? previousScreen : new MainMenuScreen(game));
        }
    }

    private void drawOptions() {
        float startY = Gdx.graphics.getHeight() / 2f + 40;

        for (int i = 0; i < options.length; i++) {
            String label = options[i];
            if (i == 0) label += (musicOn ? "ON" : "OFF");
            if (i == 1) label += (sfxOn ? "ON" : "OFF");

            layout.setText(font, label);
            float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float y = startY - i * 40;

            font.setColor(i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY);
            font.draw(batch, layout, x, y);
        }
    }

    public void setReturnToPause(boolean returnToPause) {
        this.returnToPause = returnToPause;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
