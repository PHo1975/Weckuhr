package pit.holzer.weckuhr;

import java.text.DateFormat;
import java.util.Date;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class UhrActivity extends Activity {
	private final static int STEPS=30*60;
	private final Handler mHandler = new Handler() ;
	AlarmManager alarmManager;
	private Vibrator vib;
	private static final DateFormat df=DateFormat.getTimeInstance(DateFormat.SHORT);
	public final static String ALARM_TIME = "pit.holzer.Weckuhr.ALARMTIME";
    long alarmTime=0;
    int PermissionSettings =42;
    int PermissionAlert=43;
    PowerManager.WakeLock wl;
	private TextView currTimeField;
	private TextView alarmTimeField;
	private ToggleButton alarmButton;
	private MyAnalogClock analogClock;
	PendingIntent alarmIntent=null;

	
    
	static String timeToString(long time) {
		return df.format(new Date(time));
	}
	
	private final Runnable updateTimeDisplayTask = new Runnable() {
		public void run() {	
		  final long currTime=System.currentTimeMillis();
		  final String currText=timeToString(currTime);
		  currTimeField.setText(currText);
		  if (alarmTime<currTime) updateAlarm(false);
		  mHandler.postDelayed(this, 1000);
		}
	};

	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			setShowWhenLocked(true);
			setTurnScreenOn(true);
			KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE) ;
			keyguardManager.requestDismissKeyguard(this, null);
		} else {
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
					WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
					WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}
        checkOverlay();
		setContentView(R.layout.activity_uhr);
		currTimeField=  findViewById(R.id.UhrzeitLabel);
		alarmTimeField= findViewById(R.id.alarmTextView);
		alarmButton = findViewById(R.id.alarmToggleBut);
		alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);	
		analogClock=findViewById(R.id.analogClock1);
	}
	
	@Override protected void onResume() {
	    super.onResume();
	    SharedPreferences prefs=getPreferences( 0);
	    long currTime= System.currentTimeMillis();	    
	    alarmTime=prefs.getLong(ALARM_TIME,currTime);
	    if(alarmTime==0) alarmTime=currTime;
	    Log.d("AlarmWindow"," resume alarmTime:"+alarmTime+" "+timeToString(alarmTime)+" currtime:"+currTime+" "+timeToString(currTime));
	    currTimeField.setText(timeToString(currTime));
	    wl=((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "peter:lock");
	    wl.acquire(10000L);
	    if(alarmTime>currTime) {
	    	alarmButton.setChecked(true);
	    	alarmButton.setTextColor(Color.RED);
	    	updateAlarm(true);
	    }  else {
			alarmTime=currTime;
	    	//writeAlarmTime();
	    	alarmButton.setChecked(false);
	    	alarmButton.setTextColor(Color.BLACK);
	    	updateAlarm(false);
	    }
		mHandler.removeCallbacks(updateTimeDisplayTask);
		mHandler.postDelayed(updateTimeDisplayTask, 1000);
		vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
	}	
	
	
	@Override 
	protected void onPause() {
		Log.d("AlarmWindow","pause wl:"+wl);
		mHandler.removeCallbacks(updateTimeDisplayTask);
		if(wl!=null&&wl.isHeld())
		wl.release();
		super.onPause();		
	}
	
	 @Override
	public void onAttachedToWindow() {
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
			 WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
			 WindowManager.LayoutParams.TYPE_SYSTEM_ALERT),
	            WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	            }


	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.uhr, menu);
		return true;
	}
	
	private void updateAlarm(boolean enable) {
		alarmTimeField.setText(timeToString(alarmTime));	
		if( enable) analogClock.setAlarmTime(alarmTime); else analogClock.setAlarmTime(-1); 
	}
	
	private void vibrate() {
		vib.vibrate(50);
	}
	
	public void onLeftClick(View view) {
		final long currTime=System.currentTimeMillis();
		if(alarmTime<currTime+STEPS*1000) {alarmTime= currTime;updateAlarm(false);}
		else {alarmTime-=STEPS*1000;updateAlarm(true);}
		vibrate();	
	}
	
	public void onRightClick(View view) {
		final long currTime=System.currentTimeMillis();
		if(alarmTime<currTime+STEPS*500) alarmTime= currTime;
		alarmTime+=STEPS*1000;
		updateAlarm(true);	
		vibrate();
	}
	
	public void onLeftClick1m(View view) {
		final long currTime=System.currentTimeMillis();
		if(alarmTime<currTime+30000){ alarmTime= currTime;updateAlarm(false);}
		else { alarmTime-=60000; updateAlarm(true);}			
		vibrate();
	}
	
	public void onRightClick1m(View view) {
		final long currTime=System.currentTimeMillis();
		if(alarmTime<currTime+1000) alarmTime= currTime+1000;
		alarmTime+=60000;
		updateAlarm(true);
		vibrate();
	}
	
	
	public void onAlarmClick(View view) {
		vibrate();
		Log.d("AlarmWindow","on Alarm lick "+alarmButton.isChecked()+" "+alarmTime+" "+timeToString(alarmTime));
		if(alarmButton.isChecked()) {
	       setAlarm();
	       alarmButton.setTextColor(Color.RED);
	       if(wl!=null && wl.isHeld())  wl.release();
	       android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT ,2000);
	       Log.d("AlarmWindow","Finish Activity");
	       finish();
		}
		else {
			cancelAlarm();
			alarmButton.setTextColor(Color.BLACK);
		}
	}

	@SuppressLint("NewApi")
	private PendingIntent getAlarmIntent() {
		alarmIntent=PendingIntent.getBroadcast(getApplicationContext(), 0,
				new Intent(getApplicationContext(), AlarmReceiver.class),PendingIntent.FLAG_IMMUTABLE);
		return alarmIntent;
     }

     private void checkOverlay(){
		boolean permit;
		 if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		 	permit=Settings.canDrawOverlays(this.getApplicationContext());
		 } else {
			 permit = ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED;}
		 Log.d("AlarmWindow","permission overlay :"+permit);
		 if(!permit) {
			 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				 Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				 intent.setData(Uri.parse("package:" + getPackageName()));
				 Log.d("AlarmWindow","newer Version :"+intent.getDataString());
				 this.startActivityForResult(intent, PermissionAlert);
			 } else {
				 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, PermissionAlert);
			 }
		 }
	 }
	
	private void writeAlarmTime() {
		Log.d("AlarmWindow","try write alarm time :"+alarmTime+" "+timeToString(alarmTime));
		boolean permit;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			permit=Settings.System.canWrite(this);
		} else {
			permit = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;}
		Log.d("AlarmWindow","permission write settings:"+permit);
		if(!permit) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + getPackageName()));
				Log.d("AlarmWindow","newer Version :"+intent.getDataString());
				this.startActivityForResult(intent, PermissionSettings);
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, PermissionSettings);
			}
		} else intWriteAlarmTime();
	}

	private void intWriteAlarmTime() {
		SharedPreferences prefs=getPreferences(Context.MODE_PRIVATE);
		Editor edit=prefs.edit();
		edit.putLong(ALARM_TIME, alarmTime);
		Log.d("AlarmWindow","write alarm time :"+alarmTime);
		edit.apply();
	}
	
	public void setAlarm()    {
		if(alarmTime>0) {
			Log.d("AlarmWindow","setAlarm "+alarmTime+" "+timeToString(alarmTime)+" sdk:"+android.os.Build.VERSION.SDK_INT);
			writeAlarmTime();
			alarmTimeField.setText(timeToString(alarmTime));
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				if(alarmManager.canScheduleExactAlarms())
				  alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, getAlarmIntent());
				else {
					Log.d("AlarmWindow","Can not Shedule exact");
					alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, getAlarmIntent());
					Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
					intent.setData(Uri.parse("package:" + getPackageName()));
					this.startActivityForResult(intent, 9);
				}
			} else
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				Log.d("AlarmWindow","SetandallowIdle");
				alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, getAlarmIntent());
			} else alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, getAlarmIntent());
		}
    }
	
	public void cancelAlarm() {
		Log.d("AlarmWindow","cancelAlarm "+alarmTime);
		alarmTime=0L;
		writeAlarmTime();
		alarmTime=System.currentTimeMillis();
		updateAlarm(false);
		alarmManager.cancel((alarmIntent!=null)?alarmIntent:getAlarmIntent());

        android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT ,1000*60*2);
	}

	@SuppressLint("NewApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("AlarmWindow","on Activity Result :"+requestCode);
		if (requestCode == PermissionSettings && Settings.System.canWrite(this)){
			Log.d("TAG", "MainActivity.CODE_WRITE_SETTINGS_PERMISSION success");
			intWriteAlarmTime();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.d("AlarmWindow", "onRequestPermission "+requestCode);
		if (requestCode == PermissionSettings && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			intWriteAlarmTime();
		}
	}


}
