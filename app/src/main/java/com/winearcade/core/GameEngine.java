package com.winearcade.core;

import android.graphics.Canvas;
import android.graphics.Paint;

public class GameEngine {
    private Game game;

    public void setGame(Game next) {
        if (game != null) game.finish();
        game = next;
        if (game != null) game.start();
    }

    public Game getGame() { return game; }

    public void update(long now) {
        if (game != null) game.update(now);
    }

    public void draw(Canvas c, Paint p, int w, int h) {
        if (game != null) game.draw(c, p, w, h);
    }

    public boolean key(int keyCode) {
        return game != null && game.onKeyDown(keyCode);
    }

    public void flipOpen(long t) {
        if (game != null) game.onFlipOpen(t);
    }

    public void flipClose(long t) {
        if (game != null) game.onFlipClose(t);
    }

    public void pause() { if (game != null) game.pause(); }
    public void resume() { if (game != null) game.resume(); }
}
