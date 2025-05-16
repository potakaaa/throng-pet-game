package com.throng.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.throng.game.MainMenuScreen;
import com.throng.game.ThrongGame;
import com.throng.game.audio.AudioManager;
import com.throng.game.entity.PetStatObserver;
import com.throng.game.scoring.ScoreManager;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class PetStatsUI implements PetStatObserver {
    private final Table statusTable;
    private final Table buttonTable;
    private final ThrongGame game;
    private ImageButton feedButton;
    private float feedCooldown = 0f;
    private static final float FEED_COOLDOWN_DURATION = 30f;

    private ProgressBar hungerBar;
    private ProgressBar happinessBar;
    private ProgressBar energyBar;
    private ProgressBar wellbeingBar;
    private final Group floatingGroup;

    private final int barWidth = 12;

    private final float MAX_STAT = 100f;

    private final ProgressBar.ProgressBarStyle hungerStyle;
    private final ProgressBar.ProgressBarStyle happyStyle;
    private final ProgressBar.ProgressBarStyle energyStyle;
    private final ProgressBar.ProgressBarStyle wellbeingStyle;
    private final Skin skin;

    private Image feedOverlay;
    private Label cooldownLabel;
    private Stack feedButtonStack;

    public interface PetActionListener {
        void onFeed();

        void onPlay();

        void onSleep();
    }

    public PetStatsUI(Stage stage, Skin skin, PetActionListener listener, ThrongGame game) {
        this.skin = skin;
        this.game = game;
        hungerStyle = createModernBarStyle(Color.GREEN);
        happyStyle = createModernBarStyle(Color.GREEN);
        energyStyle = createModernBarStyle(Color.GREEN);
        wellbeingStyle = createModernBarStyle(Color.GOLD);

        statusTable = buildStatusTable();
        buttonTable = buildButtonTable(listener);

        floatingGroup = new Group();
        floatingGroup.addActor(statusTable);
        stage.addActor(floatingGroup);

        // Create a container for the button table
        Table bottomContainer = new Table();
        bottomContainer.setFillParent(true);
        bottomContainer.bottom();
        bottomContainer.add(buttonTable).padBottom(20);
        stage.addActor(bottomContainer);
    }

    private Table buildStatusTable() {
        Table table = new Table();
        table.top().center();

        int boxSize = 32;
        int barSpace = 45;
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
        labelStyle.font.getData().setScale(1.1f);

        hungerBar = new ProgressBar(0, MAX_STAT, 1, true, hungerStyle);
        happinessBar = new ProgressBar(0, MAX_STAT, 1, true, happyStyle);
        energyBar = new ProgressBar(0, MAX_STAT, 1, true, energyStyle);
        wellbeingBar = new ProgressBar(0, MAX_STAT, 1, true, wellbeingStyle);
        hungerBar.setValue(MAX_STAT);
        happinessBar.setValue(MAX_STAT);
        energyBar.setValue(MAX_STAT);
        wellbeingBar.setValue(MAX_STAT);

        Stack hungerStack = new Stack();
        hungerStack.add(hungerBar);
        hungerStack.add(new Label("F", labelStyle));

        Stack happyStack = new Stack();
        happyStack.add(happinessBar);
        happyStack.add(new Label("H", labelStyle));

        Stack energyStack = new Stack();
        energyStack.add(energyBar);
        energyStack.add(new Label("E", labelStyle));

        Stack wellbeingStack = new Stack();
        wellbeingStack.add(wellbeingBar);
        wellbeingStack.add(new Label("W", labelStyle));

        table.add(hungerStack).width(barSpace).height(boxSize).padRight(8);
        table.add(happyStack).width(barSpace).height(boxSize).padRight(8);
        table.add(energyStack).width(barSpace).height(boxSize).padRight(8);
        table.add(wellbeingStack).width(barSpace).height(boxSize).padRight(8);
        table.center();

        hungerBar.setWidth(barWidth); // or your desired width
        hungerBar.setHeight(32); // optional for vertical bars

        happinessBar.setWidth(barWidth);
        happinessBar.setHeight(32);

        energyBar.setWidth(barWidth);
        energyBar.setHeight(32);

        wellbeingBar.setWidth(barWidth);
        wellbeingBar.setHeight(32);

        return table;
    }

    private ProgressBar.ProgressBarStyle createModernBarStyle(Color barColor) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        int barHeight = 1; // Only 1 pixel high (will be stretched by size)

        // Background
        Pixmap bgPixmap = new Pixmap(barWidth, barHeight, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        bgPixmap.fill();
        style.background = new TextureRegionDrawable(new TextureRegion(new Texture(bgPixmap)));

        // Filled (knobBefore)
        Pixmap knobPixmap = new Pixmap(barWidth, barHeight, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(barColor);
        knobPixmap.fill();
        style.knobBefore = new TextureRegionDrawable(new TextureRegion(new Texture(knobPixmap)));

        style.knob = null;
        return style;
    }

    private Table buildButtonTable(PetActionListener listener) {
        Table table = new Table();

        // Load button textures
        Texture feedTexture = new Texture(Gdx.files.internal(("buthrongs/feed.png")));
        Texture feedPressedTexture = new Texture(Gdx.files.internal(("buthrongs/feed_pressed.png")));
        Texture playTexture = new Texture(Gdx.files.internal("buthrongs/play.png"));
        Texture playPressedTexture = new Texture(Gdx.files.internal("buthrongs/play_pressed.png"));
        Texture sleepTexture = new Texture(Gdx.files.internal("buthrongs/sleep.png"));
        Texture sleepPressedTexture = new Texture(Gdx.files.internal("buthrongs/sleep_pressed.png"));

        // Create image buttons with pressed states
        ImageButton.ImageButtonStyle feedStyle = new ImageButton.ImageButtonStyle();
        feedStyle.imageUp = new TextureRegionDrawable(new TextureRegion(feedTexture));
        feedStyle.imageDown = new TextureRegionDrawable(new TextureRegion(feedPressedTexture));
        feedStyle.imageOver = new TextureRegionDrawable(new TextureRegion(feedPressedTexture));
        feedButton = new ImageButton(feedStyle);

        // Create dark overlay
        Pixmap overlayPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        overlayPixmap.setColor(0, 0, 0, 0.6f);
        overlayPixmap.fill();
        Texture overlayTexture = new Texture(overlayPixmap);
        overlayPixmap.dispose();
        feedOverlay = new Image(overlayTexture);
        feedOverlay.setVisible(false);

        // Create cooldown label
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
        cooldownLabel = new Label("", labelStyle);
        cooldownLabel.setAlignment(Align.center);
        cooldownLabel.setVisible(false);

        // Create a stack to layer the button, overlay, and label
        feedButtonStack = new Stack();
        feedButtonStack.add(feedButton);
        feedButtonStack.add(feedOverlay);
        feedButtonStack.add(cooldownLabel);

        ImageButton.ImageButtonStyle playStyle = new ImageButton.ImageButtonStyle();
        playStyle.imageUp = new TextureRegionDrawable(new TextureRegion(playTexture));
        playStyle.imageDown = new TextureRegionDrawable(new TextureRegion(playPressedTexture));
        playStyle.imageOver = new TextureRegionDrawable(new TextureRegion(playPressedTexture));
        ImageButton playButton = new ImageButton(playStyle);

        ImageButton.ImageButtonStyle sleepStyle = new ImageButton.ImageButtonStyle();
        sleepStyle.imageUp = new TextureRegionDrawable(new TextureRegion(sleepTexture));
        sleepStyle.imageDown = new TextureRegionDrawable(new TextureRegion(sleepPressedTexture));
        sleepStyle.imageOver = new TextureRegionDrawable(new TextureRegion(sleepPressedTexture));
        ImageButton sleepButton = new ImageButton(sleepStyle);

        // Add buttons to table with original large size and spacing
        table.add(feedButtonStack).size(140).padRight(20);
        table.add(playButton).size(140).padRight(20);
        table.add(sleepButton).size(140);

        // Add listeners
        feedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (feedCooldown <= 0) {
                    AudioManager.getInstance().playClickActionSound();
                    ScoreManager.getInstance().addScore(ScoreManager.SCORE_FEED);
                    listener.onFeed();
                    feedCooldown = FEED_COOLDOWN_DURATION;
                    feedOverlay.setVisible(true);
                    cooldownLabel.setVisible(true);
                }
            }
        });
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().playClickActionSound();
                ScoreManager.getInstance().addScore(ScoreManager.SCORE_PLAY);
                listener.onPlay();
            }
        });
        sleepButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().playClickActionSound();
                AudioManager.getInstance().playSleepingSound();
                ScoreManager.getInstance().addScore(ScoreManager.SCORE_SLEEP);
                listener.onSleep();
            }
        });

        return table;
    }

    @Override
    public void updateBars(float hunger, float happiness, float energy, float wellbeing) {
        try {
            updateBarColor(hungerBar, hunger, hungerStyle);
            updateBarColor(happinessBar, happiness, happyStyle);
            updateBarColor(energyBar, energy, energyStyle);
            updateBarColor(wellbeingBar, wellbeing, wellbeingStyle);
            wellbeingBar.setValue(wellbeing);
            hungerBar.setValue(hunger);
            happinessBar.setValue(happiness);
            energyBar.setValue(energy);
        } catch (Exception e) {
            System.err.println("[UI ERROR] updateBars crashed: " + e.getMessage());
        }
    }

    private boolean isDead = false;

    public void onPetDied() {
        if (isDead)
            return;
        isDead = true;

        // Hide all pet-related UI
        buttonTable.setVisible(false);
        statusTable.setVisible(false);

        // Table for centered death message and button
        Table deathTable = new Table();
        deathTable.setFillParent(true);
        deathTable.center();

        Label.LabelStyle deathStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.RED);
        Label deathLabel = new Label("Your pet has died", deathStyle);
        deathLabel.setFontScale(1.4f);

        // Add score labels with improved spacing
        Label.LabelStyle scoreStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
        Label scoreLabel = new Label("Score: " + ScoreManager.getInstance().getCurrentScore(), scoreStyle);
        scoreLabel.setFontScale(1.2f);
        Label highScoreLabel = new Label("High Score: " + ScoreManager.getInstance().getHighScore(), scoreStyle);
        highScoreLabel.setFontScale(1.2f);

        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        mainMenuButton.getLabel().setFontScale(1.2f);

        deathTable.add(deathLabel).padBottom(40).row();
        deathTable.add(scoreLabel).padBottom(20).row();
        deathTable.add(highScoreLabel).padBottom(40).row();
        deathTable.add(mainMenuButton).width(200).height(60);

        floatingGroup.addActor(deathTable);

        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().playClickActionSound();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        floatingGroup.toFront();
    }

    private void updateBarColor(ProgressBar bar, float value, ProgressBar.ProgressBarStyle style) {
        Color color = (value > 66) ? Color.GREEN : (value > 33) ? Color.YELLOW : Color.RED;

        int barHeight = 1;

        Pixmap knobPixmap = new Pixmap(barWidth, barHeight, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(color);
        knobPixmap.fill();
        style.knobBefore = new TextureRegionDrawable(new TextureRegion(new Texture(knobPixmap)));

        bar.setStyle(style);
    }

    public Table getStatusTable() {
        return statusTable;
    }

    public Table getActionButtonTable() {
        return buttonTable;
    }

    public Group getFloatingGroup() {
        return floatingGroup;
    }

    public void update(float delta) {
        if (feedCooldown > 0) {
            feedCooldown -= delta;
            if (feedCooldown <= 0) {
                feedCooldown = 0;
                feedOverlay.setVisible(false);
                cooldownLabel.setVisible(false);
            } else {
                // Update the countdown text
                int secondsLeft = (int) Math.ceil(feedCooldown);
                cooldownLabel.setText(String.valueOf(secondsLeft));
            }
        }
    }
}
