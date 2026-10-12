package com.winearcade.device;

import android.os.Build;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

public class DeviceManager {
    private final List<String> log = new ArrayList<String>();

    public synchronized void log(String event) {
        String line = SystemClock.elapsedRealtime() + "  " + event;
        log.add(0, line);
        while (log.size() > 18) log.remove(log.size()-1);
    }

    public synchronized List<String> snapshot() {
        return new ArrayList<String>(log);
    }

    public String summary() {
        return Build.MANUFACTURER + " " + Build.MODEL + " / Android " + Build.VERSION.RELEASE +
                " / API " + Build.VERSION.SDK_INT;
    }
}
