package com.winearcade.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import com.winearcade.MainActivity;

public class FlipKeepAliveService extends Service {

    public static final String ACTION_ARM = "com.winearcade.action.ARM_FLIP_RESTORE";
    public static final String ACTION_DISARM = "com.winearcade.action.DISARM_FLIP_RESTORE";

    private static final String PREF = "wine_flip_service";
    private static final String KEY_ARMED = "armed";

    private boolean armed = false;
    private boolean receiverRegistered = false;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action) && armed) {
                bringGameToFront();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences(PREF, MODE_PRIVATE);
        armed = prefs.getBoolean(KEY_ARMED, false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, filter);
        receiverRegistered = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_ARM.equals(action)) setArmed(true);
            else if (ACTION_DISARM.equals(action)) setArmed(false);
        }
        return START_STICKY;
    }

    private void setArmed(boolean value) {
        armed = value;
        getSharedPreferences(PREF, MODE_PRIVATE)
                .edit().putBoolean(KEY_ARMED, value).apply();
    }

    private void bringGameToFront() {
        try {
            Intent launch = new Intent(this, MainActivity.class);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            launch.putExtra("flip_restore", true);
            launch.putExtra("flip_restore_time", SystemClock.elapsedRealtime());
            startActivity(launch);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        if (receiverRegistered) {
            try { unregisterReceiver(screenReceiver); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
