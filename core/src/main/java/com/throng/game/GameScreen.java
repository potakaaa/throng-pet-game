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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.throng.game.entity.Pet;
import com.throng.game.ui.PetStatsUI;
import com.badlogic.gdx.Input;
import com.throng.game.entity.Fruit;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.Color;

public class GameScreen implements Screen {
    private final ThrongGame game;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private final Texture backgroundTexture;
    private final Skin skin;
    private final Array<Fruit> fruits = new Array<>();
    private final Pet pet;
    private final PetStatsUI petStatsUI;

    private final float petScale = 0.3f;

    private float timeSinceManualInput = 0f;
    private static final float AUTO_BEHAVIOR_TIMEOUT = 1.5f;
    private int fruitInventory = 3; // Start with 3 fruits

    public GameScreen(final ThrongGame game) {
        this.game = game;

        // Camera setup
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // Stage setup
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        // Load assets
        backgroundTexture = new Texture("background/Grass_Sample.png");
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        petStatsUI = new PetStatsUI(stage, skin, new PetStatsUI.PetActionListener() {
            @Override
            public void onFeed() {
                if (fruitInventory > 0) {
                    float angle = (float)(Math.random() * Math.PI * 2); // random direction
                    float dropDistance = 200;
                    Vector2 dropPos = new Vector2(
                        pet.getPosition().x + (float)Math.cos(angle) * dropDistance,
                        pet.getPosition().y + (float)Math.sin(angle) * dropDistance
                    );

                    fruits.add(new Fruit(dropPos));
                    fruitInventory--;
                    System.out.println("Dropped fruit! Remaining: " + fruitInventory);
                } else {
                    System.out.println("No fruit left in inventory!");
                }
            }

            @Override
            public void onPlay() {
                pet.play();
            }

            @Override
            public void onSleep() {
                pet.sleep();
            }
        });


        pet = new Pet(
                new Vector2(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f),
                petStatsUI);
    }

    private void update(float delta) {
        // Handle WASD input for pet movement
        float dx = 0, dy = 0;
        boolean keyPressed = false;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += 1;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= 1;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= 1;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += 1;
            keyPressed = true;
        }
        if (keyPressed) {
            pet.manualMove(dx, dy, viewport.getWorldWidth(), viewport.getWorldHeight(), delta);
            timeSinceManualInput = 0f;
        } else {
            timeSinceManualInput += delta;
        }
        pet.suppressAutoBehavior = (!keyPressed && timeSinceManualInput < AUTO_BEHAVIOR_TIMEOUT);
        pet.update(delta, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (int i = fruits.size - 1; i >= 0; i--) {
            Fruit fruit = fruits.get(i);
            if (pet.getBounds().overlaps(fruit.getBounds())) {
                fruit.touch();
                pet.feed();
                fruits.removeIndex(i); // Remove the fruit from the array
                fruit.dispose(); // Free texture memory
            }
        }
    }

    private void drawPet() {
        TextureRegion frame = pet.getCurrentFrame();
        Vector2 pos = pet.getPosition();
        float width = frame.getRegionWidth() * petScale;
        float height = frame.getRegionHeight() * petScale;
        game.batch.draw(frame, pos.x - width / 2, pos.y - height / 2, width, height);
    }


    private void drawFruits() {
        for (Fruit fruit : fruits) {
            TextureRegion frame = fruit.getFrame();
            Vector2 pos = fruit.getPosition();
            float width = frame.getRegionWidth() * 1f;
            float height = frame.getRegionHeight() * 1f;
            game.batch.draw(frame, pos.x - width / 2, pos.y - height / 2, width, height);
        }
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
        // Draw background
        float bgWidth = backgroundTexture.getWidth();
        float bgHeight = backgroundTexture.getHeight();
        float scale = viewport.getWorldHeight() / bgHeight;
        float drawWidth = bgWidth * scale;
        float drawHeight = bgHeight * scale;
        float x = (viewport.getWorldWidth() - drawWidth) / 2f;
        float y = 0f;
        game.batch.draw(backgroundTexture, x, y, drawWidth, drawHeight);
        // Draw pet only
        drawPet();
        drawFruits();
        game.batch.end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        skin.dispose();
        stage.dispose();
        pet.dispose(); // Pet owns animation manager
    }
}
