package com.throng.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

public class AudioManager {
    private static AudioManager instance;
    private Music backgroundMusic;
    private Sound eatingSound;
    private Sound clickMenuSound;
    private Sound clickActionSound;
    private Sound randomThrongSound; // For random sounds
    private Sound lowStatThrongSound; // For when stats are low
    private Sound highStatThrongSound; // For when stats are high
    private Sound statChangeThrongSound; // For significant stat changes
    private Sound sleepingSound;
    private float volume = 0.5f;
    private boolean isMuted = false;
    private boolean isSleeping = false;
    private float sleepSoundDuration = 0f;
    private float sleepSoundTimer = 0f;

    // Volume settings
    private static final float NORMAL_BG_VOLUME = 0.5f;
    private static final float SLEEPING_BG_VOLUME = 0.1f; // Lower background music during sleep
    private static final float SLEEPING_SOUND_VOLUME = 5.0f; // Louder sleeping sound

    // Previous stat values to detect changes
    private float prevHunger = 100f;
    private float prevHappiness = 100f;
    private float prevEnergy = 100f;
    private float prevWellbeing = 100f;

    // Thresholds for stat changes
    private static final float STAT_CHANGE_THRESHOLD = 10f; // Minimum change to trigger sound
    private static final float LOW_STAT_THRESHOLD = 30f; // Threshold for low stats
    private static final float HIGH_STAT_THRESHOLD = 70f; // Threshold for high stats

    // Random sound timing
    private float timeSinceLastRandomSound = 0f;
    private static final float MIN_TIME_BETWEEN_RANDOM_SOUNDS = 15f; // Minimum seconds between random sounds
    private static final float RANDOM_SOUND_CHANCE = 0.1f; // 10% chance per check

    private AudioManager() {
        // Private constructor for singleton
        eatingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/eating-sound-effect.mp3"));
        clickMenuSound = Gdx.audio.newSound(Gdx.files.internal("sounds/pop-clicking-effect.mp3"));
        clickActionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/tap-clicking-effect.mp3"));

        // Initialize throng sounds with specific purposes
        randomThrongSound = Gdx.audio.newSound(Gdx.files.internal("sounds/throng/Throng-001.mp3")); // Random sound
        lowStatThrongSound = Gdx.audio.newSound(Gdx.files.internal("sounds/throng/Throng-002.mp3")); // Low stat sound
        highStatThrongSound = Gdx.audio.newSound(Gdx.files.internal("sounds/throng/Throng-003.mp3")); // High stat sound
        statChangeThrongSound = Gdx.audio.newSound(Gdx.files.internal("sounds/throng/Throng-004.mp3")); // Stat change
                                                                                                        // sound

        sleepingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/lullaby-effect.mp3"));
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void update(float delta) {
        if (isMuted || isSleeping)
            return;

        // Handle random sound
        timeSinceLastRandomSound += delta;
        if (timeSinceLastRandomSound >= MIN_TIME_BETWEEN_RANDOM_SOUNDS &&
                Math.random() < RANDOM_SOUND_CHANCE) {
            randomThrongSound.play(volume);
            timeSinceLastRandomSound = 0f;
        }

        // Update sleep sound timer
        if (isSleeping) {
            sleepSoundTimer += delta;
            if (sleepSoundTimer >= sleepSoundDuration) {
                isSleeping = false;
                sleepSoundTimer = 0f;
                // Restore normal background music volume
                if (backgroundMusic != null) {
                    backgroundMusic.setVolume(isMuted ? 0f : NORMAL_BG_VOLUME);
                }
            }
        }
    }

    public void updateStats(float hunger, float happiness, float energy, float wellbeing) {
        if (isMuted || isSleeping)
            return;

        // Check for significant stat changes
        boolean hasLargeChange = false;
        boolean hasLowStat = false;
        boolean hasHighStat = false;

        // Check for large stat changes
        if (Math.abs(hunger - prevHunger) >= STAT_CHANGE_THRESHOLD ||
                Math.abs(happiness - prevHappiness) >= STAT_CHANGE_THRESHOLD ||
                Math.abs(energy - prevEnergy) >= STAT_CHANGE_THRESHOLD ||
                Math.abs(wellbeing - prevWellbeing) >= STAT_CHANGE_THRESHOLD) {
            hasLargeChange = true;
        }

        // Check for low stats
        if (hunger <= LOW_STAT_THRESHOLD ||
                happiness <= LOW_STAT_THRESHOLD ||
                energy <= LOW_STAT_THRESHOLD ||
                wellbeing <= LOW_STAT_THRESHOLD) {
            hasLowStat = true;
        }

        // Check for high stats
        if (hunger >= HIGH_STAT_THRESHOLD &&
                happiness >= HIGH_STAT_THRESHOLD &&
                energy >= HIGH_STAT_THRESHOLD &&
                wellbeing >= HIGH_STAT_THRESHOLD) {
            hasHighStat = true;
        }

        // Play appropriate sound based on conditions
        if (hasLargeChange) {
            statChangeThrongSound.play(volume);
        } else if (hasLowStat) {
            lowStatThrongSound.play(volume);
        } else if (hasHighStat) {
            highStatThrongSound.play(volume);
        }

        // Update previous values
        prevHunger = hunger;
        prevHappiness = happiness;
        prevEnergy = energy;
        prevWellbeing = wellbeing;
    }

    public void playSleepingSound() {
        // Lower background music volume
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(isMuted ? 0f : SLEEPING_BG_VOLUME);
        }

        // Play sleeping sound at higher volume
        sleepingSound.play(SLEEPING_SOUND_VOLUME);
        isSleeping = true;
        sleepSoundTimer = 0f;
        sleepSoundDuration = 5f; // Duration of the sleeping sound in seconds
    }

    public void playBackgroundMusic(String filePath) {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(filePath));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(isMuted ? 0f : NORMAL_BG_VOLUME);
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
            backgroundMusic.setVolume(isMuted ? 0f : (isSleeping ? SLEEPING_BG_VOLUME : NORMAL_BG_VOLUME));
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(isMuted ? 0f : (isSleeping ? SLEEPING_BG_VOLUME : NORMAL_BG_VOLUME));
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
        if (clickMenuSound != null) {
            clickMenuSound.dispose();
            clickMenuSound = null;
        }
        if (clickActionSound != null) {
            clickActionSound.dispose();
            clickActionSound = null;
        }
        if (randomThrongSound != null) {
            randomThrongSound.dispose();
            randomThrongSound = null;
        }
        if (lowStatThrongSound != null) {
            lowStatThrongSound.dispose();
            lowStatThrongSound = null;
        }
        if (highStatThrongSound != null) {
            highStatThrongSound.dispose();
            highStatThrongSound = null;
        }
        if (statChangeThrongSound != null) {
            statChangeThrongSound.dispose();
            statChangeThrongSound = null;
        }
        if (sleepingSound != null) {
            sleepingSound.dispose();
            sleepingSound = null;
        }
    }
}