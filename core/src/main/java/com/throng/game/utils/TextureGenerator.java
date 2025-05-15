package com.throng.game.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;

public class TextureGenerator {
    public static Texture generateEnemyTexture() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        
        // Set background transparent
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // Draw enemy body (red)
        pixmap.setColor(Color.RED);
        pixmap.fillCircle(32, 32, 24);
        
        // Draw eyes (white with black pupils)
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(24, 24, 6);
        pixmap.fillCircle(40, 24, 6);
        
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(24, 24, 2);
        pixmap.fillCircle(40, 24, 2);
        
        // Draw angry eyebrows
        pixmap.setColor(Color.BLACK);
        pixmap.drawLine(18, 20, 28, 16);
        pixmap.drawLine(34, 16, 44, 20);
        
        // Create texture from pixmap
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        
        return texture;
    }
} 