package pit.holzer.weckuhr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
	        Log.d("AlarmReceived",""+context.getClass().getName()+" action:"+intent.getAction());
	        Intent myIntent = new Intent(context.getApplicationContext(), AlarmWindow.class);
	        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(myIntent);
	    } catch (Exception e) {Log.e("AlarmReceived",e.toString());}
	}

}
