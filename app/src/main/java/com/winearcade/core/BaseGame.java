package com.winearcade.core;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

public abstract class BaseGame implements Game {
    protected final SaveManager save;
    protected final ScoreManager score;
    protected final VibrationManager vibration;
    protected final SoundManager sound;
    protected boolean finished;

    protected BaseGame(SaveManager save, ScoreManager score,
                       VibrationManager vibration, SoundManager sound) {
        this.save = save;
        this.score = score;
        this.vibration = vibration;
        this.sound = sound;
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void onFlipOpen(long eventTime) {}
    @Override public void onFlipClose(long eventTime) {}
    @Override public void update(long now) {}
    @Override public boolean isFinished() { return finished; }
    @Override public void finish() { finished = true; }

    protected void text(Canvas c, Paint p, String s, float x, float y, float size, boolean center) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFFFFF);
        p.setTextSize(size);
        p.setTextAlign(center ? Paint.Align.CENTER : Paint.Align.LEFT);
        c.drawText(s, x, y, p);
    }
}
