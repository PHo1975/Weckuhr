package pit.holzer.weckuhr;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

public class AlarmRepeater implements Runnable {
		
	public AudioManager audioManager;
	public Ringtone ringtone;
	public Handler mHandler;
	public int maxVolume=0;
	public int repeat=0;
	public boolean maxSet=false;
	public boolean shouldStop=false;
	public Uri ringtoneNotification;
	private Vibrator vib;
	
	public AlarmRepeater(Handler nHandler) {
		mHandler=nHandler;
	}
	
	public void setup(Context context) {
		audioManager= (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        Log.d("AlarmWindow","MaxVolume="+maxVolume);
        ringtoneNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(context, ringtoneNotification);
        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	public void reset() {
		repeat=0;
	    shouldStop=false;
	    maxSet=false;
	}
	
	public void run() {		  
		  if(repeat<7)repeat++;
		  if(repeat>6&& !maxSet) maxSet=true;
		  if(!maxSet) 		  
		    audioManager.setStreamVolume(AudioManager.STREAM_RING,(int)(((double) maxVolume*repeat)/7d), 0);		         
	      ringtone.play();	      
	      Log.d("AlarmWindow","run "+shouldStop+" "+repeat);
	      if(repeat>3) vib.vibrate(200);
	      //ringtone.stop();	      
		  if(!shouldStop)mHandler.postDelayed(this, 5000+(int)(4000*Math.random()));
	}
	
	public void stopAlarm() {
		shouldStop=true;
		mHandler.removeCallbacks(this);
		ringtone.stop();
	}
};


