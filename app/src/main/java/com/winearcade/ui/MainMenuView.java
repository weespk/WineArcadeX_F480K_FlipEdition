package com.winearcade.ui;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.winearcade.core.MissionManager;
import com.winearcade.core.SaveManager;

public class MainMenuView {
    private final SaveManager save;
    private final MissionManager missions;

    public MainMenuView(SaveManager save, MissionManager missions){
        this.save=save; this.missions=missions;
    }

    private void text(Canvas c,Paint p,String s,float x,float y,float size,boolean center){
        p.setColor(0xFFFFFFFF);p.setStyle(Paint.Style.FILL);p.setTextSize(size);
        p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,p);
    }

    public void draw(Canvas c,Paint p,int w,int h){
        text(c,p,"WINE ARCADE X",w/2f,38,25,true);
        text(c,p,"FLIP EDITION",w/2f,60,13,true);

        text(c,p,"LV "+save.getLevel()+"   COIN "+save.getInt("coin",0),w/2f,88,15,true);

        String[] m=missions.today();
        text(c,p,"TODAY MISSION",w/2f,116,14,true);
        text(c,p,"- "+m[0],24,138,12,false);
        text(c,p,"- "+m[1],24,156,12,false);
        text(c,p,"- "+m[2],24,174,12,false);

        text(c,p,"1  FLIP GAMES",28,215,18,false);
        text(c,p,"2  KEYPAD GAMES",28,247,18,false);
        text(c,p,"3  SIGNAL GAMES",28,279,18,false);
        text(c,p,"4  CLASSIC",28,311,18,false);
        text(c,p,"5  RECORD",28,343,18,false);
        text(c,p,"6  DEVICE LAB",28,375,18,false);

        text(c,p,"0 EXIT",w/2f,h-22,14,true);
    }
}
