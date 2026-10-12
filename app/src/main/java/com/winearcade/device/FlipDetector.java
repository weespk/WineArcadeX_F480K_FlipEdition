package com.winearcade.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class FlipDetector {
    public interface Listener {
        void onFlipOpen(long t);
        void onFlipClose(long t);
    }

    private final Context context;
    private final DeviceManager device;
    private final Listener listener;
    private boolean registered;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent intent) {
            long t = SystemClock.elapsedRealtime();
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                device.log("ACTION_SCREEN_OFF");
                listener.onFlipClose(t);
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                device.log("ACTION_SCREEN_ON");
                listener.onFlipOpen(t);
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                device.log("ACTION_USER_PRESENT");
            }
        }
    };

    public FlipDetector(Context context, DeviceManager device, Listener listener) {
        this.context = context;
        this.device = device;
        this.listener = listener;
    }

    public void register() {
        if (registered) return;
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(receiver, f);
        registered = true;
        device.log("FlipDetector.register");
    }

    public void unregister() {
        if (!registered) return;
        try { context.unregisterReceiver(receiver); } catch (Exception ignored) {}
        registered = false;
        device.log("FlipDetector.unregister");
    }
}
