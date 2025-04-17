package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.game.DeepDiveDrift;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.diver.Background;

public class MainMenuScreen implements Screen {

    private final DeepDiveDrift game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final ShapeRenderer shapeRenderer;
    private final Background background;

    private final String[] menuOptions = {"PLAY", "OPTIONS", "EXIT"};
    private int selectedIndex = 0;
    private float transitionAlpha = 1f;
    private boolean transitioning = false;

    public MainMenuScreen(DeepDiveDrift game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = FontManager.getMediumFont();
        this.layout = new GlyphLayout();
        this.shapeRenderer = new ShapeRenderer();
        this.background = new Background();
    }

    @Override
    public void show() {
        AudioManager.playBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        background.update(delta);

        batch.begin();
        background.render(batch);
        drawMenu();
        batch.end();

        if (!transitioning) {
            handleInput();
        } else {
            transitionAlpha -= delta;
            if (transitionAlpha <= 0) {
                GameScreen gameScreen = new GameScreen();
                gameScreen.resetGame(); // 🔁 CLEAR & RESET
                game.setScreen(gameScreen);
            }
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % menuOptions.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + menuOptions.length) % menuOptions.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            AudioManager.playConfirm();
            switch ((selectedIndex + 1) % menuOptions.length) {
                case 0 -> {
                    GameScreen gameScreen = new GameScreen(true);
                    game.setScreen(gameScreen);
                    transitioning = true;
                } // PLAY
                case 1 -> game.setScreen(new OptionsScreen(game)); // OPTIONS
                case 2 -> Gdx.app.exit(); // EXIT
            }
        }
    }

    private void drawMenu() {
        float startY = Gdx.graphics.getHeight() / 2f + 40;

        for (int i = 0; i < menuOptions.length; i++) {
            String option = menuOptions[i];
            layout.setText(font, option);
            float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float y = startY - i * 40;

            font.setColor(i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY);
            font.draw(batch, layout, x, y);
        }

        if (transitioning) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1 - transitionAlpha);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        background.dispose();
    }
}
