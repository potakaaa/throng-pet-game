package com.throng.game.entity;

public interface PetStatObserver {
    void updateBars(float hunger, float happiness, float energy, float wellbeing);
    void onPetDied();
}
