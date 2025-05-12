package com.throng.game.entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

public class Fruit {
    private static final float SIZE = 32f;
    private final Vector2 position;
    private final Texture texture;
    private Runnable onTouchCallback;

    public Fruit(Vector2 position) {
        this.position = position;
        this.texture = new Texture("star/Active.png"); // Add your fruit texture
    }

    public Vector2 getPosition() {
        return position;
    }

    public Texture getTexture() {
        return texture;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x - SIZE/2, position.y - SIZE/2, SIZE, SIZE);
    }
    public TextureRegion getFrame() {
        return new TextureRegion(texture); // Replace with your actual field holding the fruit's texture
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
