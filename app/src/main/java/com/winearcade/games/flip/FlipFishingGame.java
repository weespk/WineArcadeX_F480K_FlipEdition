package com.winearcade.games.flip;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;

import com.winearcade.core.BaseGame;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.Random;

public class FlipFishingGame extends BaseGame {
    private final Random random = new Random();
    private int state;
    private long biteAt;
    private boolean closed;
    private long reaction;
    private String fish="", rarity="", result="";
    private int cm;

    public FlipFishingGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd) {
        super(s, sc, v, snd);
    }

    @Override public String getName() { return "Flip Fishing"; }

    @Override public void start() {
        state=0; closed=false; reaction=-1; fish=""; result=""; rarity=""; finished=false; sound.start();
    }

    @Override public boolean onKeyDown(int keyCode) {
        if (keyCode==KeyEvent.KEYCODE_5 || keyCode==KeyEvent.KEYCODE_DPAD_CENTER) { start(); return true; }
        return false;
    }

    @Override public void onFlipClose(long t) {
        if (state==0) {
            closed=true; state=1;
            biteAt=t+1000+random.nextInt(5000);
        }
    }

    @Override public void update(long now) {
        if (state==1 && now>=biteAt) {
            state=2; vibration.triplePulse(); sound.start();
        }
    }

    @Override public void onFlipOpen(long t) {
        if (!closed) return;
        if (state==1 && t<biteAt) {
            state=4; result="TOO EARLY"; vibration.fail(); sound.fail(); return;
        }
        if (state!=2) return;

        reaction=t-biteAt;
        if (reaction>1000) {
            state=4; result="ESCAPED"; vibration.fail(); sound.fail(); return;
        }

        if (reaction<=350) result="PERFECT";
        else if (reaction<=650) result="GOOD";
        else result="LATE";

        int roll=random.nextInt(100);
        if (roll<8) rarity="EPIC";
        else if (roll<30) rarity="RARE";
        else rarity="COMMON";

        String[] common={"붕어","피라미","블루길"};
        String[] rare={"잉어","메기","가물치"};
        String[] epic={"황금잉어","대왕메기","전설붕어"};
        String[] pool="EPIC".equals(rarity)?epic:("RARE".equals(rarity)?rare:common);
        fish=pool[random.nextInt(pool.length)];
        cm=12+random.nextInt("EPIC".equals(rarity)?85:("RARE".equals(rarity)?55:32));

        save.addInt("fish_total",1);
        save.addInt("fish_"+rarity,1);
        save.putString("last_fish",fish+" "+cm+"cm "+rarity);
        save.reward("EPIC".equals(rarity)?120:("RARE".equals(rarity)?60:25), 20);
        state=3;
        vibration.success();
        if ("EPIC".equals(rarity)) sound.rare(); else sound.success();
    }

    @Override public void draw(Canvas c, Paint p, int w, int h) {
        text(c,p,"FLIP FISHING",w/2f,52,28,true);
        if (state==0) {
            text(c,p,"폴더를 닫아",w/2f,h/2f-12,24,true);
            text(c,p,"낚싯대를 던지세요",w/2f,h/2f+24,22,true);
        } else if (state==1) {
            text(c,p,"찌를 기다리는 중...",w/2f,h/2f,23,true);
        } else if (state==2) {
            text(c,p,"HIT! OPEN!",w/2f,h/2f,38,true);
        } else if (state==3) {
            text(c,p,result+"  "+reaction+"ms",w/2f,135,21,true);
            text(c,p,fish,w/2f,205,34,true);
            text(c,p,cm+" cm",w/2f,245,24,true);
            text(c,p,rarity,w/2f,285,22,true);
            text(c,p,"5 = AGAIN",w/2f,h-52,16,true);
        } else {
            text(c,p,result,w/2f,h/2f,36,true);
            text(c,p,"5 = AGAIN",w/2f,h/2f+48,17,true);
        }
        text(c,p,"BACK = MENU",w/2f,h-20,13,true);
    }
}
