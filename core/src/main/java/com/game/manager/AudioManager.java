package com.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.game.GameConfig;

public class AudioManager {

    private static Music backgroundMusic;
    private static Sound shootSound, hitSound, oxygenSound, gameoverSound, breathSound, selectSound, confirmSound;

    private static boolean musicOn = true;
    private static boolean sfxOn = true;

    private static boolean initialized = false;
    private static Preferences prefs;

    public static void initialize() {
        if (initialized) return;

        prefs = Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME);

        musicOn = prefs.getBoolean("musicOn", true);
        sfxOn = prefs.getBoolean("sfxOn", true);

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("underwater.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.3f);

        shootSound = Gdx.audio.newSound(Gdx.files.internal("shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
        oxygenSound = Gdx.audio.newSound(Gdx.files.internal("oxygen.wav"));
        gameoverSound = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
        breathSound = Gdx.audio.newSound(Gdx.files.internal("breath.mp3"));
        selectSound = Gdx.audio.newSound(Gdx.files.internal("select.wav"));
        confirmSound = Gdx.audio.newSound(Gdx.files.internal("confirm.wav"));

        initialized = true;
        if (musicOn) backgroundMusic.play();
    }


    public static void playBackgroundMusic() {
        if (musicOn && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f);
            backgroundMusic.play();
        }
    }

    public static void updateMusicState(boolean enabled) {
        musicOn = enabled;
        saveBoolean("musicOn", enabled);
        if (backgroundMusic == null) {
            return;
        }
        if (enabled && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        } else if (!enabled) {
            backgroundMusic.pause();
        }
    }

    public static void updateSfxState(boolean enabled) {
        sfxOn = enabled;
        saveBoolean("sfxOn", enabled);
    }

    public static void playShoot() {
        play(shootSound);
    }

    public static void playHit() {
        play(hitSound);
    }

    public static void playOxygen() {
        play(oxygenSound);
    }

    public static void playGameOver() {
        play(gameoverSound);
    }

    public static void playBreath() {
        play(breathSound);
    }

    public static void playSelect() {
        play(selectSound);
    }

    public static void playConfirm() {
        play(confirmSound);
    }

    public static void toggleMusic() {
        updateMusicState(!musicOn);
    }

    public static void toggleSfx() {
        updateSfxState(!sfxOn);
    }

    public static boolean isMusicEnabled() {
        return musicOn;
    }

    public static boolean isSfxEnabled() {
        return sfxOn;
    }

    public static void dispose() {
        if (!initialized) return;

        backgroundMusic.dispose();
        shootSound.dispose();
        hitSound.dispose();
        oxygenSound.dispose();
        gameoverSound.dispose();
        breathSound.dispose();
        selectSound.dispose();
        confirmSound.dispose();

        backgroundMusic = null;
        shootSound = null;
        hitSound = null;
        oxygenSound = null;
        gameoverSound = null;
        breathSound = null;
        selectSound = null;
        confirmSound = null;
        prefs = null;
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void play(Sound sound) {
        if (initialized && sfxOn && sound != null) {
            sound.play();
        }
    }

    private static void saveBoolean(String key, boolean value) {
        if (prefs != null) {
            prefs.putBoolean(key, value);
            prefs.flush();
        }
    }
}
