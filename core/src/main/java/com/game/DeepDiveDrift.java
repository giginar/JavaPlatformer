package com.game;

import com.badlogic.gdx.Game;
import com.game.diver.GameScreen;

public class DeepDiveDrift extends Game {
    @Override
    public void create() {
        this.setScreen(new GameScreen());
    }
}
