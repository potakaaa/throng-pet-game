package com.throng.game.entity.fruits;

import com.badlogic.gdx.math.Vector2;
import com.throng.game.entity.Pet;
import com.throng.game.entity.fruits.Fruit;

public class EnergyFruit extends Fruit {
    private final Pet pet;

    public EnergyFruit(Vector2 position, Pet pet) {
        super(position, "fruits/energy/energy_default.png"); // replace with your actual texture path
        this.pet = pet;
    }

    @Override
    public void applyEffect() {
        // Boost energy +30, no hunger/happiness, short duration
        pet.eat(10f, 0f, 30f, 1.5f);
    }
}
