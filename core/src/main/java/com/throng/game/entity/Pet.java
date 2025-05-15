package com.throng.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.throng.game.animation.AnimationManager;
import com.throng.game.ui.PetStatsUI;

public class Pet {

    public enum PetState {
        IDLE, WALKING, BLINKING, SLEEPING, EATING, PLAYING, DEAD
    }

    private final Vector2 position;
    private final Vector2 targetPosition;
    private boolean facingLeft = false;

    private PetState currentState;
    private PetState previousState;

    private float stateTime;
    private float stateTimer;
    private float stateDuration;

    private float hunger, happiness, energy, wellbeing;
    private static final float MAX_STAT = 100f;
    private static final float BASE_DECAY_RATE = 1f;

    private boolean isWalking;
    private static final float WALK_SPEED = 100f;

    private final AnimationManager animationManager;
    private PetStatObserver statsObserver;

    private static final float MANUAL_MOVE_SPEED = 200f;
    private boolean manualControl = false;
    private boolean moving = false;

    public boolean suppressAutoBehavior = false;

    // Timed action tracking
    private float startHunger, startHappiness, startEnergy;
    private float hungerGain, happinessGain, energyGain;

    public Pet(Vector2 startPos, PetStatObserver statsObserver) {
        this.position = new Vector2(startPos);
        this.targetPosition = new Vector2(startPos);
        this.statsObserver = statsObserver;

        this.animationManager = new AnimationManager();
        this.currentState = PetState.IDLE;
        this.previousState = PetState.IDLE;
        this.hunger = MAX_STAT;
        this.happiness = MAX_STAT;
        this.energy = MAX_STAT;
        this.wellbeing = MAX_STAT / 2f;
        this.stateTime = 0;
        this.stateTimer = 0;
        this.stateDuration = 0;
    }

    public void update(float delta, float screenWidth, float screenHeight) {
        if (currentState == PetState.DEAD) {
            return;
        }

        stateTime += delta;
        stateTimer += delta;

        handleTimedActions();
        decayStats(delta);

        if (!manualControl && !isInTimedAction()) {
            updateBehavior(screenWidth, screenHeight, delta);
        } else if (!moving && !isInTimedAction()) {
            currentState = PetState.IDLE;
        }

        manualControl = false;

        updateWellbeing(delta);

        if (statsObserver != null) {
            statsObserver.updateBars(hunger, happiness, energy, wellbeing);
        }
    }

    private void handleTimedActions() {
        if (!isInTimedAction())
            return;

        float t = Math.min(stateTimer / stateDuration, 1f);

        switch (currentState) {
            case EATING:
                hunger = Math.min(MAX_STAT, startHunger + hungerGain * t);
                happiness = Math.min(MAX_STAT, startHappiness + happinessGain * t);
                energy = Math.min(MAX_STAT, startEnergy + energyGain * t);
                break;

            case SLEEPING:
                energy = Math.min(MAX_STAT, startEnergy + energyGain * t);
                // hunger & happiness freeze — handled in decayStats()
                break;

            case PLAYING:
                happiness = Math.min(MAX_STAT, startHappiness + happinessGain * t);
                // energy & hunger decay faster — handled in decayStats()
                break;
        }

        if (stateTimer >= stateDuration && currentState != PetState.EATING) {
            currentState = PetState.IDLE;
            stateTime = 0;
            stateTimer = 0;
            stateDuration = 0;
        } else if (stateTimer >= stateDuration) {
            // For eating, we want to ensure the animation completes
            if (stateTime >= animationManager.get("EATING").getAnimationDuration()) {
                currentState = PetState.IDLE;
                stateTime = 0;
                stateTimer = 0;
                stateDuration = 0;
            }
        }
    }

    private void updateWellbeing(float delta) {
        float average = (hunger + happiness + energy) / 3f;

        if (average >= 70f) {
            wellbeing += 5f * delta; // gain if well maintained
        } else if (average >= 40f) {
            wellbeing += 1f * delta; // slow gain
        } else if (average >= 20f) {
            wellbeing -= 2f * delta; // slow drop
        } else {
            wellbeing -= 5f * delta; // bad neglect
        }

        wellbeing = Math.max(0f, Math.min(MAX_STAT, wellbeing));

        if (wellbeing == 0f) {
            die();
        }
    }
    private void die() {
        cancelTimedAction();
        isWalking = false;
        manualControl = false;
        currentState = PetState.DEAD;
        stateTime = 0;

        if (statsObserver != null) {
            statsObserver.onPetDied();
        }
    }

    public boolean isDead() {
        return currentState == PetState.DEAD;
    }


    private boolean isInTimedAction() {
        return currentState == PetState.SLEEPING || currentState == PetState.EATING || currentState == PetState.PLAYING;
    }

