package com.throng.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.throng.game.animation.AnimationManager;
import com.throng.game.entity.Pet;
import com.throng.game.ui.PetStatsUI;

public class GameScreen implements Screen {
    private final ThrongGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private final Texture backgroundTexture;
    private final Skin skin;
    private final Pet pet;
    private final PetStatsUI petStatsUI;
    private final AnimationManager animationManager;

    private final float petScale = 0.3f;

    public GameScreen(final ThrongGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        backgroundTexture = new Texture("background/background_1/background 1.png");
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        pet = new Pet(new Vector2(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2));
        animationManager = new AnimationManager();

        petStatsUI = new PetStatsUI(stage, skin, new PetStatsUI.PetActionListener() {
            @Override public void onFeed() { pet.feed(); }
            @Override public void onPlay() { pet.play(); }
            @Override public void onSleep() { pet.sleep(); }
        });
    }

    private void update(float delta) {
        pet.update(delta, viewport.getWorldWidth(), viewport.getWorldHeight());
        petStatsUI.updateBars(pet.getHunger(), pet.getHappiness(), pet.getEnergy());
    }

    private void drawPet() {
        TextureRegion frame = animationManager.get(pet.getState().toString()).getKeyFrame(pet.getStateTime());
        float width = frame.getRegionWidth() * petScale;
        float height = frame.getRegionHeight() * petScale;
        Vector2 pos = pet.getPosition();
        game.batch.draw(frame, pos.x - width / 2, pos.y - height / 2, width, height);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // clear to black
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        game.batch.setColor(1f, 1f, 1f, 1f); // ← critical: reset tint
        game.batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        drawPet(); // assumes drawPet() also uses correct tint

        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }


    @Override public void show() {}
    @Override public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        backgroundTexture.dispose();
        skin.dispose();
        stage.dispose();
        animationManager.dispose();
    }
}
