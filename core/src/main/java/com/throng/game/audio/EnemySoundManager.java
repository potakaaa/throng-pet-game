package com.throng.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;

public class EnemySoundManager implements Disposable {
    private static EnemySoundManager instance;
    private final Sound attackSound;
    private final Array<Sound> spawnSounds;
    
    private float volume = 0.5f;
    private static final float MIN_TIME_BETWEEN_SOUNDS = 100f; // Changed to milliseconds
    private long lastAttackTime = 0;
    private long lastSpawnTime = 0;

    private EnemySoundManager() {
        // Load attack sound effect
        attackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bite-effect.mp3"));
        
        // Load spawn sound effects
        spawnSounds = new Array<>(4);
        for (int i = 1; i <= 4; i++) {
            spawnSounds.add(Gdx.audio.newSound(Gdx.files.internal(
                String.format("sounds/enemy/Enemy-%03d.mp3", i))));
        }
    }

    public static EnemySoundManager getInstance() {
        if (instance == null) {
            instance = new EnemySoundManager();
        }
        return instance;
    }

    public void playAttackSound() {
        long currentTime = TimeUtils.millis();
        if (!AudioManager.getInstance().isMuted() && currentTime - lastAttackTime >= MIN_TIME_BETWEEN_SOUNDS) {
            attackSound.play(volume);
            lastAttackTime = currentTime;
        }
    }

    public void playRandomSpawnSound() {
        long currentTime = TimeUtils.millis();
        if (!AudioManager.getInstance().isMuted() && currentTime - lastSpawnTime >= MIN_TIME_BETWEEN_SOUNDS) {
            int randomIndex = (int)(Math.random() * spawnSounds.size);
            spawnSounds.get(randomIndex).play(volume);
            lastSpawnTime = currentTime;
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    @Override
    public void dispose() {
        attackSound.dispose();
        for (Sound sound : spawnSounds) {
            sound.dispose();
        }
        spawnSounds.clear();
    }
} 