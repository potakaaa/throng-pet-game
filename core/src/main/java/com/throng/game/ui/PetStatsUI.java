package com.throng.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PetStatsUI {
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

        // Attach both to stage
        statusTable.setPosition(10, stage.getHeight() - 10);  // top-left
        statusTable.top().left();
        stage.addActor(statusTable);


        Table layoutTable = new Table();
        layoutTable.setFillParent(true);

        layoutTable.top().left().add(statusTable).expand().top().left().pad(10);
        layoutTable.row();
        layoutTable.bottom().add(buttonTable).expandX().center().padBottom(30);

        stage.addActor(layoutTable);


        // Initialize bars
        hungerBar = (ProgressBar) statusTable.getCells().get(1).getActor();
        happinessBar = (ProgressBar) statusTable.getCells().get(3).getActor();
        energyBar = (ProgressBar) statusTable.getCells().get(5).getActor();
    }

    private Table buildStatusTable(Skin skin) {
        Table table = new Table();
        table.pad(10);

        ProgressBar.ProgressBarStyle hungerStyle = getColoredBarStyle(skin, new Color(0.25f, 0f, 1f, 1f));
        ProgressBar.ProgressBarStyle happyStyle = getColoredBarStyle(skin, new Color(0f, 0.5f, 0f, 1f));
        ProgressBar.ProgressBarStyle energyStyle = getColoredBarStyle(skin, new Color(0f, 0.1f, 0.8f, 1f));

        table.add(new Label("Hunger:", skin)).left().padBottom(10);
        table.add(new ProgressBar(0, MAX_STAT, 1, false, hungerStyle)).width(150).padBottom(10).row();

        table.add(new Label("Happiness:", skin)).left().padBottom(10);
        table.add(new ProgressBar(0, MAX_STAT, 1, false, happyStyle)).width(150).padBottom(10).row();

        table.add(new Label("Energy:", skin)).left().padBottom(10);
        table.add(new ProgressBar(0, MAX_STAT, 1, false, energyStyle)).width(150).padBottom(10).row();

        return table;
    }

    private Table buildButtonTable(Skin skin, PetActionListener listener) {
        Table table = new Table();
        table.pad(10);

        TextButton feedButton = new TextButton("Feed", skin);
        TextButton playButton = new TextButton("Play", skin);
        TextButton sleepButton = new TextButton("Sleep", skin);

        feedButton.getLabel().setFontScale(1.2f);
        feedButton.setColor(new Color(1f, 0.5f, 0f, 1f));
        playButton.getLabel().setFontScale(1.2f);
        playButton.setColor(new Color(0f, 0.8f, 0.2f, 1f));
        sleepButton.getLabel().setFontScale(1.2f);
        sleepButton.setColor(new Color(0.2f, 0.4f, 0.8f, 1f));

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

        table.add(feedButton).width(120).height(50).padRight(20);
        table.add(playButton).width(120).height(50).padRight(20);
        table.add(sleepButton).width(120).height(50);

        return table;
    }

    private ProgressBar.ProgressBarStyle getColoredBarStyle(Skin skin, Color barColor) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle(
            skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class));
        style.knobBefore = skin.newDrawable("white", barColor);
        return style;
    }

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
