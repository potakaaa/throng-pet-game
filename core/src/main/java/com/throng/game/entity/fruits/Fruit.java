package com.throng.game.entity.fruits;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Fruit {
    protected static final float SIZE = 64f;
    protected static final float COLLISION_SIZE = 64f;
    protected static final float EXPIRATION_TIME = 30f; // Time in seconds before fruit expires
    protected static final float BLINK_START_TIME = 5f; // Time before expiration when fruit starts blinking

    protected final Vector2 position;
    protected final Texture texture;
    protected final TextureRegion textureRegion;
    protected Runnable onTouchCallback;
    protected float timeAlive = 0f;
    protected boolean isExpired = false;

    public Fruit(Vector2 position, String texturePath) {
        this.position = new Vector2(position);
        this.texture = new Texture(texturePath);
        this.textureRegion = new TextureRegion(texture);
    }

    public Vector2 getPosition() {
        return position;
    }

    public TextureRegion getFrame() {
        return textureRegion;
    }

    public float getSize() {
        return SIZE;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x - COLLISION_SIZE / 2, position.y - COLLISION_SIZE / 2, COLLISION_SIZE, COLLISION_SIZE);
    }

    public void dispose() {
        texture.dispose();
    }

    public void setOnTouch(Runnable callback) {
        this.onTouchCallback = callback;
    }

    public void touch() {
        if (onTouchCallback != null) {
            onTouchCallback.run();
        }
        applyEffect();
    }

    public abstract void applyEffect();

    public void update(float delta) {
        timeAlive += delta;
        if (timeAlive >= EXPIRATION_TIME) {
            isExpired = true;
        }
    }

    public boolean isExpired() {
        return isExpired;
    }

    public boolean shouldBlink() {
        return timeAlive >= (EXPIRATION_TIME - BLINK_START_TIME) && 
               ((int)(timeAlive * 4) % 2 == 0); // Blink twice per second
    }
}
