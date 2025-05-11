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

    private ProgressBar hungerBar;
    private ProgressBar happinessBar;
    private ProgressBar energyBar;

    private final float MAX_STAT = 100f;

    // Store styles for dynamic color change
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
        // Create styles for dynamic color change
        hungerStyle = createModernBarStyle(skin, Color.GREEN);
        happyStyle = createModernBarStyle(skin, Color.GREEN);
        energyStyle = createModernBarStyle(skin, Color.GREEN);

        statusTable = buildStatusTable(skin);
        buttonTable = buildButtonTable(skin, listener);

        Table layoutTable = new Table();
        layoutTable.setFillParent(true);
        layoutTable.add(statusTable).expandX().expandY().top().center().padTop(8);
        layoutTable.row();
        layoutTable.add(buttonTable).expandX().expandY().bottom().center().padBottom(10);

        stage.addActor(layoutTable);
    }

    private Table buildStatusTable(Skin skin) {
        Table table = new Table();
        table.top().center();

        // Small box size
        int boxSize = 32;
        int barWidth = 40; // Increased width
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
        labelStyle.font.getData().setScale(1.1f);

        // Create vertical progress bars
        hungerBar = new ProgressBar(0, MAX_STAT, 1, true, hungerStyle);
        happinessBar = new ProgressBar(0, MAX_STAT, 1, true, happyStyle);
        energyBar = new ProgressBar(0, MAX_STAT, 1, true, energyStyle);
        hungerBar.setValue(MAX_STAT);
        happinessBar.setValue(MAX_STAT);
        energyBar.setValue(MAX_STAT);

        // Overlay a single-letter label inside each box using Stack
        Stack hungerStack = new Stack();
        hungerStack.add(hungerBar);
        hungerStack.add(new Label("F", labelStyle)); // F for Food
        Stack happyStack = new Stack();
        happyStack.add(happinessBar);
        happyStack.add(new Label("H", labelStyle)); // H for Happiness
        Stack energyStack = new Stack();
        energyStack.add(energyBar);
        energyStack.add(new Label("E", labelStyle)); // E for Energy

        table.add(hungerStack).width(barWidth).height(boxSize).padRight(8);
        table.add(happyStack).width(barWidth).height(boxSize).padRight(8);
        table.add(energyStack).width(barWidth).height(boxSize);

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
        table.bottom().center();

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
        updateBarColor(hungerBar, hunger, hungerStyle);
        updateBarColor(happinessBar, happiness, happyStyle);
        updateBarColor(energyBar, energy, energyStyle);
        hungerBar.setValue(hunger);
        happinessBar.setValue(happiness);
        energyBar.setValue(energy);
    }

    private void updateBarColor(ProgressBar bar, float value, ProgressBar.ProgressBarStyle style) {
        Color color;
        if (value > 66)
            color = Color.GREEN;
        else if (value > 33)
            color = Color.YELLOW;
        else
            color = Color.RED;
        style.knobBefore = skin.newDrawable("white", color);
        bar.setStyle(style);
    }

    public Table getStatusTable() {
        return statusTable;
    }

    public Table getActionButtonTable() {
        return buttonTable;
    }
}
