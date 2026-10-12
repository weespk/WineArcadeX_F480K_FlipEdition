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

public class SecretDialGame extends BaseGame {
    private final Random random = new Random();
    private String target="", input="";
    private int digits=4, state;
    private long hideAt;

    public SecretDialGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName(){ return "Secret Dial"; }
    @Override public void start(){ digits=4; next(); }

    private void next(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<digits;i++) sb.append(random.nextInt(10));
        target=sb.toString(); input=""; state=0; hideAt=SystemClock.elapsedRealtime()+1800;
    }

    @Override public void update(long now){ if(state==0 && now>=hideAt) state=1; }

    @Override public boolean onKeyDown(int keyCode){
        if(state==3 && keyCode==KeyEvent.KEYCODE_5){ start(); return true; }
        if(state==2 && keyCode==KeyEvent.KEYCODE_POUND){ next(); return true; }
        if(state!=1) return false;

        if(keyCode>=KeyEvent.KEYCODE_0 && keyCode<=KeyEvent.KEYCODE_9){
            input += (keyCode-KeyEvent.KEYCODE_0);
            if(input.length()==target.length()){
                if(input.equals(target)){
                    state=2; digits=Math.min(10,digits+2); save.reward(30,20);
                    sound.success(); vibration.success();
                }else{
                    state=3; sound.fail(); vibration.fail();
                }
            }
            return true;
        }
        if(keyCode==KeyEvent.KEYCODE_STAR && input.length()>0){
            input=input.substring(0,input.length()-1); return true;
        }
        return false;
    }

    @Override public void draw(Canvas c, Paint p, int w, int h){
        text(c,p,"SECRET DIAL",w/2f,48,27,true);
        text(c,p,digits+" DIGITS",w/2f,76,14,true);
        if(state==0){
            text(c,p,target,w/2f,h/2f,36,true);
            text(c,p,"REMEMBER",w/2f,h/2f+40,15,true);
        }else if(state==1){
            text(c,p,input,w/2f,h/2f,34,true);
            text(c,p,"TYPE THE NUMBER",w/2f,h/2f+42,16,true);
        }else if(state==2){
            text(c,p,"CONNECTED!",w/2f,h/2f,30,true);
            text(c,p,"# = NEXT",w/2f,h/2f+44,18,true);
        }else{
            text(c,p,"WRONG  "+target,w/2f,h/2f,27,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+44,18,true);
        }
        text(c,p,"*=DELETE  BACK=MENU",w/2f,h-20,12,true);
    }
}
