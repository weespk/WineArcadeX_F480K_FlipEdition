package com.winearcade.games.flip;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.winearcade.core.BaseGame;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.Random;

public class FlipReactionGame extends BaseGame {
    private final Random random = new Random();
    private int state;
    private long signalAt;
    private long openMs = -1;
    private boolean closed;

    public FlipReactionGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName() { return "Flip Reaction"; }

    @Override public void start() {
        state = 0; closed = false; openMs = -1; finished = false; sound.start();
    }

    @Override public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_5) {
            start();
            return true;
        }
        return false;
    }

    @Override public void onFlipClose(long t) {
        if (state == 0) {
            closed = true;
            state = 1;
            signalAt = t + 1200 + random.nextInt(3200);
        }
    }

    @Override public void onFlipOpen(long t) {
        if (!closed) return;
        if (state == 1 && t < signalAt) {
            state = 4;
            openMs = -2;
            vibration.fail(); sound.fail();
        } else if (state == 2) {
            openMs = t - signalAt;
            state = 3;
            boolean best = score.saveBestLow("flip_reaction", openMs);
            save.reward(best ? 70 : 20, best ? 40 : 15);
            vibration.success(); sound.success();
        }
    }

    @Override public void update(long now) {
        if (state == 1 && now >= signalAt) {
            state = 2;
            vibration.doublePulse();
            sound.start();
        }
    }

    @Override public void draw(Canvas c, Paint p, int w, int h) {
        text(c,p,"FLIP REACTION",w/2f,55,27,true);
        if (state == 0) {
            text(c,p,"폴더를 닫으세요",w/2f,h/2f,24,true);
            text(c,p,"진동이 오면 바로 OPEN",w/2f,h/2f+38,15,true);
        } else if (state == 1) {
            text(c,p,"WAITING...",w/2f,h/2f,28,true);
        } else if (state == 2) {
            text(c,p,"OPEN!",w/2f,h/2f,44,true);
        } else if (state == 3) {
            text(c,p,openMs+" ms",w/2f,h/2f-12,38,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+42,18,true);
        } else {
            text(c,p,"TOO EARLY",w/2f,h/2f,34,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+42,18,true);
        }
        text(c,p,"BACK = MENU",w/2f,h-22,13,true);
    }
}
