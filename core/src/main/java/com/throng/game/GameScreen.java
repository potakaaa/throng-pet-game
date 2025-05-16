package com.throng.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.throng.game.audio.AudioManager;
import com.throng.game.entity.fruits.Fruit;
import com.throng.game.entity.Pet;
import com.throng.game.entity.fruits.FruitFactory;
import com.throng.game.ui.PetStatsUI;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.math.Vector3;
import com.throng.game.entity.Enemy;
import com.throng.game.entity.EnemyManager;
import com.badlogic.gdx.InputAdapter;
import com.throng.game.scoring.ScoreManager;

public class GameScreen implements Screen {
    private final ThrongGame game;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage gameStage; // for game world elements
    private final Stage uiStage; // for UI elements

    // Make world much larger and use simple dimensions
    private static final float WORLD_WIDTH = 4000f;
    private static final float WORLD_HEIGHT = 4000f;
    private static final float WORLD_PADDING = 50f;

    private final Texture backgroundTexture;
    private final Skin skin;
    private final Array<Fruit> fruits = new Array<>();
    private final Pet pet;
    private final PetStatsUI petStatsUI;
    private float timeSinceManualInput = 0f;
    private static final float AUTO_BEHAVIOR_TIMEOUT = 1.5f;
    private Texture soundOnDefault;
    private Texture soundOnHover;
    private Texture soundOffDefault;
    private Texture soundOffHover;
    private final EnemyManager enemyManager;
    private static final float ENEMY_CLICK_DAMAGE = 25f; // Amount of damage dealt when clicking an enemy
    private static final float ENEMY_TAP_DAMAGE = 25f; // Amount of damage dealt when tapping an enemy

