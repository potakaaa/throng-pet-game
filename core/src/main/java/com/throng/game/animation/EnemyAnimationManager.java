package com.throng.game.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;

public class EnemyAnimationManager implements Disposable {
    private static final float FRAME_DURATION = 0.15f;
    private static final float ENEMY_SCALE = 0.3f;
    private static final float HURT_ANIMATION_DURATION = 0.3f;
    private static final float DEATH_ANIMATION_DURATION = 0.5f; // Total death animation duration
    
    // Static texture cache shared by all instances
    private static final Map<String, Texture> textureCache = new HashMap<>();
    private static int instanceCount = 0;
    private static boolean texturesPreloaded = false;
    
    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> hurtAnimation;
    private final Animation<TextureRegion> deathAnimation;
    private float stateTime = 0;
    private EnemyState currentState = EnemyState.IDLE;
    private boolean isAnimationFinished = false;
    private final float width;
    private final float height;

    public enum EnemyState {
        IDLE,
        WALKING,
        HURT,
        DEATH
    }

    public EnemyAnimationManager() {
        instanceCount++;
        
        // Preload all textures on first instance
        if (!texturesPreloaded) {
            preloadTextures();
            texturesPreloaded = true;
        }
        
        // Load animations using cached textures
        idleAnimation = createAnimation("enemy/Idle/0_Skeleton_Crusader_Idle_%03d.png", 18, FRAME_DURATION);
        walkAnimation = createAnimation("enemy/Walking/0_Skeleton_Crusader_Walking_%03d.png", 24, FRAME_DURATION);
        hurtAnimation = createAnimation("enemy/Hurt/0_Skeleton_Crusader_Hurt_%03d.png", 12, HURT_ANIMATION_DURATION / 12f);
        deathAnimation = createAnimation("enemy/Dying/0_Skeleton_Crusader_Dying_%03d.png", 15, DEATH_ANIMATION_DURATION / 15f);

        // Calculate scaled dimensions based on the first frame of idle animation
        TextureRegion firstFrame = idleAnimation.getKeyFrame(0);
        this.width = firstFrame.getRegionWidth() * ENEMY_SCALE;
        this.height = firstFrame.getRegionHeight() * ENEMY_SCALE;
    }

    private void preloadTextures() {
        // Preload all animation textures
        preloadAnimationTextures("enemy/Idle/0_Skeleton_Crusader_Idle_%03d.png", 18);
        preloadAnimationTextures("enemy/Walking/0_Skeleton_Crusader_Walking_%03d.png", 24);
        preloadAnimationTextures("enemy/Hurt/0_Skeleton_Crusader_Hurt_%03d.png", 12);
        preloadAnimationTextures("enemy/Dying/0_Skeleton_Crusader_Dying_%03d.png", 15);
    }

    private void preloadAnimationTextures(String pathFormat, int frameCount) {
        for (int i = 0; i < frameCount; i++) {
            String filePath = String.format(pathFormat, i);
            if (!textureCache.containsKey(filePath) && Gdx.files.internal(filePath).exists()) {
                textureCache.put(filePath, new Texture(Gdx.files.internal(filePath)));
            }
        }
    }

    private Animation<TextureRegion> createAnimation(String pathFormat, int frameCount, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            String filePath = String.format(pathFormat, i);
            try {
                // Try to get texture from cache first
                Texture texture = textureCache.get(filePath);
                if (texture == null) {
                    // If not in cache, load and store it
                    if (Gdx.files.internal(filePath).exists()) {
                        texture = new Texture(Gdx.files.internal(filePath));
                        textureCache.put(filePath, texture);
                    } else {
                        Gdx.app.error("EnemyAnimationManager", "File not found: " + filePath);
                        continue;
                    }
                }
                frames.add(new TextureRegion(texture));
            } catch (Exception e) {
                Gdx.app.error("EnemyAnimationManager", "Error loading frame " + i + ": " + e.getMessage());
            }
        }

        if (frames.size == 0) {
            // Create a small default texture if no frames were loaded
            String defaultKey = "default_enemy_texture";
            Texture defaultTexture = textureCache.get(defaultKey);
            if (defaultTexture == null) {
                defaultTexture = new Texture(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
                textureCache.put(defaultKey, defaultTexture);
            }
            frames.add(new TextureRegion(defaultTexture));
        }

        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta) {
        stateTime += delta;

        // Check if hurt or death animations are finished
        if (currentState == EnemyState.HURT) {
            isAnimationFinished = hurtAnimation.isAnimationFinished(stateTime);
        } else if (currentState == EnemyState.DEATH) {
            isAnimationFinished = deathAnimation.isAnimationFinished(stateTime);
        }
    }

    public void setState(EnemyState newState) {
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0;
            isAnimationFinished = false;
        }
    }

    public TextureRegion getCurrentFrame() {
        Animation<TextureRegion> currentAnimation;
        boolean looping = true;

        switch (currentState) {
            case WALKING:
                currentAnimation = walkAnimation;
                break;
            case HURT:
                currentAnimation = hurtAnimation;
                looping = false;
                break;
            case DEATH:
                currentAnimation = deathAnimation;
                looping = false;
                break;
            case IDLE:
            default:
                currentAnimation = idleAnimation;
                break;
        }

        return currentAnimation.getKeyFrame(stateTime, looping);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isAnimationFinished() {
        return isAnimationFinished;
    }

    @Override
    public void dispose() {
        instanceCount--;
        if (instanceCount == 0) {
            // Only dispose textures when the last instance is disposed
            for (Texture texture : textureCache.values()) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            textureCache.clear();
        }
    }
}
