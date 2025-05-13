package com.throng.game.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

public class DraggablePetActor extends Actor {
    private final Pet pet;
    private final float scale = 0.3f;
    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;

    public DraggablePetActor(Pet pet) {
        this.pet = pet;

        Vector2 pos = pet.getPosition();
        float width = pet.getCurrentFrame().getRegionWidth() * scale;
        float height = pet.getCurrentFrame().getRegionHeight() * scale;

        setSize(width, height);
        setOrigin(width / 2f, height / 2f);
        setPosition(pos.x - width / 2f, pos.y - height / 2f);

        addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragging = true;
                dragOffsetX = x;
                dragOffsetY = y;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                float newX = getX() + x - dragOffsetX;
                float newY = getY() + y - dragOffsetY;

                float stageWidth = getStage().getWidth();
                float stageHeight = getStage().getHeight();

                // Clamp new position to stay within stage bounds
                newX = Math.max(0, Math.min(stageWidth - getWidth(), newX));
                newY = Math.max(0, Math.min(stageHeight - getHeight(), newY));

                setPosition(newX, newY);
                pet.setPosition(new Vector2(newX + getWidth() / 2f, newY + getHeight() / 2f));
                pet.suppressAutoBehavior = true;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                dragging = false;
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!dragging) {
            Vector2 pos = pet.getPosition();
            setPosition(pos.x - getWidth() / 2f, pos.y - getHeight() / 2f);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(
            pet.getCurrentFrame(),
            getX(), getY(),
            getWidth(), getHeight()
        );
    }
}