    private void decayStats(float delta) {
        switch (currentState) {
            case SLEEPING:
                // hunger & happiness frozen
                break;
            case PLAYING:
                // faster decay
                hunger = Math.max(hunger - (BASE_DECAY_RATE * 15f / 5f) * delta, 0);
                energy = Math.max(energy - (BASE_DECAY_RATE * 20f / 5f) * delta, 0);
                break;
            case EATING:
                // freeze decay during eating
                break;
            default:
                hunger = Math.max(hunger - BASE_DECAY_RATE * delta, 0);
                happiness = Math.max(happiness - BASE_DECAY_RATE * delta, 0);
                energy = Math.max(energy - BASE_DECAY_RATE * delta, 0);
                break;
        }
    }

    private void updateBehavior(float screenWidth, float screenHeight, float delta) {
        previousState = currentState;
        if (suppressAutoBehavior)
            return;

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
        position.mulAdd(direction, WALK_SPEED * delta);
        facingLeft = direction.x < 0;

        if (position.dst(targetPosition) < 5f) {
            isWalking = false;
            currentState = PetState.IDLE;
            stateTime = 0;
        }
    }

    public void startRandomWalk(float screenWidth, float screenHeight) {
        float padding = 50f;
        
        targetPosition.x = padding + (float) Math.random() * (screenWidth - 2 * padding);
        targetPosition.y = padding + (float) Math.random() * (screenHeight - 2 * padding);

        isWalking = true;
        currentState = PetState.WALKING;
        stateTime = 0;
    }

    public void play() {
        if (isDead()) return;

        cancelTimedAction();

        startHappiness = happiness;
        startEnergy = energy;

        happinessGain = Math.min(25, MAX_STAT - happiness);
        energyGain = 0; // real energy loss handled in decay

        currentState = PetState.PLAYING;
        stateTime = 0;
        stateTimer = 0;
        stateDuration = 20f;
    }

    public void sleep() {
        if (isDead()) return;

        cancelTimedAction();

        startEnergy = energy;
        startHunger = hunger;
        startHappiness = happiness;

        energyGain = Math.min(100, MAX_STAT - energy);

        currentState = PetState.SLEEPING;
        stateTime = 0;
        stateTimer = 0;
        stateDuration = 60f;
    }

    public void eat(float hungerBoost, float happinessBoost, float energyBoost, float duration) {
        if (isDead()) return;
        
        cancelTimedAction();

        startHunger = hunger;
        startHappiness = happiness;
        startEnergy = energy;

        hungerGain = hungerBoost;
        happinessGain = happinessBoost;
        energyGain = energyBoost;

        currentState = PetState.EATING;
        stateTime = 0;
        stateTimer = 0;
        stateDuration = duration;
    }


    private void cancelTimedAction() {
        stateDuration = 0;
        stateTimer = 0;
        stateTime = 0;
        currentState = PetState.IDLE;
    }

    public void manualMove(float dx, float dy, float screenWidth, float screenHeight, float delta) {
        if (isInTimedAction())
            cancelTimedAction();

        manualControl = true;
        moving = (dx != 0 || dy != 0);

        if (moving) {
            currentState = PetState.WALKING;
            facingLeft = dx < 0;
            float moveAmount = MANUAL_MOVE_SPEED * delta;
            float newX = position.x + dx * moveAmount;
            float newY = position.y + dy * moveAmount;

            // Simple boundary check with fixed padding
            float padding = 50f;
            newX = Math.max(padding, Math.min(screenWidth - padding, newX));
            newY = Math.max(padding, Math.min(screenHeight - padding, newY));

            position.set(newX, newY);
        } else {
            currentState = PetState.IDLE;
        }
    }

    public void setPosition(Vector2 newPosition) {
        position.set(newPosition);
        targetPosition.set(newPosition);
    }

    public TextureRegion getCurrentFrame() {
        if (animationManager.get(currentState.toString()) != null) {
            return animationManager.get(currentState.toString()).getKeyFrame(stateTime);
        } else if (animationManager.get("IDLE") != null) {
            return animationManager.get("IDLE").getKeyFrame(stateTime);
        } else {
            return new TextureRegion(); // fallback
        }
    }

    public Rectangle getBounds() {
        float width = getCurrentFrame().getRegionWidth() * 0.3f;
        float height = getCurrentFrame().getRegionHeight() * 0.3f;
        float collisionWidth = width / 3f;
        float collisionHeight = height / 3f;
        return new Rectangle(
                position.x - collisionWidth / 2,
                position.y - collisionHeight / 2,
                collisionWidth,
                collisionHeight);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        animationManager.dispose();
    }

    public void setStatsObserver(PetStatsUI statsObserver) {
        this.statsObserver = statsObserver;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}
