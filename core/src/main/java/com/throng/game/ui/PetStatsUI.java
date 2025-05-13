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
import com.throng.game.entity.PetStatObserver;
import com.badlogic.gdx.utils.Align;

public class PetStatsUI implements PetStatObserver {
    private final Table statusTable;
    private final Table buttonTable;

    private ProgressBar hungerBar;
    private ProgressBar happinessBar;
    private ProgressBar energyBar;
    private final Group floatingGroup;

    private final float MAX_STAT = 100f;

    private final ProgressBar.ProgressBarStyle hungerStyle;
    private final ProgressBar.ProgressBarStyle happyStyle;
    private final ProgressBar.ProgressBarStyle energyStyle;
    private final Skin skin;

    public interface PetActionListener {
        void onFeed();

        void onPlay();

        void onSleep();
    }

    public PetStatsUI(Stage stage, Skin skin, PetActionListener listener) {
        this.skin = skin;

        hungerStyle = createModernBarStyle(Color.GREEN);
        happyStyle = createModernBarStyle(Color.GREEN);
        energyStyle = createModernBarStyle(Color.GREEN);

        statusTable = buildStatusTable();
        buttonTable = buildButtonTable(listener);

        floatingGroup = new Group();
        floatingGroup.addActor(statusTable);
        stage.addActor(floatingGroup);

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.align(Align.bottom); // <-- Important

        bottomTable.row(); // new row
        bottomTable.add(buttonTable).center().padBottom(16);

        stage.addActor(bottomTable);

    }

    private Table buildStatusTable() {
        Table table = new Table();
        table.top().center();

        int boxSize = 32;
        int barWidth = 40;
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
        labelStyle.font.getData().setScale(1.1f);

        hungerBar = new ProgressBar(0, MAX_STAT, 1, true, hungerStyle);
        happinessBar = new ProgressBar(0, MAX_STAT, 1, true, happyStyle);
        energyBar = new ProgressBar(0, MAX_STAT, 1, true, energyStyle);
        hungerBar.setValue(MAX_STAT);
        happinessBar.setValue(MAX_STAT);
        energyBar.setValue(MAX_STAT);

        Stack hungerStack = new Stack();
        hungerStack.add(hungerBar);
        hungerStack.add(new Label("F", labelStyle));

        Stack happyStack = new Stack();
        happyStack.add(happinessBar);
        happyStack.add(new Label("H", labelStyle));

        Stack energyStack = new Stack();
        energyStack.add(energyBar);
        energyStack.add(new Label("E", labelStyle));

        table.add(hungerStack).width(barWidth).height(boxSize).padRight(8);
        table.add(happyStack).width(barWidth).height(boxSize).padRight(8);
        table.add(energyStack).width(barWidth).height(boxSize);
        table.center();

        return table;
    }

    private ProgressBar.ProgressBarStyle createModernBarStyle(Color barColor) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        bgPixmap.fill();
        style.background = new TextureRegionDrawable(new TextureRegion(new Texture(bgPixmap)));

        Pixmap knobPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
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
        Texture playTexture = new Texture(Gdx.files.internal("buthrongs/play.png"));
        Texture sleepTexture = new Texture(Gdx.files.internal("buthrongs/sleep.png"));

        // Create image buttons
        ImageButton feedButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(feedTexture)));
        ImageButton playButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(playTexture)));
        ImageButton sleepButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(sleepTexture)));

        // Add listeners
        feedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onFeed();
            }
        });
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onPlay();
            }
        });
        sleepButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onSleep();
            }
        });

        // Add buttons to table with appropriate sizing
        table.add(feedButton).size(140).padRight(20);
        table.add(playButton).size(140).padRight(20);
        table.add(sleepButton).size(140);

        return table;
    }

    @Override
    public void updateBars(float hunger, float happiness, float energy) {
        try {
            updateBarColor(hungerBar, hunger, hungerStyle);
            updateBarColor(happinessBar, happiness, happyStyle);
            updateBarColor(energyBar, energy, energyStyle);
            hungerBar.setValue(hunger);
            happinessBar.setValue(happiness);
            energyBar.setValue(energy);
        } catch (Exception e) {
            System.err.println("[UI ERROR] updateBars crashed: " + e.getMessage());
        }
    }

    private void updateBarColor(ProgressBar bar, float value, ProgressBar.ProgressBarStyle style) {
        Color color = (value > 66) ? Color.GREEN : (value > 33) ? Color.YELLOW : Color.RED;
        Pixmap knobPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
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
}
