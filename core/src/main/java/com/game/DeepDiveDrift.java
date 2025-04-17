package com.game;

import com.badlogic.gdx.Game;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.screen.GameScreen;
import com.game.screen.MainMenuScreen;

public class DeepDiveDrift extends Game {
    @Override
    public void create() {
        if (!AudioManager.isInitialized()) {
            AudioManager.initialize();
        }
        FontManager.initialize();
        this.setScreen(new MainMenuScreen(this));
    }

}
