package com.winearcade.device;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundManager {
    private final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 65);

    public void start() { play(ToneGenerator.TONE_PROP_BEEP, 90); }
    public void success() { play(ToneGenerator.TONE_PROP_ACK, 120); }
    public void fail() { play(ToneGenerator.TONE_PROP_NACK, 150); }
    public void rare() { play(ToneGenerator.TONE_DTMF_9, 170); }
    public void levelUp() { play(ToneGenerator.TONE_DTMF_6, 150); }

    private void play(int code, int ms) {
        try { tone.startTone(code, ms); } catch (Exception ignored) {}
    }

    public void release() {
        try { tone.release(); } catch (Exception ignored) {}
    }
}
