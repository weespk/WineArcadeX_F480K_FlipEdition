package com.winearcade.games.keypad;

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

public class RetroOperatorGame extends BaseGame {
    private final Random random = new Random();
    private String target="", input="";
    private int streak, state;
    private long deadline;

    public RetroOperatorGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName(){ return "Retro Operator"; }

    @Override public void start(){ streak=0; nextCall(); }

    private void nextCall(){
        target=String.format("%04d",random.nextInt(10000));
        input=""; state=0;
        deadline=SystemClock.elapsedRealtime()+Math.max(2200,5200-streak*180);
        sound.start();
    }

    @Override public void update(long now){
        if(state==0 && now>deadline){ state=2; sound.fail(); vibration.fail(); }
    }

    @Override public boolean onKeyDown(int keyCode){
        if(state==2 && keyCode==KeyEvent.KEYCODE_5){ start(); return true; }
        if(state==1 && keyCode==KeyEvent.KEYCODE_POUND){ nextCall(); return true; }
        if(state!=0) return false;

        if(keyCode>=KeyEvent.KEYCODE_0 && keyCode<=KeyEvent.KEYCODE_9){
            if(input.length()<4) input+=(keyCode-KeyEvent.KEYCODE_0);
            return true;
        }
        if(keyCode==KeyEvent.KEYCODE_STAR && input.length()>0){
            input=input.substring(0,input.length()-1); return true;
        }
        if(keyCode==KeyEvent.KEYCODE_CALL || keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
            if(input.equals(target)){
                streak++; state=1; save.reward(20+streak*2,15);
                sound.success(); vibration.shortPulse();
            }else{
                state=2; sound.fail(); vibration.fail();
            }
            return true;
        }
        return false;
    }

    @Override public void draw(Canvas c, Paint p, int w, int h){
        text(c,p,"RETRO OPERATOR",w/2f,45,25,true);
        text(c,p,"STREAK "+streak,w/2f,72,14,true);
        if(state==0){
            text(c,p,"INCOMING CALL",w/2f,130,18,true);
            text(c,p,"SEOUL  "+target,w/2f,190,31,true);
            text(c,p,input,w/2f,245,28,true);
            text(c,p,"CALL키 또는 OK로 연결",w/2f,300,15,true);
        }else if(state==1){
            text(c,p,"CONNECTED!",w/2f,h/2f,32,true);
            text(c,p,"# = NEXT",w/2f,h/2f+44,18,true);
        }else{
            text(c,p,"MISSED / WRONG",w/2f,h/2f,30,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+44,18,true);
        }
        text(c,p,"*=DELETE  BACK=MENU",w/2f,h-20,12,true);
    }
}
