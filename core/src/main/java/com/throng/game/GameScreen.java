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

    private final float petScale = 0.3f;

    public GameScreen(final ThrongGame game) {
        this.game = game;

        // Camera setup
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // Stage setup
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        // Load assets
        backgroundTexture = new Texture("background/background_1/background 1.png");
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        // UI and pet
        petStatsUI = new PetStatsUI(stage, skin, new PetStatsUI.PetActionListener() {
            @Override public void onFeed() { pet.feed(); }
            @Override public void onPlay() { pet.play(); }
            @Override public void onSleep() { pet.sleep(); }
        });

        pet = new Pet(
            new Vector2(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f),
            petStatsUI
        );
    }

    private void update(float delta) {
        pet.update(delta, viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    private void drawPet() {
        TextureRegion frame = pet.getCurrentFrame();
        Vector2 pos = pet.getPosition();

        float width = frame.getRegionWidth() * petScale;
        float height = frame.getRegionHeight() * petScale;

        game.batch.draw(frame, pos.x - width / 2, pos.y - height / 2, width, height);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.setColor(1f, 1f, 1f, 1f);
        game.batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        drawPet();
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        skin.dispose();
        stage.dispose();
        pet.dispose(); // Pet owns animation manager
    }
}
