/**
 * Created by Chadwick on 3/15/2015.
 */


package com.example.chadwick.feelwearugo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class StepService extends Service implements TextToSpeech.OnInitListener {

    private PedometerSettings mPedometerSettings;
    private TextToSpeech mTts;
    private SensorManager mSensorManager;
    private StepDetector mStepDetector;
    private StepDisplayer mStepDisplayer;
    private PaceNotifier mPaceNotifier;
    private SpeedNotifier mSpeedNotifier;
    private DistanceNotifier mDistanceNotifier;
    private SpeakingTimer mSpeakingTimer;
    private StepBuzzer mStepBuzzer; // used for debugging

    private PowerManager.WakeLock wakeLock;
    private NotificationManager mNM;

    private int mSteps;
    private int mDegree;
    private int mPace;
    private float mDistance;
    private float mSpeed;



    public StepService() {

    }

    public class StepBinder extends Binder{
        StepService getService(){
            return StepService.this;
        }
    }

    @Override
    public void onCreate(){

        super.onCreate();
        mTts = new TextToSpeech(this, this);
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        //TODO Make showNotification()

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepService");
        wakeLock.acquire();

        //Load settings
        mPedometerSettings = new PedometerSettings("m",77f,5.0f,true,true);

        mStepDetector = new StepDetector();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mStepDetector,
                        SensorManager.SENSOR_ACCELEROMETER |
                        SensorManager.SENSOR_MAGNETIC_FIELD |
                        SensorManager.SENSOR_ORIENTATION,
                        SensorManager.SENSOR_DELAY_FASTEST);

        mStepDisplayer = new StepDisplayer(mPedometerSettings, mTts);
        mStepDisplayer.setSteps(0);
        mStepDisplayer.addListener(mStepListener);
        mStepDetector.addStepListener(mStepDisplayer);

        mPaceNotifier = new PaceNotifier(mPedometerSettings, mTts);
        mPaceNotifier.setPace(0);
        mPaceNotifier.addListener(mPaceListener);
        mStepDetector.addStepListener(mPaceNotifier);

        mDistanceNotifier = new DistanceNotifier(mDistanceListener, mPedometerSettings, mTts);
        mDistanceNotifier.setDistance(0.0000001f);
        mStepDetector.addStepListener(mDistanceNotifier);

        mSpeedNotifier = new SpeedNotifier(mSpeedListener, mPedometerSettings, mTts);
        mSpeedNotifier.setSpeed(0.00000001f);
        mPaceNotifier.addListener(mSpeedNotifier);

        mSpeakingTimer = new SpeakingTimer(mPedometerSettings);
        mSpeakingTimer.addListener(mStepDisplayer);
        mSpeakingTimer.addListener(mPaceNotifier);
        mSpeakingTimer.addListener(mDistanceNotifier);
        mSpeakingTimer.addListener(mSpeedNotifier);
        mStepDetector.addStepListener(mSpeakingTimer);

        mStepBuzzer = new StepBuzzer(getApplicationContext());
        reloadSettings();

    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);

        final Handler handler = new Handler();

        final Runnable updateGUI = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getText(R.string.started), Toast.LENGTH_SHORT).show();
            }


        };

        final Runnable updateToast = new Runnable() {
            @Override
            public void run() {
                handler.post(updateGUI);
            }
        };

        Thread thread = new Thread(null, updateToast, "Toast_background");
        thread.start();


    }

    @Override
    public void onDestroy(){
        mNM.cancel(R.string.app_name);
        wakeLock.release();
        super.onDestroy();
        mSensorManager.unregisterListener(mStepDetector);

        if(mTts != null){
           mTts.shutdown();
        }

        final Handler handler = new Handler();

        final Runnable updateGUI = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),getText(R.string.stopped),  Toast.LENGTH_SHORT).show();
            }


        };

        final Runnable updateToast = new Runnable() {
            @Override
            public void run() {
                handler.post(updateGUI);
            }
        };

        Thread thread = new Thread(null, updateToast, "Toast_background");
        thread.start();

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
        public void stepsChanged(int value);
        public void paceChanged(int value);
        public void distanceChanged(float value);
        public void speedChanged (float value);
       // public void headingChanged(int value);


    }

    private ICallback mCallBack;

    public void registerCallBack(ICallback cb){
        mCallBack = cb;
    }

    private int mDesiredPace;
    private float mDesiredSpeed;

    public void setDesiredPace(int desiredPace){
        mDesiredPace = desiredPace;
        if(mPaceNotifier!=null){
            mPaceNotifier.setDesiredPace(mDesiredPace);

        }
    }
    public void setDesiredSpeed(float desiredSpeed){
        mDesiredSpeed = desiredSpeed;
        if(mSpeedNotifier != null){
            mSpeedNotifier.setDesiredSpeed(mDesiredSpeed);
        }
    }

    public void reloadSettings() {
        if (mStepDetector != null) {
            mStepDetector.setSensitivity(30);
        }
        boolean userWantsVoice = mPedometerSettings.shouldSpeak();
        if (mTts == null && userWantsVoice) {
            //mTts = new TextToSpeech(this, this);

            if (mSpeakingTimer != null) mSpeakingTimer.setTts(mTts);

            if (mStepDisplayer != null) mStepDisplayer.setTts(mTts);

            if (mPaceNotifier != null) mPaceNotifier.setTts(mTts);

            if (mSpeedNotifier != null) mSpeedNotifier.setTts(mTts);

            if (mDistanceNotifier != null) mDistanceNotifier.setTts(mTts);

        }

        if (mStepDisplayer != null) mStepDisplayer.reloadSettings();
        if (mPaceNotifier != null) mPaceNotifier.reloadSettings();
        if (mDistanceNotifier != null) mDistanceNotifier.reloadSettings();
        if (mSpeedNotifier != null)
            try {
                    mDistanceNotifier.reloadSettings();

            } catch (NullPointerException e) {

            }
        if (mSpeakingTimer != null) mSpeakingTimer.reloadSettings();
    }




    public void resetValues(){
        mStepDisplayer.setSteps(0);
        mPaceNotifier.setPace(0);
        mDistanceNotifier.setDistance(0.00f);
        mSpeedNotifier.setSpeed(0.0f);
    }



    private StepDisplayer.Listener mStepListener = new StepDisplayer.Listener(){

        public void stepsChanged(int values){
            mSteps = values;
            passValue();
        }

        public void passValue(){
            if(mCallBack != null){
                mCallBack.stepsChanged(mSteps);
            }
        }
    };

    private PaceNotifier.Listener mPaceListener = new PaceNotifier.Listener(){

        @Override
        public void paceChanged(int value) {
            mPace = value;
            passValue();
        }

        @Override
        public void passValue() {
            if(mCallBack != null){
                mCallBack.paceChanged(mPace);
            }
        }
    };

    private SpeedNotifier.Listener mSpeedListener = new SpeedNotifier.Listener(){

        @Override
        public void valueChanged(float value) {
            mSpeed = value;
            passValue();
        }

        @Override
        public void passValue() {
            if (mCallBack != null){
                mCallBack.speedChanged(mSpeed);
            }
        }
    };

    private DistanceNotifier.Listener mDistanceListener = new DistanceNotifier.Listener(){

        @Override
        public void valueChanged(float value) {
            mDistance = value;
            passValue();
        }

        @Override
        public void passValue() {
            if(mCallBack != null)
                mCallBack.distanceChanged(mDistance);
        }
    };


    private void showNotification() {
        CharSequence text = getText(R.string.app_name);

        Notification notification = new Notification(R.drawable.ic_launcher, null,
                System.currentTimeMillis());

        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), 0);

        notification.setLatestEventInfo(getApplicationContext(),text,
                            getText(R.string.notification_subtitle), contentIntent);

        mNM.notify(R.string.app_name, notification);
    }





    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = mTts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "Not Supported");
            }


        }else{
            Log.e("TTS", "Initialization Failed");
        }
    }



}