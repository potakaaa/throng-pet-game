package com.throng.game.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

public class Fruit {
    private static final float SIZE = 64f;
    private static final float COLLISION_SIZE = 64f;
    private static final float SCREEN_WIDTH = 1280f;
    private static final float SCREEN_HEIGHT = 720f;
    private static final float PADDING = 100f;
    private final Vector2 position;
    private final Texture texture;
    private final TextureRegion textureRegion;
    private Runnable onTouchCallback;

    public Fruit(Vector2 position) {
        // screen bounds with padding
        float x = Math.max(SIZE / 2 + PADDING, Math.min(SCREEN_WIDTH - SIZE / 2 - PADDING, position.x));
        float y = Math.max(SIZE / 2 + PADDING, Math.min(SCREEN_HEIGHT - SIZE / 2 - PADDING, position.y));
        this.position = new Vector2(x, y);
        this.texture = new Texture("apple/apple_default.png");
        this.textureRegion = new TextureRegion(texture);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Texture getTexture() {
        return texture;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x - COLLISION_SIZE / 2, position.y - COLLISION_SIZE / 2, COLLISION_SIZE,
                COLLISION_SIZE);
    }

    public TextureRegion getFrame() {
        return textureRegion;
    }

    public float getSize() {
        return SIZE;
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
    }
}
