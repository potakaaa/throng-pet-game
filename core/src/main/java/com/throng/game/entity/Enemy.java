package com.throng.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.throng.game.entity.fruits.Fruit;
import com.throng.game.entity.fruits.FruitFactory;
import com.throng.game.animation.EnemyAnimationManager;
import com.throng.game.audio.EnemySoundManager;

public class Enemy extends Entity {
    private static final float MOVEMENT_SPEED = 120f;
    private static final float ATTACK_RANGE = 75f;
    private static final float ATTACK_DAMAGE = 10f;
    private static final float ATTACK_COOLDOWN = 1.5f;
    private static final float HURT_DURATION = 0.3f;

    private final EnemyAnimationManager animationManager;
    private float health;
    private float attackTimer;
    private float hurtTimer;
    private boolean isDead;
    private boolean isHurt;
    private final Pet targetPet;
    private boolean isMoving;

    public Enemy(Vector2 position, Pet targetPet) {
        super(position);
        this.targetPet = targetPet;
        this.animationManager = new EnemyAnimationManager();
        reset();
    }

    public void reset() {
        this.health = 100f;
        this.attackTimer = 0f;
        this.hurtTimer = 0f;
        this.isDead = false;
        this.isHurt = false;
        this.isMoving = false;
        if (this.position != null) {
            this.position.set(0, 0);
        }
        this.velocity.set(0, 0);
        this.facingLeft = false;
        if (this.animationManager != null) {
            this.animationManager.setState(EnemyAnimationManager.EnemyState.IDLE);
        }
    }

    public void spawn() {
        reset();
        EnemySoundManager.getInstance().playRandomSpawnSound();
    }

    public void setPosition(Vector2 newPosition) {
        this.position.set(newPosition);
    }

    public boolean isAnimationFinished() {
        return isDead && animationManager.isAnimationFinished();
    }

    @Override
    public void update(float delta, float worldWidth, float worldHeight) {
        if (isDead) {
            animationManager.setState(EnemyAnimationManager.EnemyState.DEATH);
            animationManager.update(delta);
            return;
        }

        // Update hurt timer
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) {
                isHurt = false;
            }
        }

        // Update attack cooldown
        attackTimer = Math.max(0, attackTimer - delta);

        // Only move and attack if not hurt
        if (!isHurt) {
            // Move towards the pet
            Vector2 direction = new Vector2(targetPet.getPosition()).sub(position).nor();
            velocity.set(direction).scl(MOVEMENT_SPEED);
            position.add(velocity.x * delta, velocity.y * delta);
            facingLeft = direction.x < 0;
            isMoving = velocity.len2() > 1f;

            // Check if in attack range
            if (position.dst(targetPet.getPosition()) <= ATTACK_RANGE && attackTimer <= 0) {
                attack(targetPet);
            }
        }

        // Update animation state
        if (isHurt) {
            animationManager.setState(EnemyAnimationManager.EnemyState.HURT);
        } else if (isMoving) {
            animationManager.setState(EnemyAnimationManager.EnemyState.WALKING);
        } else {
            animationManager.setState(EnemyAnimationManager.EnemyState.IDLE);
        }
        animationManager.update(delta);

        // Check if hurt animation finished
        if (isHurt && animationManager.isAnimationFinished()) {
            isHurt = false;
            hurtTimer = 0;
        }
    }

    private void attack(Pet target) {
        target.eat(-ATTACK_DAMAGE, -ATTACK_DAMAGE, -ATTACK_DAMAGE, 0f);
        attackTimer = ATTACK_COOLDOWN;
        EnemySoundManager.getInstance().playAttackSound();
    }

    public void takeDamage(float damage) {
        if (!isHurt && !isDead) {  // Only take damage if not already hurt
            health -= damage;
            if (health <= 0) {
                die();
            } else {
                isHurt = true;
                hurtTimer = HURT_DURATION;
                animationManager.setState(EnemyAnimationManager.EnemyState.HURT);
            }
        }
    }

    public void die() {
        isDead = true;
        animationManager.setState(EnemyAnimationManager.EnemyState.DEATH);
    }

    public boolean isDead() {
        return isDead;
    }

    @Override
    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationManager.getCurrentFrame();
        if (facingLeft && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (!facingLeft && frame.isFlipX()) {
            frame.flip(true, false);
        }
        return frame;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            position.x - animationManager.getWidth() / 2f,
            position.y - animationManager.getHeight() / 2f,
            animationManager.getWidth(),
            animationManager.getHeight()
        );
    }

    public Fruit dropFruit() {
        return FruitFactory.createRandomFruit(new Vector2(position), targetPet);
    }

    @Override
    public void dispose() {
        animationManager.dispose();
    }
}
