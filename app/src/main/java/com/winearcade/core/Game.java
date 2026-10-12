package com.winearcade.core;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface Game {
    String getName();
    void start();
    void pause();
    void resume();
    boolean onKeyDown(int keyCode);
    void onFlipOpen(long eventTime);
    void onFlipClose(long eventTime);
    void update(long now);
    void draw(Canvas canvas, Paint paint, int width, int height);
    boolean isFinished();
    void finish();
}
