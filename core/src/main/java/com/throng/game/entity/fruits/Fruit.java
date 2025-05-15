package com.throng.game.entity.fruits;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Fruit {
    protected static final float SIZE = 64f;
    protected static final float COLLISION_SIZE = 64f;

    protected final Vector2 position;
    protected final Texture texture;
    protected final TextureRegion textureRegion;
    protected Runnable onTouchCallback;

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
}
