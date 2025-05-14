package com.throng.game.entity.fruits;

import com.badlogic.gdx.math.Vector2;
import com.throng.game.entity.Pet;

public class AppleFruit extends Fruit {
    private final Pet pet;

    public AppleFruit(Vector2 position, Pet pet) {
        super(position, "fruits/apple/apple_default.png");
        this.pet = pet;
    }

    @Override
    public void applyEffect() {
        pet.eat(40, 10, 0, 2.1f);
    }
}

