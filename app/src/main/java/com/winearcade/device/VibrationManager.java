package com.winearcade.device;

import android.content.Context;
import android.os.Vibrator;

public class VibrationManager {
    private final Vibrator vibrator;

    public VibrationManager(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void shortPulse() { vibrate(90); }
    public void longPulse() { vibrate(420); }
    public void success() { pattern(new long[]{0,80,80,80}); }
    public void fail() { pattern(new long[]{0,250,90,120}); }
    public void doublePulse() { pattern(new long[]{0,100,100,100}); }
    public void triplePulse() { pattern(new long[]{0,90,80,90,80,90}); }

    public void signal(boolean[] longPulse) {
        long[] p = new long[longPulse.length * 2];
        for (int i=0; i<longPulse.length; i++) {
            p[i*2] = (i==0 ? 0 : 180);
            p[i*2+1] = longPulse[i] ? 420 : 100;
        }
        pattern(p);
    }

    private void vibrate(long ms) {
        try { if (vibrator != null) vibrator.vibrate(ms); } catch (Exception ignored) {}
    }

    private void pattern(long[] pattern) {
        try { if (vibrator != null) vibrator.vibrate(pattern, -1); } catch (Exception ignored) {}
    }
}
