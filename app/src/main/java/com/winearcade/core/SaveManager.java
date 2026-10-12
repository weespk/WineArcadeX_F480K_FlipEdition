package com.winearcade.core;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveManager {
    private final SharedPreferences prefs;

    public SaveManager(Context context) {
        prefs = context.getSharedPreferences("wine_arcade_x", Context.MODE_PRIVATE);
    }

    public int getInt(String key, int def) { return prefs.getInt(key, def); }
    public long getLong(String key, long def) { return prefs.getLong(key, def); }
    public String getString(String key, String def) { return prefs.getString(key, def); }

    public void putInt(String key, int value) { prefs.edit().putInt(key, value).apply(); }
    public void putLong(String key, long value) { prefs.edit().putLong(key, value).apply(); }
    public void putString(String key, String value) { prefs.edit().putString(key, value).apply(); }

    public void addInt(String key, int delta) { putInt(key, getInt(key, 0) + delta); }

    public int getLevel() {
        int xp = getInt("xp", 0);
        return 1 + xp / 250;
    }

    public void reward(int coin, int xp) {
        addInt("coin", coin);
        addInt("xp", xp);
    }
}
