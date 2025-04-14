package com.diver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameScreen implements Screen {

    private ShapeRenderer shapeRenderer;
    private Diver diver;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        diver = new Diver();
    }

    @Override
    public void render(float delta) {
        // Su mavisi arkaplan
        Gdx.gl.glClearColor(0f, 0.2f, 0.4f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        diver.update(delta);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        diver.render(shapeRenderer);
        shapeRenderer.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        shapeRenderer.dispose();
    }
}
