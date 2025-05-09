package com.throng.game.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class AnimationManager {
    private final HashMap<String, Animation<TextureRegion>> animations = new HashMap<>();

    public AnimationManager() {
        animations.put("IDLE", loadAnimation("sprite/Idle", 18, 0.1f));
        animations.put("BLINKING", loadAnimation("sprite/Idle Blinking", 18, 0.1f));
        animations.put("WALKING", loadAnimation("sprite/Walking", 18, 0.1f));
    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < frameCount; i++) {
            String index = String.format("%03d", i);
            String name = folderPath.substring(folderPath.lastIndexOf("/") + 1);
            Texture texture = new Texture(Gdx.files.internal(folderPath + "/0_Dark_Oracle_" + name + "_" + index + ".png"));
            frames.add(new TextureRegion(texture));
        }

        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    public Animation<TextureRegion> get(String key) {
        return animations.get(key);
    }

    public void dispose() {
        for (Animation<TextureRegion> animation : animations.values()) {
            for (TextureRegion frame : animation.getKeyFrames()) {
                frame.getTexture().dispose();
            }
        }
    }
}
