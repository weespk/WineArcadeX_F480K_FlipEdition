package com.winearcade.games.signal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;

import com.winearcade.core.BaseGame;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.Random;

public class PocketSignalGame extends BaseGame {
    private final Random random = new Random();
    private boolean[] pattern;
    private int state, level=3;
    private String answer="";

    public PocketSignalGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName(){ return "Pocket Signal"; }

    @Override public void start(){ level=3; prepare(); }

    private void prepare(){
        pattern=new boolean[level];
        for(int i=0;i<level;i++) pattern[i]=random.nextBoolean();
        answer=""; state=0;
    }

    @Override public void onFlipClose(long t){
        if(state==0){ state=1; vibration.signal(pattern); }
    }

    @Override public void onFlipOpen(long t){
        if(state==1){ state=2; vibration.shortPulse(); }
    }

    @Override public boolean onKeyDown(int keyCode){
        if(state==4 && keyCode==KeyEvent.KEYCODE_5){ start(); return true; }
        if(state==3 && keyCode==KeyEvent.KEYCODE_POUND){ prepare(); return true; }
        if(state!=2) return false;

        if(keyCode==KeyEvent.KEYCODE_1){ answer+="S"; }
        else if(keyCode==KeyEvent.KEYCODE_2){ answer+="L"; }
        else if(keyCode==KeyEvent.KEYCODE_STAR && answer.length()>0){ answer=answer.substring(0,answer.length()-1); return true; }
        else return false;

        if(answer.length()==pattern.length){
            StringBuilder correct=new StringBuilder();
            for(boolean b:pattern) correct.append(b?"L":"S");
            if(answer.equals(correct.toString())){
                state=3; level=Math.min(8,level+1); save.reward(30,25);
                sound.success(); vibration.success();
            }else{
                state=4; sound.fail(); vibration.fail();
            }
        }
        return true;
    }

    @Override public void draw(Canvas c, Paint p, int w, int h){
        text(c,p,"POCKET SIGNAL",w/2f,48,26,true);
        text(c,p,"LEVEL "+level,w/2f,76,14,true);
        if(state==0){
            text(c,p,"CLOSE PHONE",w/2f,h/2f,30,true);
            text(c,p,"진동 패턴을 기억하세요",w/2f,h/2f+42,15,true);
        }else if(state==1){
            text(c,p,"FEEL THE SIGNAL",w/2f,h/2f,25,true);
        }else if(state==2){
            text(c,p,"1=SHORT  2=LONG",w/2f,150,20,true);
            text(c,p,answer,w/2f,225,34,true);
        }else if(state==3){
            text(c,p,"SIGNAL OK",w/2f,h/2f,32,true);
            text(c,p,"# = NEXT",w/2f,h/2f+42,18,true);
        }else{
            text(c,p,"WRONG SIGNAL",w/2f,h/2f,30,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+42,18,true);
        }
        text(c,p,"*=DELETE  BACK=MENU",w/2f,h-20,12,true);
    }
}
