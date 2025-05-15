package com.throng.game.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.throng.game.entity.fruits.Fruit;

public class EnemyManager {
    private static final float SPAWN_INTERVAL = 5f;
    private static final float MIN_SPAWN_DISTANCE = 400f;
    private static final float MAX_SPAWN_DISTANCE = 600f;
    private static final int MAX_ENEMIES = 5;
    private static final int POOL_INITIAL_CAPACITY = 10;

    private final Array<Enemy> activeEnemies;
    private final Pool<Enemy> enemyPool;
    private final Array<Fruit> fruits;
    private float spawnTimer;
    private final Pet pet;

    public EnemyManager(Pet pet, Array<Fruit> fruits) {
        this.pet = pet;
        this.fruits = fruits;
        this.spawnTimer = SPAWN_INTERVAL;
        this.activeEnemies = new Array<>(false, POOL_INITIAL_CAPACITY);
        
        // Create enemy pool
        this.enemyPool = new Pool<Enemy>(POOL_INITIAL_CAPACITY) {
            @Override
            protected Enemy newObject() {
                return new Enemy(new Vector2(), pet);
            }

            @Override
            protected void reset(Enemy enemy) {
                enemy.reset();
            }
        };
    }

    public void update(float delta, float worldWidth, float worldHeight) {
        // Update spawn timer
        spawnTimer -= delta;
        if (spawnTimer <= 0 && activeEnemies.size < MAX_ENEMIES) {
            spawnEnemy(worldWidth, worldHeight);
            spawnTimer = SPAWN_INTERVAL;
        }

        // Update existing enemies
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            Enemy enemy = activeEnemies.get(i);
            enemy.update(delta, worldWidth, worldHeight);

            if (enemy.isDead() && enemy.isAnimationFinished()) {
                // Drop a fruit when enemy dies
                Fruit droppedFruit = enemy.dropFruit();
                if (droppedFruit != null) {
                    fruits.add(droppedFruit);
                }
                // Return enemy to pool
                enemyPool.free(enemy);
                activeEnemies.removeIndex(i);
            }
        }
    }

    private void spawnEnemy(float worldWidth, float worldHeight) {
        float angle = MathUtils.random(MathUtils.PI2);
        float distance = MathUtils.random(MIN_SPAWN_DISTANCE, MAX_SPAWN_DISTANCE);

        float spawnX = pet.getPosition().x + MathUtils.cos(angle) * distance;
        float spawnY = pet.getPosition().y + MathUtils.sin(angle) * distance;

        // Clamp spawn position to world bounds
        spawnX = MathUtils.clamp(spawnX, 100, worldWidth - 100);
        spawnY = MathUtils.clamp(spawnY, 100, worldHeight - 100);

        // Get enemy from pool and initialize it
        Enemy enemy = enemyPool.obtain();
        enemy.setPosition(new Vector2(spawnX, spawnY));
        enemy.spawn();
        activeEnemies.add(enemy);
    }

    public Array<Enemy> getEnemies() {
        return activeEnemies;
    }

    public void dispose() {
        for (Enemy enemy : activeEnemies) {
            enemy.dispose();
        }
        activeEnemies.clear();
        enemyPool.clear();
    }
}
