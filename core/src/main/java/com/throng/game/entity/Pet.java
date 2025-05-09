package com.throng.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.throng.game.animation.AnimationManager;
import com.throng.game.ui.PetStatsUI;

public class Pet {
    public enum PetState {
        IDLE, WALKING, BLINKING, SLEEPING, EATING, PLAYING
    }

    private final Vector2 position;
    private final Vector2 targetPosition;

    private PetState currentState;
    private PetState previousState;

    private float stateTime;

    private float hunger, happiness, energy;
    private static final float MAX_STAT = 100f;
    private static final float STAT_DECAY_RATE = 5f;

    private boolean isWalking;
    private static final float WALK_SPEED = 100f;

    private final AnimationManager animationManager;
    private final PetStatsUI statsUI;

    public Pet(Vector2 startPos, PetStatsUI statsUI) {
        this.position = new Vector2(startPos);
        this.targetPosition = new Vector2(startPos);
        this.statsUI = statsUI;

        this.animationManager = new AnimationManager();
        this.currentState = PetState.IDLE;
        this.previousState = PetState.IDLE;
        this.hunger = MAX_STAT;
        this.happiness = MAX_STAT;
        this.energy = MAX_STAT;
        this.stateTime = 0;
    }

    public void update(float delta, float screenWidth, float screenHeight) {
        stateTime += delta;
        decayStats(delta);

        updateBehavior(screenWidth, screenHeight, delta);

        // Sync UI
        statsUI.updateBars(hunger, happiness, energy);
    }

    private void decayStats(float delta) {
        hunger = Math.max(hunger - STAT_DECAY_RATE * delta, 0);
        happiness = Math.max(happiness - STAT_DECAY_RATE * delta, 0);
        energy = Math.max(energy - STAT_DECAY_RATE * delta, 0);
    }

    private void updateBehavior(float screenWidth, float screenHeight, float delta) {
        previousState = currentState;

        if (currentState == PetState.IDLE || currentState == PetState.BLINKING) {
            if (Math.random() < 0.01) toggleBlink();
            if (Math.random() < 0.002 && !isWalking) startRandomWalk(screenWidth, screenHeight);
        }

        if (currentState == PetState.WALKING) updateWalking(delta);
    }

    private void toggleBlink() {
        currentState = (currentState == PetState.IDLE) ? PetState.BLINKING : PetState.IDLE;
        stateTime = 0;
    }

    private void updateWalking(float delta) {
        Vector2 direction = new Vector2(targetPosition).sub(position).nor();
        position.mulAdd(direction, WALK_SPEED * delta);

        if (position.dst(targetPosition) < 5f) {
            isWalking = false;
            currentState = PetState.IDLE;
            stateTime = 0;
        }
    }

    public void startRandomWalk(float screenWidth, float screenHeight) {
        float paddingX = screenWidth * 0.2f;
        float paddingY = screenHeight * 0.2f;

        targetPosition.x = paddingX + (float) Math.random() * (screenWidth - 2 * paddingX);
        targetPosition.y = paddingY + (float) Math.random() * (screenHeight - 2 * paddingY);

        isWalking = true;
        currentState = PetState.WALKING;
        stateTime = 0;
    }

    public void feed() {
        hunger = Math.min(MAX_STAT, hunger + 30);
        currentState = PetState.IDLE;
        stateTime = 0;
    }

    public void play() {
        happiness = Math.min(MAX_STAT, happiness + 25);
        energy = Math.max(0, energy - 10);
        currentState = PetState.WALKING;
        stateTime = 0;
    }

    public void sleep() {
        energy = Math.min(MAX_STAT, energy + 50);
        currentState = PetState.IDLE;
        stateTime = 0;
    }

    public TextureRegion getCurrentFrame() {
        return animationManager.get(currentState.toString()).getKeyFrame(stateTime);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        animationManager.dispose();
    }
}
