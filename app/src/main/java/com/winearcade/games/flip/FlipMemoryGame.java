package com.winearcade.games.flip;

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

public class FlipMemoryGame extends BaseGame {
    private final Random random = new Random();
    private String target="", input="";
    private int level=3, state=0;
    private long hideAt;
    private boolean closed;

    public FlipMemoryGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName(){ return "Flip Memory"; }

    @Override public void start(){ level=3; next(); }

    private void next() {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<level;i++) sb.append(random.nextInt(10));
        target=sb.toString(); input=""; state=0; closed=false;
        hideAt=SystemClock.elapsedRealtime()+1800;
    }

    @Override public void update(long now){ if(state==0 && now>=hideAt) state=1; }

    @Override public void onFlipClose(long t){
        if(state>=1 && state<=2){ closed=true; state=2; }
    }

    @Override public void onFlipOpen(long t){
        if(closed && state==2){ state=3; vibration.shortPulse(); }
    }

    @Override public boolean onKeyDown(int keyCode){
        if(state==5 && (keyCode==KeyEvent.KEYCODE_5||keyCode==KeyEvent.KEYCODE_DPAD_CENTER)){ start(); return true; }
        if(state==4 && (keyCode==KeyEvent.KEYCODE_POUND||keyCode==KeyEvent.KEYCODE_DPAD_CENTER)){ next(); return true; }
        if(state!=3) return false;

        if(keyCode>=KeyEvent.KEYCODE_0 && keyCode<=KeyEvent.KEYCODE_9){
            int n=keyCode-KeyEvent.KEYCODE_0;
            input+=n;
            if(input.length()==target.length()){
                if(input.equals(target)){
                    state=4; level++; save.reward(25,20); sound.success(); vibration.success();
                }else{
                    state=5; sound.fail(); vibration.fail();
                }
            }
            return true;
        }
        return false;
    }

    @Override public void draw(Canvas c, Paint p, int w, int h){
        text(c,p,"FLIP MEMORY",w/2f,48,26,true);
        text(c,p,"LEVEL "+level,w/2f,75,15,true);
        if(state==0) text(c,p,target,w/2f,h/2f,42,true);
        else if(state==1) text(c,p,"CLOSE PHONE",w/2f,h/2f,28,true);
        else if(state==2) text(c,p,"MEMORIZE...",w/2f,h/2f,24,true);
        else if(state==3){
            text(c,p,"INPUT",w/2f,150,18,true);
            text(c,p,input,w/2f,220,38,true);
        } else if(state==4){
            text(c,p,"CORRECT!",w/2f,h/2f-20,32,true);
            text(c,p,"# = NEXT",w/2f,h/2f+30,18,true);
        } else {
            text(c,p,"MISS  "+target,w/2f,h/2f-15,30,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+35,18,true);
        }
        text(c,p,"BACK = MENU",w/2f,h-20,13,true);
    }
}
