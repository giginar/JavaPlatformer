package com.game;

import com.badlogic.gdx.Game;
import com.game.screen.GameScreen;
import com.game.screen.MainMenuScreen;

public class DeepDiveDrift extends Game {
    @Override
    public void create() {
        this.setScreen(new MainMenuScreen(this));
    }

}
