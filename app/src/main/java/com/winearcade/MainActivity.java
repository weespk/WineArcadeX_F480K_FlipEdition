package com.winearcade;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.winearcade.device.DeviceManager;
import com.winearcade.device.FlipDetector;
import com.winearcade.ui.WineArcadeView;
import com.winearcade.service.FlipKeepAliveService;

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

        startService(new Intent(this, FlipKeepAliveService.class));

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
        syncFlipRestoreState();
    }

    @Override protected void onPause(){
        device.log("Activity.onPause");
        view.activityPause();

        boolean screenOn = powerManager == null || powerManager.isScreenOn();

        if(!screenOn && view.needsFlipRestore()){
            // Keep the restore flag armed across the physical close/open cycle.
            setFlipRestoreArmed(true);
            device.log("Flip restore ARMED (screen off)");
        }

        if(screenOn){
            // Home / another Activity while the screen remains ON is not a flip close.
            // Disarm so Wine Arcade X does not unexpectedly jump to the foreground later.
            setFlipRestoreArmed(false);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (device != null && intent != null && intent.getBooleanExtra("flip_restore", false)) {
            device.log("Activity restored by FlipKeepAliveService");
        }
        if (view != null) {
            view.activityResume();
            view.requestFocus();
            syncFlipRestoreState();
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(e.getAction()==KeyEvent.ACTION_DOWN && view!=null && view.handleKey(e.getKeyCode())){
            syncFlipRestoreState();
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    private void syncFlipRestoreState(){
        if(view==null) return;
        setFlipRestoreArmed(view.needsFlipRestore());
    }

    private void setFlipRestoreArmed(boolean armed){
        Intent intent = new Intent(this, FlipKeepAliveService.class);
        intent.setAction(armed ? FlipKeepAliveService.ACTION_ARM : FlipKeepAliveService.ACTION_DISARM);
        startService(intent);
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
