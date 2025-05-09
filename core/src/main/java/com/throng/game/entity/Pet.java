package com.throng.game.entity;

import com.badlogic.gdx.math.Vector2;

public class Pet {
    public enum PetState {
        IDLE, WALKING, BLINKING, SLEEPING, EATING, PLAYING
    }

    private Vector2 position, targetPosition;
    private PetState currentState, previousState;
    private float stateTime;
    private float hunger, happiness, energy;
    private final float MAX_STAT = 100f;
    private final float STAT_DECAY_RATE = 5f;
    private boolean isWalking;
    private final float walkSpeed = 100f;

    public Pet(Vector2 startPos) {
        this.position = new Vector2(startPos);
        this.targetPosition = new Vector2(startPos);
        currentState = PetState.IDLE;
        previousState = PetState.IDLE;
        hunger = happiness = energy = MAX_STAT;
    }

    public void update(float delta, float screenWidth, float screenHeight) {
        stateTime += delta;
        hunger = Math.max(hunger - STAT_DECAY_RATE * delta, 0);
        happiness = Math.max(happiness - STAT_DECAY_RATE * delta, 0);
        energy = Math.max(energy - STAT_DECAY_RATE * delta, 0);

        previousState = currentState;

        if (currentState == PetState.IDLE || currentState == PetState.BLINKING) {
            if (Math.random() < 0.01)
                toggleBlink();
            if (Math.random() < 0.002 && !isWalking)
                startRandomWalk(screenWidth, screenHeight);
        }

        if (currentState == PetState.WALKING)
            updateWalking(delta);
    }

    private void toggleBlink() {
        currentState = (currentState == PetState.IDLE) ? PetState.BLINKING : PetState.IDLE;
        stateTime = 0;
    }

    private void updateWalking(float delta) {
        Vector2 direction = new Vector2(targetPosition).sub(position).nor();
        position.mulAdd(direction, walkSpeed * delta);

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

        currentState = PetState.WALKING;
        isWalking = true;
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

    // Getters
    public float getHunger() { return hunger; }
    public float getHappiness() { return happiness; }
    public float getEnergy() { return energy; }
    public Vector2 getPosition() { return position; }
    public PetState getState() { return currentState; }
    public float getStateTime() { return stateTime; }
}
