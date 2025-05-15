package com.throng.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    protected final Vector2 position;
    protected final Vector2 velocity;
    protected boolean facingLeft;
    protected float health;
    protected boolean isDead;
    protected static final float MAX_HEALTH = 100f;
    protected static final float COLLISION_SIZE = 64f;

    public Entity(Vector2 position) {
        this.position = new Vector2(position);
        this.velocity = new Vector2();
        this.facingLeft = false;
        this.health = MAX_HEALTH;
        this.isDead = false;
    }

    public abstract void update(float delta, float worldWidth, float worldHeight);

    public abstract TextureRegion getCurrentFrame();

    public abstract void dispose();

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0 && !isDead) {
            die();
        }
    }

    protected void die() {
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    public Vector2 getPosition() {
        return position;
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
            collisionHeight
        );
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public float getHealth() {
        return health;
    }
} 