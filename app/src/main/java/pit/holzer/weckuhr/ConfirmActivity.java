package pit.holzer.weckuhr;

import java.util.Arrays;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MotionEvent;
import android.view.WindowManager;

public class ConfirmActivity extends Activity {
    View confirmView;    
    float firstX = -1f;
    private final Handler mHandler = new Handler();
    PowerManager.WakeLock wl;	
	int viewWidth=200;
	long waitUntilRing=2*60*1000;
	
	private AlarmRepeater mTask = new AlarmRepeater(mHandler);
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm);			
		confirmView= findViewById(R.id.confirmArea);
		mTask.setup(this);
		confirmView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
            	
                float x = e.getX(0);
                int action=e.getAction();
                int actionCode = action & MotionEvent.ACTION_MASK;
                switch (actionCode) {
                    case MotionEvent.ACTION_MOVE:{                    	
                		if(Math.abs(x-firstX)>viewWidth){
                			stopIt();
                		}break;
                    }	
                    case MotionEvent.ACTION_DOWN: {                    	
                    	firstX=x;
                    	viewWidth=confirmView.getWidth()*4/5;                    	
                    }
                }    		
                return true;
            }
       });
	}
	
	private void stopIt () {
		viewWidth=10000;
		mTask.stopAlarm();
		android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT ,1000*2*60);
		Intent myIntent = new Intent(this, UhrActivity.class);			    
	    myIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);	    
	    startActivity(myIntent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.confirm, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
	try {	  
      super.onResume();     
      wl=((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
      		PowerManager.FULL_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP| PowerManager.ON_AFTER_RELEASE, "weckuhr:mainWakelock");
      wl.acquire();
      mTask.reset(); 
      mHandler.removeCallbacks(mTask);
	  mHandler.postDelayed(mTask, waitUntilRing);
	} catch (Exception e) {Log.e("AlarmConfirm"," resume "+e.toString());Log.e("AlarmConfirm",Arrays.toString(e.getStackTrace()));}
	}
	
	@Override protected void onPause() {
		Log.d("AlarmConfirm","OnPause "+wl);
		mHandler.removeCallbacks(mTask);
		wl.release();      
	  super.onPause();		
	}
	
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

}
