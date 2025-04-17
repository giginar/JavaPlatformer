package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.game.DeepDiveDrift;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;

public class MainMenuScreen implements Screen {

    private final DeepDiveDrift game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    private final String[] menuItems = {"Play Game", "Options", "Exit"};
    private int selectedIndex = 0;

    public MainMenuScreen(DeepDiveDrift game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = FontManager.getMediumFont();
        layout = new GlyphLayout();

        AudioManager.playBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        layout.setText(font, "Deep Dive Drift");
        font.draw(batch, layout, centerX(layout), Gdx.graphics.getHeight() - 100);

        float startY = Gdx.graphics.getHeight() / 2f;
        for (int i = 0; i < menuItems.length; i++) {
            font.setColor(i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY);
            layout.setText(font, menuItems[i]);
            font.draw(batch, layout, centerX(layout), startY - i * 40);
        }

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + menuItems.length) % menuItems.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % menuItems.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            AudioManager.playConfirm();
            switch (selectedIndex) {
                case 0 -> game.setScreen(new GameScreen());
                case 1 -> game.setScreen(new OptionsScreen(game));
                case 2 -> Gdx.app.exit();
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
