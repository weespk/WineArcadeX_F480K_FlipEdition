package com.winearcade.games.bonus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.winearcade.core.BaseGame;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.Random;

public class RouletteGame extends BaseGame {

    private static final String[] LABELS = {
            "+10", "+20", "+30", "+50", "BONUS", "+5", "+100", "MISS"
    };

    private static final int[] REWARDS = {
            10, 20, 30, 50, 75, 5, 100, 0
    };

    private final Random random = new Random();

    private boolean spinning;
    private boolean resultReady;
    private float angle;
    private float startAngle;
    private float targetAngle;
    private long spinStart;
    private long spinDuration;
    private int resultIndex = -1;
    private int reward;

    public RouletteGame(SaveManager save, ScoreManager score,
                        VibrationManager vibration, SoundManager sound) {
        super(save, score, vibration, sound);
    }

    @Override
    public String getName() {
        return "Lucky Roulette";
    }

    @Override
    public void start() {
        finished = false;
        spinning = false;
        resultReady = false;
        resultIndex = -1;
        reward = 0;
        angle = 0f;
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_5 ||
                keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (!spinning) spin();
            return true;
        }
        return false;
    }

    private void spin() {
        spinning = true;
        resultReady = false;
        resultIndex = random.nextInt(LABELS.length);

        float slice = 360f / LABELS.length;
        float selectedCenter = resultIndex * slice + slice / 2f;

        startAngle = normalize(angle);

        // Pointer is fixed at 12 o'clock. Rotate selected segment beneath it.
        float desired = normalize(-90f - selectedCenter);
        float delta = normalize(desired - startAngle);

        targetAngle = startAngle
                + 360f * (4 + random.nextInt(3))
                + delta;

        spinStart = SystemClock.elapsedRealtime();
        spinDuration = 2300L + random.nextInt(700);

        vibration.shortPulse();
        sound.start();
    }

    @Override
    public void update(long now) {
        if (!spinning) return;

        float t = (float)(now - spinStart) / (float)spinDuration;

        if (t >= 1f) {
            angle = normalize(targetAngle);
            spinning = false;
            finishSpin();
            return;
        }

        if (t < 0f) t = 0f;

        float eased = 1f - (float)Math.pow(1f - t, 3);
        angle = startAngle + (targetAngle - startAngle) * eased;
    }

    private void finishSpin() {
        reward = REWARDS[resultIndex];
        resultReady = true;

        save.addInt("roulette_play_count", 1);

        if (reward > 0) {
            save.reward(reward, 5);

            int best = save.getInt("roulette_best_reward", 0);
            if (reward > best) {
                save.putInt("roulette_best_reward", reward);
            }

            vibration.success();
            if (reward >= 75) sound.rare();
            else sound.success();
        } else {
            vibration.fail();
            sound.fail();
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int width, int height) {
        text(canvas, paint, "LUCKY ROULETTE", width / 2f, 35, 23, true);
        text(canvas, paint, "FREE BONUS SPIN", width / 2f, 55, 12, true);

        float cx = width / 2f;
        float cy = 200f;
        float radius = Math.min(width * 0.38f, 120f);

        RectF wheel = new RectF(
                cx - radius, cy - radius,
                cx + radius, cy + radius
        );

        float slice = 360f / LABELS.length;

        for (int i = 0; i < LABELS.length; i++) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor((i % 2 == 0) ? 0xFFEAEAEA : 0xFF737373);

            canvas.drawArc(wheel, angle + i * slice, slice, true, paint);

            float a = (float)Math.toRadians(angle + i * slice + slice / 2f);
            float tx = cx + (float)Math.cos(a) * radius * 0.64f;
            float ty = cy + (float)Math.sin(a) * radius * 0.64f;

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(13f);
            paint.setColor((i % 2 == 0) ? 0xFF101010 : 0xFFFFFFFF);
            canvas.drawText(LABELS[i], tx, ty + 5f, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF101010);
        canvas.drawCircle(cx, cy, 19f, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(11f);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("WINE", cx, cy + 4f, paint);

        // pointer
        Path pointer = new Path();
        pointer.moveTo(cx, cy - radius - 5f);
        pointer.lineTo(cx - 14f, cy - radius - 30f);
        pointer.lineTo(cx + 14f, cy - radius - 30f);
        pointer.close();

        paint.setColor(0xFFFFFFFF);
        canvas.drawPath(pointer, paint);

        if (spinning) {
            text(canvas, paint, "SPINNING...", width / 2f, 360, 21, true);
        } else if (resultReady) {
            if (reward > 0) {
                text(canvas, paint, LABELS[resultIndex] + " COIN!", width / 2f, 355, 23, true);
            } else {
                text(canvas, paint, "MISS!", width / 2f, 355, 25, true);
            }
            text(canvas, paint, "5 / OK = AGAIN", width / 2f, 385, 15, true);
        } else {
            text(canvas, paint, "5 / OK = SPIN", width / 2f, 375, 18, true);
        }

        text(canvas, paint,
                "COIN " + save.getInt("coin", 0) +
                        "   BEST +" + save.getInt("roulette_best_reward", 0),
                width / 2f, 420, 12, true);

        text(canvas, paint, "BACK = MENU", width / 2f, height - 10, 11, true);
    }

    private float normalize(float value) {
        value %= 360f;
        if (value < 0f) value += 360f;
        return value;
    }
}
