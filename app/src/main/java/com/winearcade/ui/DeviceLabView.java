package com.winearcade.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;

import com.winearcade.device.DeviceManager;
import com.winearcade.device.KeyInputManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.List;

public class DeviceLabView {
    private final DeviceManager device;
    private final KeyInputManager keys;
    private final VibrationManager vibration;
    private final SoundManager sound;
    private int page=0;
    private String lastKey="NO KEY";

    public DeviceLabView(DeviceManager d,KeyInputManager k,VibrationManager v,SoundManager s){
        device=d;keys=k;vibration=v;sound=s;
    }

    public void reset(){page=0;}

    public boolean key(int keyCode){
        if(page==0){
            if(keyCode>=KeyEvent.KEYCODE_1&&keyCode<=KeyEvent.KEYCODE_6){
                page=keyCode-KeyEvent.KEYCODE_0; return true;
            }
        }else{
            lastKey=keys.describe(keyCode);device.log("KEY "+lastKey);
            if(page==3){
                if(keyCode==KeyEvent.KEYCODE_1)vibration.shortPulse();
                else if(keyCode==KeyEvent.KEYCODE_2)vibration.longPulse();
                else if(keyCode==KeyEvent.KEYCODE_3)vibration.doublePulse();
                else if(keyCode==KeyEvent.KEYCODE_4)vibration.triplePulse();
            }
            if(page==4){
                if(keyCode==KeyEvent.KEYCODE_1)sound.start();
                else if(keyCode==KeyEvent.KEYCODE_2)sound.success();
                else if(keyCode==KeyEvent.KEYCODE_3)sound.fail();
            }
        }
        return page != 0;
    }

    private void text(Canvas c,Paint p,String s,float x,float y,float size,boolean center){
        p.setColor(0xFFFFFFFF);p.setTextSize(size);p.setStyle(Paint.Style.FILL);
        p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,p);
    }

    public void draw(Canvas c,Paint p,int w,int h){
        text(c,p,"DEVICE LAB",w/2f,35,24,true);
        if(page==0){
            text(c,p,"1 KEY TEST",24,85,18,false);
            text(c,p,"2 FLIP TEST",24,120,18,false);
            text(c,p,"3 VIBRATION TEST",24,155,18,false);
            text(c,p,"4 SOUND TEST",24,190,18,false);
            text(c,p,"5 SCREEN EVENT TEST",24,225,18,false);
            text(c,p,"6 DEVICE INFO",24,260,18,false);
        }else if(page==1){
            text(c,p,"KEY TEST",w/2f,70,18,true);
            text(c,p,lastKey,w/2f,125,18,true);
            drawLog(c,p,165);
        }else if(page==2||page==5){
            text(c,p,page==2?"FLIP TEST":"SCREEN EVENTS",w/2f,70,18,true);
            text(c,p,"폴더를 열고 닫아 로그 확인",w/2f,96,13,true);
            drawLog(c,p,125);
        }else if(page==3){
            text(c,p,"1 SHORT  2 LONG",w/2f,100,18,true);
            text(c,p,"3 DOUBLE 4 TRIPLE",w/2f,135,18,true);
            text(c,p,lastKey,w/2f,190,14,true);
        }else if(page==4){
            text(c,p,"1 START 2 SUCCESS 3 FAIL",w/2f,115,16,true);
            text(c,p,lastKey,w/2f,170,14,true);
        }else if(page==6){
            text(c,p,device.summary(),w/2f,105,13,true);
            drawLog(c,p,150);
        }
        text(c,p,"BACK = DEVICE LAB / MENU",w/2f,h-18,12,true);
    }

    private void drawLog(Canvas c,Paint p,int y){
        List<String> lines=device.snapshot();
        int yy=y;
        for(int i=0;i<lines.size()&&i<10;i++){text(c,p,lines.get(i),12,yy,10,false);yy+=22;}
    }

    public int getPage(){return page;}
    public void back(){if(page==0){}else page=0;}
}
