package com.throng.game;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {
    private final ThrongGame game;

    // Camera and view
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;

    // bg and ui
    private Texture backgroundTexture;
    private Skin skin;

    // pet animation
    private float stateTime;
    private HashMap<String, Animation<TextureRegion>> animations;
    private PetState currentState;
    private PetState previousState;
    private TextureRegion currentFrame;
    private Vector2 petPosition;
    private float petScale = 0.5f; // Scale of the pet

    // Pet status
    private float hunger;
    private float happiness;
    private float energy;
    private static final float MAX_STAT = 100f;
    private static final float STAT_DECAY_RATE = 5f;

    private ProgressBar hungerBar;
    private ProgressBar happinessBar;
    private ProgressBar energyBar;

    // pet state enum
    private enum PetState {
        IDLE,
        WALKING,
        BLINKING,
        SLEEPING,
        EATING,
        PLAYING
    }

    public GameScreen(final ThrongGame game) {
        this.game = game;

        // Initialize camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // set up ui stage
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        // load bg
        backgroundTexture = new Texture("background/background_1/background 1.png");

        // load skin
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        loadAnimations();

        // initialize pet position
        petPosition = new Vector2(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);

        // Initalize pet state
        currentState = PetState.IDLE;
        previousState = PetState.IDLE;
        stateTime = 0f;

        // Initialize pet stats
        hunger = MAX_STAT;
        happiness = MAX_STAT;
        energy = MAX_STAT;

        createUI();

    }

    private void loadAnimations() {
        animations = new HashMap<>();

        animations.put("IDLE", loadAnimation("sprite/Idle", 18, 0.1f));
        animations.put("BLINKING", loadAnimation("sprite/Idle Blinking", 18, 0.1f));
        animations.put("WALKING", loadAnimation("sprite/Walking", 18, 0.1f));

    }

    private Animation<TextureRegion> loadAnimation(String folderPath, int frameCount, float frameDuration) {
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);

        for (int i = 0; i < frameCount; i++) {
            // format number to match filename pattern
            String index = String.format("%03d", i);
            String path = folderPath + "/0_Dark_Oracle_" + folderPath.substring(folderPath.lastIndexOf("/") + 1) + "_"
                    + index + ".png";
            Texture texture = new Texture(Gdx.files.internal(path));
            frames.add(new TextureRegion(texture));
        }

        return new Animation<TextureRegion>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Create a table for the pet status
        Table statusTable = new Table();
        statusTable.top().pad(10);
        mainTable.add(statusTable).expandX().fillX().padBottom(20).row();

        Table actionTable = new Table();
        actionTable.bottom().pad(10);

        hungerBar = createProgressBar(hunger, MAX_STAT, 0.25f, 0, 1);
        happinessBar = createProgressBar(happiness, MAX_STAT, 0, 0.5f, 0);
        energyBar = createProgressBar(energy, MAX_STAT, 0, 0.1f, 0.8f);

        // Labels and prog bars
        Label hungerLabel = new Label("Hunger: ", skin);
        Label happinessLabel = new Label("Happiness: ", skin);
        Label energyLabel = new Label("Energy: ", skin);

        statusTable.add(hungerLabel).left().padRight(5);
        statusTable.add(hungerBar).width(150).padRight(20);

        statusTable.add(happinessLabel).left().padRight(5);
        statusTable.add(happinessBar).width(150).padRight(20);

        statusTable.add(energyLabel).left().padRight(5);
        statusTable.add(energyBar).width(150).padRight(20);

        mainTable.add(statusTable).expandX().fillX().padBottom(20).row();

        // Buttons for feeding, playing, and sleeping
        TextButton feedButton = new TextButton("Feed", skin);
        TextButton playButton = new TextButton("Play", skin);
        TextButton sleepButton = new TextButton("Sleep", skin);

        styleActionButton(feedButton, new Color(1f, 0.5f, 0f, 1f)); // Orange
        styleActionButton(playButton, new Color(0f, 0.8f, 0.2f, 1f)); // Green
        styleActionButton(sleepButton, new Color(0.2f, 0.4f, 0.8f, 1f)); // Blue

        game.batch.setColor(1, 1, 1, 1);

        // Button listeners
        feedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                feedPet();
            }
        });

        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playWithPet();
            }
        });

        sleepButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                putPetToSleep();
            }
        });

        // Add buttons to action table
        actionTable.add(feedButton).width(120).height(50).padRight(20);
        actionTable.add(playButton).width(120).height(50).padRight(20);
        actionTable.add(sleepButton).width(120).height(50);

        mainTable.add(actionTable).expandX().fillX().expandY();

    }

    private ProgressBar createProgressBar(float initial, float max, float r, float g, float b) {
        ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle(
                skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class));
        progressBarStyle.knobBefore = skin.newDrawable("white", new Color(r, g, b, 1));

        ProgressBar progressBar = new ProgressBar(0, max, 1, false, progressBarStyle);
        progressBar.setValue(initial);
        return progressBar;
    }

    private void styleActionButton(TextButton button, Color color) {
        button.getLabel().setFontScale(1.2f);
        button.setColor(color);
    }

    private void feedPet() {
        hunger = Math.min(MAX_STAT, hunger + 30);

        hungerBar.setValue(hunger);

        currentState = PetState.IDLE;
        stateTime = 0;

        Gdx.app.log("Feed", "Pet fed! Hunger: " + hunger);
    }

    private void playWithPet() {
        // increase happiness and decrease energy
        happiness = Math.min(MAX_STAT, happiness + 25);
        energy = Math.max(0, energy - 10);

        happinessBar.setValue(happiness);
        energyBar.setValue(energy);

        currentState = PetState.WALKING;
        stateTime = 0;

        Gdx.app.log("Play", "Pet played! Happiness: " + happiness + " Energy: " + energy);
    }

    private void putPetToSleep() {
        energy = Math.min(MAX_STAT, energy + 50);

        energyBar.setValue(energy);

        currentState = PetState.IDLE;
        stateTime = 0;

        Gdx.app.log("Sleep", "Pet slept! Energy: " + energy);

    }

    @Override
    public void show() {
        stateTime = 0f;
    }

    @Override
    public void render(float delta) {
        // update logic
        update(delta);

        // Clear the screen
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update camera
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        game.batch.begin();
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        drawPet();
        game.batch.end();

        // Draw UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    private void update(float delta) {
        stateTime += delta;

        updatePetStats(delta);

        hungerBar.setValue(hunger);
        happinessBar.setValue(happiness);
        energyBar.setValue(energy);

        updatePetState();
    }

    private void drawPet() {
        Animation<TextureRegion> animation = animations.get(currentState.toString());

        if (animation != null) {
            currentFrame = animation.getKeyFrame(stateTime);

            // center pet
            float width = currentFrame.getRegionWidth() * petScale;
            float height = currentFrame.getRegionHeight() * petScale;

            game.batch.draw(currentFrame, petPosition.x - width / 2, petPosition.y - height / 2, width, height);
        }
    }

    private void updatePetStats(float delta) {
        hunger = Math.max(hunger - STAT_DECAY_RATE * delta, 0);
        happiness = Math.max(happiness - STAT_DECAY_RATE * delta, 0);
        energy = Math.max(energy - STAT_DECAY_RATE * delta, 0);
    }

    private void updatePetState() {
        previousState = currentState;

        if (currentState == PetState.IDLE || currentState == PetState.BLINKING) {
            if (Math.random() < 0.01) {
                currentState = (currentState == PetState.IDLE) ? PetState.BLINKING : PetState.IDLE;

                if (previousState != currentState) {
                    stateTime = 0;
                }
            }

        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
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

        for (Animation<TextureRegion> animation : animations.values()) {
            for (TextureRegion frame : animation.getKeyFrames()) {
                if (frame.getTexture() != null)
                    frame.getTexture().dispose();
            }
        }
    }

}
