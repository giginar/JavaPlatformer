package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;

public class MainMenuScreen implements Screen {
    private final DeepDiveDrift game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    private Rectangle playBounds;
    private Rectangle optionsBounds;
    private Rectangle exitBounds;

    public MainMenuScreen(DeepDiveDrift game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        layout = new GlyphLayout();

        layout.setText(font, "[P] Play Game");
        playBounds = new Rectangle(centerX(layout), 280, layout.width, layout.height);

        layout.setText(font, "[O] Options");
        optionsBounds = new Rectangle(centerX(layout), 230, layout.width, layout.height);

        layout.setText(font, "[ESC] Exit");
        exitBounds = new Rectangle(centerX(layout), 180, layout.width, layout.height);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                screenY = Gdx.graphics.getHeight() - screenY;
                if (playBounds.contains(screenX, screenY)) {
                    game.setScreen(new GameScreen());
                } else if (optionsBounds.contains(screenX, screenY)) {
                    game.setScreen(new OptionsScreen(game));
                } else if (exitBounds.contains(screenX, screenY)) {
                    Gdx.app.exit();
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

        layout.setText(font, "Deep Dive Drift");
        font.setColor(Color.CYAN);
        font.draw(batch, layout, centerX(layout), 400);

        drawButton("[P] Play Game", 300, playBounds);
        drawButton("[O] Options", 250, optionsBounds);
        drawButton("[ESC] Exit", 200, exitBounds);

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
