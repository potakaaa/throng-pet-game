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
        animations.put("IDLE", loadAnimation("sprite/Idle", 8, 0.25f));
        animations.put("BLINKING", loadAnimation("sprite/Idle Blinking", 3, 0.2f));
        animations.put("WALKING", loadAnimation("sprite/Walking", 4, 0.2f));
        animations.put("SLEEPING", loadAnimation("sprite/Sleeping", 3, 0.3f));
        animations.put("PLAYING", loadAnimation("sprite/Playing", 5, 0.2f, "Playing"));
        animations.put("EATING", loadAnimation("sprite/Eating", 7, 0.3f, "Eating"));
        animations.put("DEAD", loadAnimation("sprite/Dying", 4, 0.5f, "Dying", Animation.PlayMode.NORMAL));
    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration) {
        return loadAnimation(folderPath, frameCount, frameDuration, "", Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration,
            String fileNameOverride) {
        return loadAnimation(folderPath, frameCount, frameDuration, fileNameOverride, Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration,
            String fileNameOverride, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();

        String baseName = fileNameOverride.isEmpty()
                ? folderPath.substring(folderPath.lastIndexOf("/") + 1)
                : fileNameOverride;

        for (int i = 0; i < frameCount; i++) {
            String index = String.format("%03d", i);
            String filePath = folderPath + "/0_Throng_" + baseName + "_" + index + ".png";
            Texture texture = new Texture(Gdx.files.internal(filePath));
            frames.add(new TextureRegion(texture));
        }

        return new Animation<>(frameDuration, frames, playMode);
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
