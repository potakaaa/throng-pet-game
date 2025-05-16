package com.throng.game.scoring;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;

public class ScoreManager {
    private static ScoreManager instance;
    private int currentScore;
    private int highScore;
    private final Preferences prefs;
    
    private static final String PREFS_NAME = "throng_game_prefs";
    private static final String HIGH_SCORE_KEY = "high_score";
    
    // Score values for different actions
    public static final int SCORE_FEED = 10;
    public static final int SCORE_PLAY = 15;
    public static final int SCORE_SLEEP = 5;
    public static final int SCORE_KILL_ENEMY = 50;
    public static final int SCORE_COLLECT_FRUIT = 20;
    
    private ScoreManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        highScore = prefs.getInteger(HIGH_SCORE_KEY, 0);
        currentScore = 0;
    }
    
    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }
    
    public void addScore(int points) {
        currentScore += points;
        if (currentScore > highScore) {
            highScore = currentScore;
            prefs.putInteger(HIGH_SCORE_KEY, highScore);
            prefs.flush();
        }
    }
    
    public void resetScore() {
        currentScore = 0;
    }
    
    public int getCurrentScore() {
        return currentScore;
    }
    
    public int getHighScore() {
        return highScore;
    }
} 