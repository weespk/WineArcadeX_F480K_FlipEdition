package com.winearcade.games.classic;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.KeyEvent;

import com.winearcade.core.BaseGame;
import com.winearcade.core.SaveManager;
import com.winearcade.core.ScoreManager;
import com.winearcade.device.SoundManager;
import com.winearcade.device.VibrationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClassicHubGame extends BaseGame {
    private final Random random=new Random();
    private int mode=0, scoreValue=0;
    private final List<Point> snake=new ArrayList<Point>();
    private Point food=new Point(8,8);
    private int dx=1,dy=0;
    private long tick;

    public ClassicHubGame(SaveManager s, ScoreManager sc, VibrationManager v, SoundManager snd){
        super(s,sc,v,snd);
    }

    @Override public String getName(){return "Classic Pack";}
    @Override public void start(){mode=0;scoreValue=0;}

    private void startSnake(){
        mode=1;scoreValue=0;snake.clear();
        snake.add(new Point(7,10));snake.add(new Point(6,10));snake.add(new Point(5,10));
        dx=1;dy=0;food=new Point(random.nextInt(16),random.nextInt(20));tick=0;
    }

    @Override public void update(long now){
        if(mode!=1||now-tick<180)return;
        tick=now;
        Point h=snake.get(0); int nx=h.x+dx,ny=h.y+dy;
        if(nx<0||nx>=16||ny<0||ny>=20||contains(nx,ny)){
            vibration.fail();sound.fail();mode=2;return;
        }
        snake.add(0,new Point(nx,ny));
        if(nx==food.x&&ny==food.y){
            scoreValue+=10;food=new Point(random.nextInt(16),random.nextInt(20));sound.start();
        }else snake.remove(snake.size()-1);
    }

    private boolean contains(int x,int y){for(Point q:snake)if(q.x==x&&q.y==y)return true;return false;}

    @Override public boolean onKeyDown(int k){
        if(mode==0){
            if(k==KeyEvent.KEYCODE_1){startSnake();return true;}
            return false;
        }
        if(mode==2&&k==KeyEvent.KEYCODE_5){startSnake();return true;}
        if(k==KeyEvent.KEYCODE_2&&dy!=1){dx=0;dy=-1;return true;}
        if(k==KeyEvent.KEYCODE_8&&dy!=-1){dx=0;dy=1;return true;}
        if(k==KeyEvent.KEYCODE_4&&dx!=1){dx=-1;dy=0;return true;}
        if(k==KeyEvent.KEYCODE_6&&dx!=-1){dx=1;dy=0;return true;}
        return false;
    }

    @Override public void draw(Canvas c,Paint p,int w,int h){
        text(c,p,"CLASSIC",w/2f,40,27,true);
        if(mode==0){
            text(c,p,"1  SNAKE",28,95,20,false);
            text(c,p,"2  TETRIS      (V3 유지 예정)",28,130,15,false);
            text(c,p,"3  BREAKOUT    (V3 유지 예정)",28,160,15,false);
            text(c,p,"4  DODGE       (V3 유지 예정)",28,190,15,false);
            text(c,p,"5  REACTION    (V3 유지 예정)",28,220,15,false);
            text(c,p,"6  BASEBALL    (V3 유지 예정)",28,250,15,false);
            text(c,p,"7  LIGHTS OUT  (V3 유지 예정)",28,280,15,false);
            text(c,p,"8  MEMORY      (V3 유지 예정)",28,310,15,false);
            text(c,p,"9  MAZE        (V3 유지 예정)",28,340,15,false);
        }else{
            text(c,p,"SNAKE "+scoreValue,w/2f,66,16,true);
            int cell=Math.min(w/16,(h-95)/20),ox=(w-cell*16)/2,oy=78;
            p.setColor(0xFFFFFFFF);p.setStyle(Paint.Style.FILL);
            for(Point q:snake)c.drawRect(ox+q.x*cell+1,oy+q.y*cell+1,ox+(q.x+1)*cell-1,oy+(q.y+1)*cell-1,p);
            p.setColor(0xFF888888);c.drawCircle(ox+food.x*cell+cell/2f,oy+food.y*cell+cell/2f,cell*.34f,p);
            if(mode==2)text(c,p,"GAME OVER - 5 RETRY",w/2f,h/2f,19,true);
        }
        text(c,p,"BACK = MENU",w/2f,h-18,12,true);
    }
}
