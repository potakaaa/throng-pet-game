package com.throng.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private static AudioManager instance;
    private Music backgroundMusic;
    private Sound eatingSound;
    private Sound clickMenuSound;
    private Sound clickActionSound;
    private float volume = 0.5f;
    private boolean isMuted = false;

    private AudioManager() {
        // Private constructor for singleton
        eatingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/eating-sound-effect.mp3"));
        clickMenuSound = Gdx.audio.newSound(Gdx.files.internal("sounds/pop-clicking-effect.mp3"));
        clickActionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/tap-clicking-effect.mp3"));

    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void playBackgroundMusic(String filePath) {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(filePath));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(volume);
        backgroundMusic.play();
    }

    public void playEatingSound() {
        eatingSound.play(volume);
    }

    public void playClickMenuSound() {
        clickMenuSound.play(volume);
    }

    public void playClickActionSound() {
        clickActionSound.play(volume);
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(isMuted ? 0f : this.volume);
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(isMuted ? 0f : volume);
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void pause() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    public void resume() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void dispose() {
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        if (eatingSound != null) {
            eatingSound.dispose();
            eatingSound = null;
        }
    }
}