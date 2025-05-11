package com.throng.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.throng.game.entity.PetStatObserver;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class PetStatsUI implements PetStatObserver {
    private final Table statusTable;
    private final Table buttonTable;

    private final ProgressBar hungerBar;
    private final ProgressBar happinessBar;
    private final ProgressBar energyBar;

    private final float MAX_STAT = 100f;

    public interface PetActionListener {
        void onFeed();

        void onPlay();

        void onSleep();
    }

    public PetStatsUI(Stage stage, Skin skin, PetActionListener listener) {
        statusTable = buildStatusTable(skin);
        buttonTable = buildButtonTable(skin, listener);

        Table layoutTable = new Table();
        layoutTable.setFillParent(true);
        layoutTable.top().left().add(statusTable).expand().top().left().pad(20);
        layoutTable.row();
        layoutTable.bottom().add(buttonTable).expandX().center().padBottom(40);

        stage.addActor(layoutTable);

        // Initialize progress bar references
        hungerBar = (ProgressBar) statusTable.getCells().get(1).getActor();
        happinessBar = (ProgressBar) statusTable.getCells().get(3).getActor();
        energyBar = (ProgressBar) statusTable.getCells().get(5).getActor();
    }

    private Table buildStatusTable(Skin skin) {
        Table table = new Table();
        table.pad(15);
        table.background(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.7f)));

        // Create modern progress bar styles
        ProgressBar.ProgressBarStyle hungerStyle = createModernBarStyle(skin, new Color(0.95f, 0.3f, 0.2f, 1f));
        ProgressBar.ProgressBarStyle happyStyle = createModernBarStyle(skin, new Color(0.2f, 0.8f, 0.4f, 1f));
        ProgressBar.ProgressBarStyle energyStyle = createModernBarStyle(skin, new Color(0.2f, 0.4f, 0.9f, 1f));

        // Add labels with modern styling
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
        labelStyle.font.getData().setScale(1.2f);

        table.add(new Label("Hunger:", labelStyle)).left().padBottom(15);
        table.add(new ProgressBar(0, MAX_STAT, 1, false, hungerStyle)).width(200).height(20).padBottom(15).row();

        table.add(new Label("Happiness:", labelStyle)).left().padBottom(15);
        table.add(new ProgressBar(0, MAX_STAT, 1, false, happyStyle)).width(200).height(20).padBottom(15).row();

        table.add(new Label("Energy:", labelStyle)).left().padBottom(15);
        table.add(new ProgressBar(0, MAX_STAT, 1, false, energyStyle)).width(200).height(20).padBottom(15).row();

        return table;
    }

    private ProgressBar.ProgressBarStyle createModernBarStyle(Skin skin, Color barColor) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        // Create background with rounded corners
        Drawable background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f));
        style.background = background;

        // Create filled part with gradient effect
        Drawable knobBefore = skin.newDrawable("white", barColor);
        style.knobBefore = knobBefore;

        // Remove the knob for a cleaner look
        style.knob = null;

        return style;
    }

    private Table buildButtonTable(Skin skin, PetActionListener listener) {
        Table table = new Table();
        table.pad(15);

        // Create modern button styles
        TextButton.TextButtonStyle feedStyle = createModernButtonStyle(skin, new Color(0.95f, 0.3f, 0.2f, 1f));
        TextButton.TextButtonStyle playStyle = createModernButtonStyle(skin, new Color(0.2f, 0.8f, 0.4f, 1f));
        TextButton.TextButtonStyle sleepStyle = createModernButtonStyle(skin, new Color(0.2f, 0.4f, 0.9f, 1f));

        TextButton feedButton = new TextButton("Feed", feedStyle);
        TextButton playButton = new TextButton("Play", playStyle);
        TextButton sleepButton = new TextButton("Sleep", sleepStyle);

        // Add hover effects
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

        table.add(feedButton).width(140).height(50).padRight(20);
        table.add(playButton).width(140).height(50).padRight(20);
        table.add(sleepButton).width(140).height(50);

        return table;
    }

    private TextButton.TextButtonStyle createModernButtonStyle(Skin skin, Color buttonColor) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        // Create button background
        Drawable up = skin.newDrawable("white", buttonColor);
        Drawable down = skin.newDrawable("white", buttonColor.cpy().mul(0.8f));
        Drawable over = skin.newDrawable("white", buttonColor.cpy().mul(1.2f));

        style.up = up;
        style.down = down;
        style.over = over;

        // Set font and colors
        style.font = skin.getFont("default-font");
        style.font.getData().setScale(1.2f);
        style.fontColor = Color.WHITE;
        style.downFontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;

        return style;
    }

    @Override
    public void updateBars(float hunger, float happiness, float energy) {
        hungerBar.setValue(hunger);
        happinessBar.setValue(happiness);
        energyBar.setValue(energy);
    }

    public Table getStatusTable() {
        return statusTable;
    }

    public Table getActionButtonTable() {
        return buttonTable;
    }
}
