package pit.holzer.weckuhr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

import androidx.core.content.ContextCompat;

import java.util.TimeZone;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
@RemoteView
public class MyAnalogClock extends View {
    private Time mCalendar;

    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable alarmHand;
    private Drawable mDial;
    
    private long alarmTime;
    
    private float alarmAngle= -1;

    private final int mDialWidth;
    private final int mDialHeight;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private float mMinutes;
    private float mHour;
    private boolean mChanged;

    public MyAnalogClock(Context context) {
        this(context, null);
    }    
    

    public MyAnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyAnalogClock(Context context, AttributeSet attrs,
                         int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
        mDial= ContextCompat.getDrawable(context,R.drawable.dial3);
        mHourHand=ContextCompat.getDrawable(context,R.drawable.stundens1);
        mMinuteHand=ContextCompat.getDrawable(context,R.drawable.minutens2);
        alarmHand=ContextCompat.getDrawable(context,R.drawable.alarm1);
        mCalendar = new Time();
        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
        } else {
        	mDialWidth=300;
        	mDialHeight=300;
        }
    }
    
    //public long getAlarmTime(){return alarmTime;}
    
    public void setAlarmTime(long newTime) {
    	alarmTime=newTime;
    	if(alarmTime<0) alarmAngle= -1;
    	else {
    		mCalendar.set(newTime);
    		int hour = mCalendar.hour;
            int minute = mCalendar.minute;            

            float mm = minute + mCalendar.second / 60.0f;
            alarmAngle = hour + mm / 60.0f;
    	}
    	mChanged=true;
    	invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
        }       
        mCalendar = new Time();        
        onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
                resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
    }

    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }
        if(!isInEditMode()) {
	        int availableWidth = getWidth();
	        int availableHeight = getHeight();
	        int x = availableWidth / 2;
	        int y = availableHeight / 2;
	        final Drawable dial = mDial;
	        int w = dial.getIntrinsicWidth();
	        int h = dial.getIntrinsicHeight();
	        boolean scaled = false;
	
	        if (availableWidth < w || availableHeight < h) {
	            scaled = true;
	            float scale = Math.min((float) availableWidth / (float) w,
	                                   (float) availableHeight / (float) h);
	            canvas.save();
	            canvas.scale(scale, scale, x, y);
	        }
	
	        if (changed) {
	            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
	        }
	        dial.draw(canvas);        
	
	        canvas.save();
	        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
	
	        final Drawable minuteHand = mMinuteHand;
	        if (changed) {
	            w = minuteHand.getIntrinsicWidth();
	            h = minuteHand.getIntrinsicHeight();
	            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
	        }
	        minuteHand.draw(canvas);
	        canvas.restore();	       
	        
	        canvas.save();
	        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
	        final Drawable hourHand = mHourHand;
	        if (changed) {
	            w = hourHand.getIntrinsicWidth();
	            h = hourHand.getIntrinsicHeight();
	            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
	        }
	        hourHand.draw(canvas);
	        canvas.restore();
	        
	        if(alarmTime>0) {
		        canvas.save();
		        canvas.rotate(alarmAngle / 12.0f * 360.0f, x, y);
		        final Drawable alHand = alarmHand;
		        if (changed) {
		            w = alHand.getIntrinsicWidth();
		            h = alHand.getIntrinsicHeight();
		            alHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
		        }
		        alHand.draw(canvas);
		        canvas.restore();
	        }	        
	        if (scaled) {
	            canvas.restore();
	        }	        
        }
    }

    private void onTimeChanged() {
        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;

        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }

            onTimeChanged();
            
            invalidate();
        }
    };
}
