package com.winearcade.device;

import android.view.KeyEvent;

public class KeyInputManager {
    public String describe(int keyCode) {
        return KeyEvent.keyCodeToString(keyCode) + " = " + keyCode;
    }
}
