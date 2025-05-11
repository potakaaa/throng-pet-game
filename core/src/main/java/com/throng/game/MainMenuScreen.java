package com.throng.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MainMenuScreen implements Screen {
    private final ThrongGame game;
    private final OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;

    private Texture backgroundTexture;
    private Texture playButtonDefault;
    private Texture playButtonHover;
    private Texture exitButtonDefault;
    private Texture exitButtonHover;

    public MainMenuScreen(ThrongGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(640, 480, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // UI Stage
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        // Load assets
        backgroundTexture = new Texture(Gdx.files.internal("background/menuBG.jpg"));
        playButtonDefault = new Texture(Gdx.files.internal("buttons/Square/Play/Default.png"));
        playButtonHover = new Texture(Gdx.files.internal("buttons/Square/Play/Hover.png"));
        exitButtonDefault = new Texture(Gdx.files.internal("buttons/Square/Home/Default.png"));
        exitButtonHover = new Texture(Gdx.files.internal("buttons/Square/Home/Hover.png"));

        // UI elements
        createUI();
    }

    private void createUI() {
        // Table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Create buttons with hover effects
        ImageButton playButton = new ImageButton(
                new TextureRegionDrawable(playButtonDefault),
                new TextureRegionDrawable(playButtonHover));

        ImageButton exitButton = new ImageButton(
                new TextureRegionDrawable(exitButtonDefault),
                new TextureRegionDrawable(exitButtonHover));

        // Append to table with spacing
        table.add(playButton).width(100).height(100).padBottom(10);
        table.row();
        table.add(exitButton).width(100).height(100);

        // Listeners
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // --- Background draw with zoom (no stretch) ---
        float bgWidth = backgroundTexture.getWidth();
        float bgHeight = backgroundTexture.getHeight();

        float scale = viewport.getWorldHeight() / bgHeight; // fill screen height

        float drawWidth = bgWidth * scale;
        float drawHeight = bgHeight * scale;

        float x = (viewport.getWorldWidth() - drawWidth) / 2f;
        float y = 0f;

        game.batch.begin();
        game.batch.draw(backgroundTexture, x, y, drawWidth, drawHeight);
        game.batch.end();
        // --- End background draw ---

        // Draw UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
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
        dispose();
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        playButtonDefault.dispose();
        playButtonHover.dispose();
        exitButtonDefault.dispose();
        exitButtonHover.dispose();
        stage.dispose();
    }
}
