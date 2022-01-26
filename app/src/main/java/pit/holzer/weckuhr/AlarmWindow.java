package pit.holzer.weckuhr;


import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class AlarmWindow extends Activity {
	private TextView currTimeField;
	PowerManager.WakeLock wl;	
	private final Handler mHandler = new Handler();	
	private static final DateFormat df=DateFormat.getTimeInstance(DateFormat.SHORT);	
	private AlarmRepeater mTask = new AlarmRepeater(mHandler);	
	
	
	@Override protected void onCreate(Bundle savedInstanceState) {	
		try {			
		super.onCreate(savedInstanceState);		
		Log.d("AlarmWindow","created");
		setContentView(R.layout.alarm_window);
		currTimeField= (TextView) findViewById(R.id.alarmWindowText);        
		mTask.setup(this);
        android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT ,120000);
	  } catch (Exception e) {Log.e("AlarmWindow"," create "+e.toString());Log.e("AlarmWindow",Arrays.toString(e.getStackTrace()));}
	}
	
	@Override
	protected void onResume() {
	try {	  
      super.onResume();  
      mTask.reset();
      wl=((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
      		PowerManager.FULL_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP| PowerManager.ON_AFTER_RELEASE, "peter:weckuhr");
      wl.acquire(18000000L);
      Log.d("AlarmWindow","OnResume "+wl);
      currTimeField.setText("Es ist "+df.format(new Date(System.currentTimeMillis()))+" !");      
      mHandler.removeCallbacks(mTask);
	  mHandler.postDelayed(mTask, 1000);
	} catch (Exception e) {Log.e("AlarmWindow"," resume "+e.toString());Log.e("AlarmWindow",Arrays.toString(e.getStackTrace()));}
	}
	
	@Override protected void onPause() {
		Log.d("AlarmWindow","OnPause "+wl);
		mHandler.removeCallbacks(mTask);
		wl.release();
      
	  super.onPause();		
	}
	
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
				((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
						WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT),
	            WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}
	
	public void onAlarmButClick(View view){		
		Intent myIntent = new Intent(this, ConfirmActivity.class);
		Log.d("AlarmWindow","Button clicked intent "+myIntent);	
		mTask.stopAlarm();	    
	    myIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);	    
	    startActivity(myIntent);
		finish();
	}
}
