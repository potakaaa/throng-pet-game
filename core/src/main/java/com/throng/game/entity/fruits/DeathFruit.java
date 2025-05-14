package com.throng.game.entity.fruits;

import com.badlogic.gdx.math.Vector2;
import com.throng.game.entity.Pet;

public class DeathFruit extends Fruit {
    private final Pet pet;

    public DeathFruit(Vector2 position, Pet pet) {
        super(position, "fruits/death/death_default.png"); // replace with your actual texture path
        this.pet = pet;
    }

    @Override
    public void applyEffect() {
        pet.eat(-100f, -100f, -100f, 0f);
    }
}
