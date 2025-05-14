package com.throng.game.entity.fruits;

import com.badlogic.gdx.math.Vector2;
import com.throng.game.entity.Pet;

import java.util.Random;

public class FruitFactory {
    private static final Random random = new Random();

    public static Fruit createRandomFruit(Vector2 position, Pet pet) {
        int pick = random.nextInt(3); // update if you add more types

        if (pick == 0) {
            return new AppleFruit(position, pet);
        } else if (pick == 1) {
            return new EnergyFruit(position, pet);
        } else if (pick == 2) {
            return new DeathFruit(position, pet);
        } else {
            return new BananaFruit(position, pet); // fallback
        }
    }
}
