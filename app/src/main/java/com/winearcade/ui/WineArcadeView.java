package com.winearcade.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;

import com.winearcade.core.Game;
import com.winearcade.core.GameEngine;
import com.winearcade.core.MissionManager;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.DeviceManager;
import com.winearcade.device.KeyInputManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;
import com.winearcade.games.classic.ClassicHubGame;
import com.winearcade.games.flip.FlipFishingGame;
import com.winearcade.games.flip.FlipMemoryGame;
import com.winearcade.games.flip.FlipReactionGame;
import com.winearcade.games.flip.VaultPhoneGame;
import com.winearcade.games.keypad.RetroOperatorGame;
import com.winearcade.games.keypad.SecretDialGame;
import com.winearcade.games.signal.PocketSignalGame;

public class WineArcadeView extends View {
    private static final int MAIN=0, FLIP=1, KEYPAD=2, SIGNAL=3, RECORD=4, LAB=5, GAME=6;

    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler=new Handler();

    private final SaveManager save;
    private final ScoreManager score;
    private final MissionManager missions=new MissionManager();
    private final VibrationManager vibration;
    private final SoundManager sound;
    private final DeviceManager device;
    private final KeyInputManager keys=new KeyInputManager();
    private final GameEngine engine=new GameEngine();

    private final MainMenuView mainMenu;
    private final DeviceLabView lab;

    private int screen=MAIN;

    private final Runnable loop=new Runnable(){
        @Override public void run(){
            if(screen==GAME){
                engine.update(SystemClock.elapsedRealtime());
                Game g=engine.getGame();
                if(g!=null&&g.isFinished())screen=MAIN;
            }
            invalidate();
            handler.postDelayed(this,33);
        }
    };

    public WineArcadeView(Context context, DeviceManager device){
        super(context);
        setFocusable(true);setFocusableInTouchMode(true);requestFocus();
        this.device=device;
        save=new SaveManager(context);
        score=new ScoreManager(save);
        vibration=new VibrationManager(context);
        sound=new SoundManager();
        mainMenu=new MainMenuView(save,missions);
        lab=new DeviceLabView(device,keys,vibration,sound);
        handler.post(loop);
    }

    public boolean needsFlipRestore() {
        if (screen == LAB) return lab.getPage() == 2 || lab.getPage() == 5;
        if (screen != GAME) return false;
        Game g = engine.getGame();
        return g instanceof FlipFishingGame ||
               g instanceof FlipReactionGame ||
               g instanceof FlipMemoryGame ||
               g instanceof VaultPhoneGame ||
               g instanceof PocketSignalGame;
    }

    public GameEngine getEngine(){return engine;}
    public SoundManager getSound(){return sound;}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        c.drawRGB(10,14,18);
        int w=getWidth(),h=getHeight();