    public GameScreen(final ThrongGame game) {
        this.game = game;
        
        // Reset score at the start of a new game
        ScoreManager.getInstance().resetScore();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Initialize audio managers
        AudioManager.getInstance();  // Ensure main audio manager is initialized
        com.throng.game.audio.EnemySoundManager.getInstance();  // Ensure enemy sound manager is initialized

        // Create separate stages for game and UI
        gameStage = new Stage(viewport, game.batch);
        uiStage = new Stage(new ScreenViewport(), game.batch);

        backgroundTexture = new Texture("background/Grass_Sample.png");
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        soundOnDefault = new Texture(Gdx.files.internal("buttons/Square/SoundOn/Default.png"));
        soundOnHover = new Texture(Gdx.files.internal("buttons/Square/SoundOn/Hover.png"));
        soundOffDefault = new Texture(Gdx.files.internal("buttons/Square/SoundOff/Default.png"));
        soundOffHover = new Texture(Gdx.files.internal("buttons/Square/SoundOff/Hover.png"));

        // Create sound toggle button
        ImageButton soundButton = new ImageButton(
                new TextureRegionDrawable(AudioManager.getInstance().isMuted() ? soundOffDefault : soundOnDefault),
                new TextureRegionDrawable(AudioManager.getInstance().isMuted() ? soundOffHover : soundOnHover));
        soundButton.setPosition(20, Gdx.graphics.getHeight() - 70);
        soundButton.setSize(50, 50);
        uiStage.addActor(soundButton);

        soundButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().toggleMute();
                soundButton.getStyle().imageUp = new TextureRegionDrawable(
                        AudioManager.getInstance().isMuted() ? soundOffDefault : soundOnDefault);
                soundButton.getStyle().imageOver = new TextureRegionDrawable(
                        AudioManager.getInstance().isMuted() ? soundOffHover : soundOnHover);
            }
        });

        pet = new Pet(new Vector2(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f), null);
        enemyManager = new EnemyManager(pet, fruits);
        petStatsUI = new PetStatsUI(uiStage, skin, new PetStatsUI.PetActionListener() {
            @Override
            public void onFeed() {
                float minSpawnDistance = 200f;
                float maxSpawnDistance = 400f;

                float angle = (float) (Math.random() * Math.PI * 2);
                float distance = minSpawnDistance + (float) Math.random() * (maxSpawnDistance - minSpawnDistance);

                float dropX = pet.getPosition().x + (float) Math.cos(angle) * distance;
                float dropY = pet.getPosition().y + (float) Math.sin(angle) * distance;

                Vector2 dropPos = new Vector2(dropX, dropY);
                Fruit fruit = FruitFactory.createRandomFruit(dropPos, pet);
                fruits.add(fruit);
            }

            @Override
            public void onPlay() {
                pet.play();
            }

            @Override
            public void onSleep() {
                pet.sleep();
            }
        }, game);
        pet.setStatsObserver(petStatsUI);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(gameStage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                checkEnemyTap(worldCoords.x, worldCoords.y);
                return true;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void checkFruitCollision() {
        for (int i = fruits.size - 1; i >= 0; i--) {
            Fruit fruit = fruits.get(i);
            try {
                if (fruit != null && pet.getBounds().overlaps(fruit.getBounds())) {
                    fruit.touch();
                    fruit.applyEffect();
                    AudioManager.getInstance().playEatingSound();
                    ScoreManager.getInstance().addScore(ScoreManager.SCORE_COLLECT_FRUIT);
                    fruit.dispose();
                    fruits.removeIndex(i);
                }
            } catch (Exception e) {
                Gdx.app.error("Collision", "Error during fruit collision: " + e.getMessage(), e);
            }
        }
    }

    private void update(float delta) {
        float dx = 0, dy = 0;
        boolean keyPressed = false;

        // Only process movement if pet is alive
        if (!pet.isDead()) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                dy += 0.7f;
                keyPressed = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                dy -= 0.7f;
                keyPressed = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                dx -= 0.7f;
                keyPressed = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                dx += 0.7f;
                keyPressed = true;
            }

            if (keyPressed) {
                pet.manualMove(dx, dy, WORLD_WIDTH, WORLD_HEIGHT, delta);
                timeSinceManualInput = 0f;
            } else {
                timeSinceManualInput += delta;
            }
            pet.suppressAutoBehavior = (!keyPressed && timeSinceManualInput < AUTO_BEHAVIOR_TIMEOUT);
        }

        pet.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        enemyManager.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        petStatsUI.update(delta);

        // Update and check fruits
        for (int i = fruits.size - 1; i >= 0; i--) {
            Fruit fruit = fruits.get(i);
            fruit.update(delta);
            if (fruit.isExpired()) {
                fruit.dispose();
                fruits.removeIndex(i);
            }
        }

        checkFruitCollision();

        // Simple camera following
        Vector2 petPos = pet.getPosition();
        camera.position.x = petPos.x;
        camera.position.y = petPos.y;
    }

    private void drawFruits() {
        // Draw fruits after background but before UI
        for (Fruit fruit : fruits) {
            TextureRegion frame = fruit.getFrame();
            Vector2 pos = fruit.getPosition();
            float size = fruit.getSize() * 1.2f;

            // Skip drawing if fruit is blinking and in invisible phase
            if (fruit.shouldBlink()) {
                continue;
            }

            // Draw the fruit
            game.batch.draw(frame,
                    pos.x - size / 2,
                    pos.y - size / 2,
                    size,
                    size
            );
        }
    }

    private boolean isPositionVisible(Vector2 position) {
        float leftBound = camera.position.x - viewport.getWorldWidth() / 2f;
        float rightBound = camera.position.x + viewport.getWorldWidth() / 2f;
        float bottomBound = camera.position.y - viewport.getWorldHeight() / 2f;
        float topBound = camera.position.y + viewport.getWorldHeight() / 2f;

        return position.x >= leftBound && position.x <= rightBound &&
                position.y >= bottomBound && position.y <= topBound;
    }

    private void checkEnemyTap(float worldX, float worldY) {
        Vector2 tapLocation = new Vector2(worldX, worldY);
        Array<Enemy> enemies = enemyManager.getEnemies();

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getBounds().contains(tapLocation.x, tapLocation.y)) {
                enemy.takeDamage(ENEMY_TAP_DAMAGE);
                if (enemy.isDead()) {
                    ScoreManager.getInstance().addScore(ScoreManager.SCORE_KILL_ENEMY);
                }
                AudioManager.getInstance().playEatingSound();
                break;
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        Vector2 petPos = pet.getPosition();

        // Update UI positions to follow camera
        float uiOffsetY = 100f;

        // Convert world position to screen position for floating stats
        Vector3 screenPos = camera.project(new Vector3(petPos.x, petPos.y, 0));
        petStatsUI.getFloatingGroup().setPosition(
                screenPos.x - petStatsUI.getFloatingGroup().getWidth() / 2f,
                screenPos.y + uiOffsetY);

        // Update audio managers
        AudioManager.getInstance().update(delta);
        AudioManager.getInstance().updateStats(
                pet.getHunger(),
                pet.getHappiness(),
                pet.getEnergy(),
                pet.getWellbeing());

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render game world
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Draw background tiles
        float startX = camera.position.x - viewport.getWorldWidth() / 2f;
        float startY = camera.position.y - viewport.getWorldHeight() / 2f;
        float endX = startX + viewport.getWorldWidth();
        float endY = startY + viewport.getWorldHeight();

        float bgWidth = backgroundTexture.getWidth();
        float bgHeight = backgroundTexture.getHeight();

        int startTileX = (int) Math.floor(startX / bgWidth);
        int startTileY = (int) Math.floor(startY / bgHeight);
        int endTileX = (int) Math.ceil(endX / bgWidth);
        int endTileY = (int) Math.ceil(endY / bgHeight);

        for (int x = startTileX; x <= endTileX; x++) {
            for (int y = startTileY; y <= endTileY; y++) {
                game.batch.draw(backgroundTexture,
                        x * bgWidth,
                        y * bgHeight);
            }
        }

        drawFruits();

        // Draw enemies
        for (Enemy enemy : enemyManager.getEnemies()) {
            TextureRegion enemyTexture = enemy.getCurrentFrame();
            game.batch.draw(
                enemyTexture,
                enemy.getPosition().x - enemy.getBounds().width / 2f,
                enemy.getPosition().y - enemy.getBounds().height / 2f,
                enemy.getBounds().width,
                enemy.getBounds().height
            );
        }

        // Draw pet
        TextureRegion petFrame = pet.getCurrentFrame();
        float scale = 0.3f;
        float width = petFrame.getRegionWidth() * scale;
        float height = petFrame.getRegionHeight() * scale;

        if (pet.isFacingLeft()) {
            petFrame.flip(true, false);
        }
        game.batch.draw(
            petFrame,
            pet.getPosition().x - width / 2f,
            pet.getPosition().y - height / 2f,
            width,
            height
        );
        if (pet.isFacingLeft()) {
            petFrame.flip(true, false); // Flip back to avoid affecting other draws
        }

        game.batch.end();

        // Render UI
        uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {

    }

    @Override
    public void pause() {
        AudioManager.getInstance().pause();
    }

    @Override
    public void resume() {
        AudioManager.getInstance().resume();
    }

    @Override
    public void hide() {
        AudioManager.getInstance().pause();
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        skin.dispose();
        gameStage.dispose();
        uiStage.dispose();
        pet.dispose();
        soundOnDefault.dispose();
        soundOnHover.dispose();
        soundOffDefault.dispose();
        soundOffHover.dispose();
    }
}
