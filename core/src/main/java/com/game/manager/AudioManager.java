package com.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    private static Music backgroundMusic;
    private static Sound shootSound, hitSound, oxygenSound, gameoverSound, breathSound;

    private static boolean musicOn = true;
    private static boolean sfxOn = true;

    private static boolean initialized = false;
    private static Preferences prefs;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        prefs = Gdx.app.getPreferences("DeepDiveDriftPrefs");

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
        if (enabled) {
            if (!backgroundMusic.isPlaying()) backgroundMusic.play();
        } else {
            backgroundMusic.stop();
        }
    }

    public static void updateSfxState(boolean enabled) {
        sfxOn = enabled;
    }

    public static void playShoot() {
        if (sfxOn) shootSound.play();
    }

    public static void playHit() {
        if (sfxOn) hitSound.play();
    }

    public static void playOxygen() {
        if (sfxOn) oxygenSound.play();
    }

    public static void playGameOver() {
        if (sfxOn) gameoverSound.play();
    }

    public static void playBreath() {
        if (sfxOn) breathSound.play();
    }

    public static void toggleMusic() {
        musicOn = !musicOn;
        prefs.putBoolean("musicOn", musicOn);
        prefs.flush();
        updateMusicState(musicOn);
    }

    public static void toggleSfx() {
        sfxOn = !sfxOn;
        prefs.putBoolean("sfxOn", sfxOn);
        prefs.flush();
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

        initialized = false;
    }

    public static boolean isInitialized() {
        return prefs != null;
    }
}
