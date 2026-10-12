package com.winearcade.games.flip;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;

import com.winearcade.core.BaseGame;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VaultPhoneGame extends BaseGame {
    private final Random random = new Random();
    private final List<String> pattern = new ArrayList<String>();
    private int pos, level=3, state;

    public VaultPhoneGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName(){ return "Vault Phone"; }

    @Override public void start(){ level=3; makePattern(); }

    private void makePattern(){
        pattern.clear(); pos=0; state=0;
        String[] digits={"2","4","6","8"};

        // The phone starts open. A CLOSE is always followed by OPEN,
        // and physical key presses are only generated while the phone is open.
        for(int i=0;i<level;i++){
            if(random.nextBoolean()){
                pattern.add(digits[random.nextInt(digits.length)]);
            }else{
                pattern.add("CLOSE");
                pattern.add("OPEN");
            }
        }
    }

    private void accept(String action){
        if(state!=1) return;
        if(pos<pattern.size() && pattern.get(pos).equals(action)){
            pos++; sound.start();
            if(pos==pattern.size()){
                state=2; level=Math.min(8,level+1); save.reward(35,25);
                vibration.success(); sound.success();
            }
        }else{
            state=3; vibration.fail(); sound.fail();
        }
    }

    @Override public void onFlipClose(long t){ accept("CLOSE"); }
    @Override public void onFlipOpen(long t){ accept("OPEN"); }

    @Override public boolean onKeyDown(int keyCode){
        if(state==0 && (keyCode==KeyEvent.KEYCODE_5||keyCode==KeyEvent.KEYCODE_DPAD_CENTER)){ state=1; return true; }
        if(state==2 && (keyCode==KeyEvent.KEYCODE_POUND||keyCode==KeyEvent.KEYCODE_DPAD_CENTER)){ makePattern(); return true; }
        if(state==3 && (keyCode==KeyEvent.KEYCODE_5||keyCode==KeyEvent.KEYCODE_DPAD_CENTER)){ makePattern(); return true; }
        if(keyCode==KeyEvent.KEYCODE_2){accept("2");return true;}
        if(keyCode==KeyEvent.KEYCODE_4){accept("4");return true;}
        if(keyCode==KeyEvent.KEYCODE_6){accept("6");return true;}
        if(keyCode==KeyEvent.KEYCODE_8){accept("8");return true;}
        return false;
    }

    @Override public void draw(Canvas c, Paint p, int w, int h){
        text(c,p,"VAULT PHONE",w/2f,48,26,true);
        text(c,p,"LEVEL "+level,w/2f,76,15,true);

        if(state==0){
            int y=125;
            for(String a:pattern){ text(c,p,a,w/2f,y,22,true); y+=30; }
            text(c,p,"5 = START",w/2f,h-58,17,true);
        }else if(state==1){
            text(c,p,"STEP "+(pos+1)+" / "+pattern.size(),w/2f,150,20,true);
            text(c,p,"DO THE PATTERN",w/2f,215,25,true);
        }else if(state==2){
            text(c,p,"UNLOCKED!",w/2f,h/2f,32,true);
            text(c,p,"# = NEXT",w/2f,h/2f+44,18,true);
        }else{
            text(c,p,"LOCKED!",w/2f,h/2f,34,true);
            text(c,p,"5 = RETRY",w/2f,h/2f+44,18,true);
        }
        text(c,p,"BACK = MENU",w/2f,h-20,13,true);
    }
}
