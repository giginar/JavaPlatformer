package com.game.manager;

import com.badlogic.gdx.Application;
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
    private static float masterVolume = 1f;
    private static float lastAudibleVolume = 1f;
    private static boolean soundChoicePending;
    private static Sound[] soundEffects;

    private static boolean initialized = false;
    private static Preferences prefs;

    public static void initialize() {
        if (initialized) return;

        prefs = Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME);

        musicOn = prefs.getBoolean("musicOn", true);
        sfxOn = prefs.getBoolean("sfxOn", true);
        masterVolume = clampVolume(prefs.getFloat("masterVolume", 1f));
        lastAudibleVolume = masterVolume > 0f ? masterVolume
            : Math.max(0.25f, clampVolume(prefs.getFloat("lastAudibleVolume", 1f)));
        // Reconcile preferences saved before the menu and channel switches were linked.
        if (masterVolume == 0f) {
            musicOn = false;
            sfxOn = false;
        } else if (!musicOn && !sfxOn) {
            masterVolume = 0f;
        }
        saveSettings();
        Application.ApplicationType platform = Gdx.app.getType();
        soundChoicePending = (platform == Application.ApplicationType.Android
            || platform == Application.ApplicationType.iOS)
            && !prefs.getBoolean("startupSoundChosen", false);

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("underwater.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.3f * effectiveVolume());

        shootSound = Gdx.audio.newSound(Gdx.files.internal("shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
        oxygenSound = Gdx.audio.newSound(Gdx.files.internal("oxygen.wav"));
        gameoverSound = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
        breathSound = Gdx.audio.newSound(Gdx.files.internal("breath.mp3"));
        selectSound = Gdx.audio.newSound(Gdx.files.internal("select.wav"));
        confirmSound = Gdx.audio.newSound(Gdx.files.internal("confirm.wav"));
        soundEffects = new Sound[] {
            shootSound, hitSound, oxygenSound, gameoverSound, breathSound, selectSound, confirmSound
        };

        initialized = true;
        playBackgroundMusic();
    }


    public static void playBackgroundMusic() {
        if (musicOn && effectiveVolume() > 0f && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f * effectiveVolume());
            backgroundMusic.play();
        }
    }

    public static void updateMusicState(boolean enabled) {
        musicOn = enabled;
        syncMasterVolumeWithChannels();
        saveSettings();
        applyMusicVolume();
    }

    public static void updateSfxState(boolean enabled) {
        sfxOn = enabled;
        syncMasterVolumeWithChannels();
        saveSettings();
        if (!enabled) stopSoundEffects();
        applyMusicVolume();
    }

    public static boolean needsStartupSoundChoice() {
        return soundChoicePending;
    }

    public static void chooseStartupSound(boolean enabled) {
        soundChoicePending = false;
        prefs.putBoolean("startupSoundChosen", true);
        setMasterVolume(enabled ? lastAudibleVolume : 0f);
    }

    public static float getMasterVolume() {
        return masterVolume;
    }

    public static void toggleMute() {
        setMasterVolume(masterVolume > 0f ? 0f : lastAudibleVolume);
    }

    public static void setMasterVolume(float volume) {
        boolean wasMuted = masterVolume == 0f;
        masterVolume = clampVolume(volume);
        if (masterVolume == 0f) {
            musicOn = false;
            sfxOn = false;
        } else {
            lastAudibleVolume = masterVolume;
            if (wasMuted) {
                musicOn = true;
                sfxOn = true;
            }
        }
        saveSettings();
        stopSoundEffects();
        applyMusicVolume();
    }

    private static void syncMasterVolumeWithChannels() {
        if (!musicOn && !sfxOn) {
            masterVolume = 0f;
        } else if (masterVolume == 0f) {
            masterVolume = lastAudibleVolume;
        }
    }

    private static void applyMusicVolume() {
        if (backgroundMusic == null) return;
        backgroundMusic.setVolume(0.3f * effectiveVolume());
        if (!musicOn || effectiveVolume() == 0f) {
            backgroundMusic.pause();
        } else {
            playBackgroundMusic();
        }
    }

    private static float effectiveVolume() {
        return soundChoicePending ? 0f : masterVolume;
    }

    private static float clampVolume(float volume) {
        return Float.isNaN(volume) || Float.isInfinite(volume) ? 0f
            : Math.max(0f, Math.min(1f, volume));
    }

    private static void stopSoundEffects() {
        if (soundEffects != null) {
            for (Sound sound : soundEffects) sound.stop();
        }
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
        soundEffects = null;
        soundChoicePending = false;
        prefs = null;
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void play(Sound sound) {
        if (initialized && sfxOn && effectiveVolume() > 0f && sound != null) {
            sound.play(effectiveVolume());
        }
    }

    private static void saveSettings() {
        if (prefs != null) {
            prefs.putBoolean("musicOn", musicOn);
            prefs.putBoolean("sfxOn", sfxOn);
            prefs.putFloat("masterVolume", masterVolume);
            prefs.putFloat("lastAudibleVolume", lastAudibleVolume);
            prefs.flush();
        }
    }
}
