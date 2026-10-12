package com.winearcade.core;

public class ScoreManager {
    private final SaveManager save;

    public ScoreManager(SaveManager save) {
        this.save = save;
    }

    public boolean saveHighScore(String key, int value) {
        int old = save.getInt("score_" + key, 0);
        if (value > old) {
            save.putInt("score_" + key, value);
            return true;
        }
        return false;
    }

    public boolean saveBestLow(String key, long value) {
        long old = save.getLong("best_" + key, 0L);
        if (value > 0 && (old == 0L || value < old)) {
            save.putLong("best_" + key, value);
            return true;
        }
        return false;
    }
}
