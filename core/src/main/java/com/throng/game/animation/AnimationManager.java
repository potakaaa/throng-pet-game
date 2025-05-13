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
        animations.put("SLEEPING", loadAnimation("sprite/hurt", 11, 0.1f));
        animations.put("PLAYING", loadAnimation("sprite/Slashing Air", 11, 0.1f, "Slashing in The Air"));
        animations.put("EATING", loadAnimation("sprite/Throwing Air", 11, 0.1f, "Throwing in The Air"));
    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration) {
        return loadAnimation(folderPath, frameCount, frameDuration, "");
    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration, String fileNameOverride) {
        Array<TextureRegion> frames = new Array<>();

        String baseName = fileNameOverride.isEmpty()
            ? folderPath.substring(folderPath.lastIndexOf("/") + 1)
            : fileNameOverride;

        for (int i = 0; i < frameCount; i++) {
            String index = String.format("%03d", i);
            String filePath = folderPath + "/0_Dark_Oracle_" + baseName + "_" + index + ".png";
            Texture texture = new Texture(Gdx.files.internal(filePath));
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
