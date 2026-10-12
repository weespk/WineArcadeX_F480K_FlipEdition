package com.winearcade;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.winearcade.device.DeviceManager;
import com.winearcade.device.FlipDetector;
import com.winearcade.ui.WineArcadeView;

public class MainActivity extends Activity implements FlipDetector.Listener {
    private DeviceManager device;
    private FlipDetector flip;
    private WineArcadeView view;
    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        device=new DeviceManager();
        view=new WineArcadeView(this,device);
        setContentView(view);

        flip=new FlipDetector(this,device,this);
        flip.register();

        powerManager=(PowerManager)getSystemService(POWER_SERVICE);
        if(powerManager!=null)
            wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"WineArcadeX:FlipGame");
        device.log("Activity.onCreate");
    }

    @Override protected void onStart(){
        super.onStart();
        device.log("Activity.onStart");
    }

    @Override protected void onResume(){
        super.onResume();
        device.log("Activity.onResume");
        if(wakeLock!=null&&!wakeLock.isHeld()){
            try{wakeLock.acquire();}catch(Exception ignored){}
        }
        view.activityResume();
    }

    @Override protected void onPause(){
        device.log("Activity.onPause");
        view.activityPause();

        // If the screen is still ON, this is likely Home/another Activity rather than
        // the flip being closed, so do not keep the CPU awake unnecessarily.
        if(powerManager!=null && powerManager.isScreenOn()){
            if(wakeLock!=null&&wakeLock.isHeld()){
                try{wakeLock.release();}catch(Exception ignored){}
            }
        }
        super.onPause();
    }

    @Override protected void onStop(){
        device.log("Activity.onStop");
        // Keep FlipDetector registered so ACTION_SCREEN_ON can be received after
        // the physical flip is opened again.
        super.onStop();
    }

    @Override protected void onDestroy(){
        if(flip!=null)flip.unregister();
        if(wakeLock!=null&&wakeLock.isHeld()){
            try{wakeLock.release();}catch(Exception ignored){}
        }
        if(view!=null)view.shutdown();
        super.onDestroy();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(e.getAction()==KeyEvent.ACTION_DOWN&&view!=null&&view.handleKey(e.getKeyCode()))return true;
        return super.dispatchKeyEvent(e);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(device!=null)device.log("WindowFocus="+hasFocus);
    }

    @Override public void onFlipOpen(long t){
        if(view!=null)view.flipOpen(t);
    }

    @Override public void onFlipClose(long t){
        if(view!=null)view.flipClose(t);
    }
}