        if(screen==MAIN)mainMenu.draw(c,paint,w,h);
        else if(screen==FLIP)drawList(c,w,h,"FLIP GAMES",
                new String[]{"1 FLIP FISHING","2 FLIP REACTION","3 FLIP MEMORY","4 VAULT PHONE"});
        else if(screen==KEYPAD)drawList(c,w,h,"KEYPAD GAMES",
                new String[]{"1 SECRET DIAL","2 RETRO OPERATOR"});
        else if(screen==SIGNAL)drawList(c,w,h,"SIGNAL GAMES",
                new String[]{"1 POCKET SIGNAL"});
        else if(screen==RECORD)drawRecord(c,w,h);
        else if(screen==LAB)lab.draw(c,paint,w,h);
        else if(screen==GAME)engine.draw(c,paint,w,h);
    }

    private void drawList(Canvas c,int w,int h,String title,String[] items){
        text(c,title,w/2f,48,25,true);
        int y=105;
        for(String s:items){text(c,s,28,y,18,false);y+=40;}
        text(c,"BACK = MENU",w/2f,h-22,13,true);
    }

    private void drawRecord(Canvas c,int w,int h){
        text(c,"RECORD",w/2f,48,26,true);
        text(c,"LV "+save.getLevel()+"  XP "+save.getInt("xp",0),25,95,16,false);
        text(c,"COIN "+save.getInt("coin",0),25,125,16,false);
        text(c,"FISH "+save.getInt("fish_total",0),25,165,16,false);
        text(c,"  COMMON "+save.getInt("fish_COMMON",0),25,193,14,false);
        text(c,"  RARE   "+save.getInt("fish_RARE",0),25,218,14,false);
        text(c,"  EPIC   "+save.getInt("fish_EPIC",0),25,243,14,false);
        text(c,"LAST "+save.getString("last_fish","-"),25,285,14,false);
        text(c,"BEST REACTION "+save.getLong("best_flip_reaction",0)+"ms",25,325,14,false);
        text(c,"BACK = MENU",w/2f,h-22,13,true);
    }

    private void text(Canvas c,String s,float x,float y,float size,boolean center){
        paint.setColor(0xFFFFFFFF);paint.setTextSize(size);paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,paint);
    }

    public boolean handleKey(int k){
        device.log("KEY "+keys.describe(k));

        if(k==KeyEvent.KEYCODE_BACK){
            if(screen==LAB&&lab.getPage()!=0){lab.back();return true;}
            if(screen==GAME){engine.setGame(null);screen=MAIN;return true;}
            if(screen!=MAIN){screen=MAIN;return true;}
            return false;
        }

        if(screen==MAIN){
            if(k==KeyEvent.KEYCODE_1){screen=FLIP;return true;}
            if(k==KeyEvent.KEYCODE_2){screen=KEYPAD;return true;}
            if(k==KeyEvent.KEYCODE_3){screen=SIGNAL;return true;}
            if(k==KeyEvent.KEYCODE_4){startGame(new ClassicHubGame(save,score,vibration,sound));return true;}
            if(k==KeyEvent.KEYCODE_5){screen=RECORD;return true;}
            if(k==KeyEvent.KEYCODE_6){lab.reset();screen=LAB;return true;}
            if(k==KeyEvent.KEYCODE_0){((Activity)getContext()).finish();return true;}
        }else if(screen==FLIP){
            if(k==KeyEvent.KEYCODE_1){startGame(new FlipFishingGame(save,score,vibration,sound));return true;}
            if(k==KeyEvent.KEYCODE_2){startGame(new FlipReactionGame(save,score,vibration,sound));return true;}
            if(k==KeyEvent.KEYCODE_3){startGame(new FlipMemoryGame(save,score,vibration,sound));return true;}
            if(k==KeyEvent.KEYCODE_4){startGame(new VaultPhoneGame(save,score,vibration,sound));return true;}
        }else if(screen==KEYPAD){
            if(k==KeyEvent.KEYCODE_1){startGame(new SecretDialGame(save,score,vibration,sound));return true;}
            if(k==KeyEvent.KEYCODE_2){startGame(new RetroOperatorGame(save,score,vibration,sound));return true;}
        }else if(screen==SIGNAL){
            if(k==KeyEvent.KEYCODE_1){startGame(new PocketSignalGame(save,score,vibration,sound));return true;}
        }else if(screen==LAB){
            return lab.key(k);
        }else if(screen==GAME){
            return engine.key(k);
        }

        return false;
    }

    private void startGame(Game game){
        engine.setGame(game);
        screen=GAME;
    }

    public void flipOpen(long t){device.log("FLIP OPEN candidate");engine.flipOpen(t);}
    public void flipClose(long t){device.log("FLIP CLOSE candidate");engine.flipClose(t);}
    public void activityPause(){engine.pause();}
    public void activityResume(){engine.resume();requestFocus();}

    public void shutdown(){
        handler.removeCallbacksAndMessages(null);
        sound.release();
    }
}
