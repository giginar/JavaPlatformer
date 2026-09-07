package com.game.manager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.game.model.MemoryPreferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class AudioManagerTest {
    private final MemoryPreferences preferences = new MemoryPreferences();
    private Application previousApplication;
    private Audio previousAudio;
    private Files previousFiles;
    private Application.ApplicationType platform = Application.ApplicationType.Android;
    private int musicStarts;
    private int effectStarts;
    private int effectStops;
    private boolean musicPlaying;
    private float musicVolume;
    private float effectVolume;

    @BeforeEach
    void setUp() {
        previousApplication = Gdx.app;
        previousAudio = Gdx.audio;
        previousFiles = Gdx.files;
        Music music = proxy(Music.class, (instance, method, args) -> {
            switch (method.getName()) {
                case "play" -> { musicStarts++; musicPlaying = true; }
                case "pause", "stop", "dispose" -> musicPlaying = false;
                case "isPlaying" -> { return musicPlaying; }
                case "setVolume" -> musicVolume = (float) args[0];
                case "setLooping" -> { }
                default -> throw new UnsupportedOperationException(method.getName());
            }
            return null;
        });
        Sound sound = proxy(Sound.class, (instance, method, args) -> {
            switch (method.getName()) {
                case "play" -> {
                    effectStarts++;
                    effectVolume = args.length == 0 ? 1f : (float) args[0];
                    return 1L;
                }
                case "stop" -> effectStops++;
                case "dispose" -> { }
                default -> throw new UnsupportedOperationException(method.getName());
            }
            return null;
        });
        Gdx.app = proxy(Application.class, (instance, method, args) -> switch (method.getName()) {
            case "getPreferences" -> preferences;
            case "getType" -> platform;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        Gdx.audio = proxy(Audio.class, (instance, method, args) -> switch (method.getName()) {
            case "newMusic" -> music;
            case "newSound" -> sound;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        Gdx.files = proxy(Files.class, (instance, method, args) -> switch (method.getName()) {
            case "internal" -> new FileHandle((String) args[0]);
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    @AfterEach
    void tearDown() {
        AudioManager.dispose();
        Gdx.app = previousApplication;
        Gdx.audio = previousAudio;
        Gdx.files = previousFiles;
    }

    @Test
    void firstMobileLaunchBlocksMusicAndEffectsUntilAnExplicitChoice() {
        AudioManager.initialize();
        AudioManager.playBackgroundMusic();
        AudioManager.updateMusicState(true);
        AudioManager.playSelect();
        AudioManager.playConfirm();

        assertTrue(AudioManager.needsStartupSoundChoice());
        assertEquals(0, musicStarts);
        assertEquals(0, effectStarts);
        assertEquals(0f, musicVolume);
    }

    @Test
    void silentChoiceIsRememberedAcrossLaunches() {
        AudioManager.initialize();
        AudioManager.chooseStartupSound(false);
        AudioManager.dispose();
        AudioManager.initialize();
        AudioManager.playBackgroundMusic();
        AudioManager.playShoot();

        assertFalse(AudioManager.needsStartupSoundChoice());
        assertEquals(0f, AudioManager.getMasterVolume());
        assertEquals(0, musicStarts);
        assertEquals(0, effectStarts);
    }

    @Test
    void enablingSoundStartsMusicAndRemembersTheChoice() {
        AudioManager.initialize();
        AudioManager.chooseStartupSound(true);
        AudioManager.playConfirm();

        assertFalse(AudioManager.needsStartupSoundChoice());
        assertTrue(preferences.getBoolean("startupSoundChosen"));
        assertTrue(musicPlaying);
        assertEquals(1, musicStarts);
        assertEquals(0.3f, musicVolume, 0.0001f);
        assertEquals(1f, effectVolume);
    }

    @Test
    void masterVolumeAppliesToMusicAndEffects() {
        preferences.putBoolean("startupSoundChosen", true);
        AudioManager.initialize();
        AudioManager.setMasterVolume(0.25f);
        AudioManager.playShoot();

        assertEquals(0.075f, musicVolume, 0.0001f);
        assertEquals(0.25f, effectVolume);
        assertEquals(0.25f, preferences.getFloat("masterVolume"));
    }

    @Test
    void mutingStopsActiveAudioAndUnmutingRestoresThePreviousVolume() {
        preferences.putBoolean("startupSoundChosen", true);
        AudioManager.initialize();
        AudioManager.setMasterVolume(0.5f);
        AudioManager.playShoot();
        int previousStops = effectStops;
        AudioManager.toggleMute();
        AudioManager.playConfirm();

        assertFalse(musicPlaying);
        assertEquals(0f, musicVolume);
        assertEquals(1, effectStarts);
        assertTrue(effectStops > previousStops);
        AudioManager.toggleMute();
        assertTrue(musicPlaying);
        assertEquals(0.5f, AudioManager.getMasterVolume());
        assertEquals(0.15f, musicVolume, 0.0001f);
    }

    @Test
    void globalVolumePreservesSeparateMusicAndEffectsPreferences() {
        preferences.putBoolean("startupSoundChosen", true);
        preferences.putBoolean("musicOn", false);
        preferences.putBoolean("sfxOn", false);
        AudioManager.initialize();
        AudioManager.toggleMute();
        AudioManager.toggleMute();
        AudioManager.playShoot();

        assertEquals(0, musicStarts);
        assertEquals(0, effectStarts);
        assertFalse(AudioManager.isMusicEnabled());
        assertFalse(AudioManager.isSfxEnabled());
    }

    @Test
    void desktopLaunchUsesTheSavedVolumeWithoutAnOnboardingPrompt() {
        platform = Application.ApplicationType.Desktop;
        preferences.putFloat("masterVolume", 0.5f);
        AudioManager.initialize();

        assertFalse(AudioManager.needsStartupSoundChoice());
        assertTrue(musicPlaying);
        assertEquals(0.15f, musicVolume, 0.0001f);
    }

    @Test
    void volumeControlsStayWithinTheirBounds() {
        preferences.putBoolean("startupSoundChosen", true);
        AudioManager.initialize();
        AudioManager.setMasterVolume(1.25f);
        assertEquals(1f, AudioManager.getMasterVolume());
        AudioManager.setMasterVolume(-0.25f);
        assertEquals(0f, AudioManager.getMasterVolume());
        assertFalse(musicPlaying);
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }
}
