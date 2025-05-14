package com.throng.game.entity.fruits;

import com.badlogic.gdx.math.Vector2;
import com.throng.game.entity.Pet;
import com.throng.game.entity.fruits.Fruit;

public class BananaFruit extends Fruit {
    private final Pet pet;

    public BananaFruit(Vector2 position, Pet pet) {
        super(position, "fruits/banana/banana_default.png");
        this.pet = pet;
    }

    @Override
    public void applyEffect() {
        // Boost happiness +20, no hunger/energy
        pet.eat(20f, 20f, 0f, 2.1f); // Duration matches animation length (7 frames * 0.3s)
    }
}
