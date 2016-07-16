package com.example.chadwick.feelwearugo;

import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * Created by Chadwick on 3/18/2015.
 */
public class SpeedNotifier  implements  PaceNotifier.Listener, SpeakingTimer.Listener {

    public interface Listener {
        public void valueChanged(float value);

        public void passValue();
    }

    private Listener mListener;

    int mCounter = 0;
    float mSpeed = 0;

    boolean mIsMetric;
    float mStepLength;

    PedometerSettings mSettings;
    TextToSpeech mTts;

    float mDesiredSpeed;


    boolean mShouldTellFasterSlower;
    boolean mShouldTellSpeed;

    private long mSpokenAt = 0;

    ExponentialMovingAverage exponentialMovingAverage = new ExponentialMovingAverage(0.3);

    public SpeedNotifier(Listener listener, PedometerSettings settings, TextToSpeech tts) {
        mListener = listener;
        mTts = tts;
        mSettings = settings;
        mDesiredSpeed = settings.getDesiredSpeed();
        reloadSettings();
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
        notifyListener();
    }

    public void reloadSettings() {
        mIsMetric = mSettings.isMetric();
        mStepLength = mSettings.getStepLength();
        mShouldTellSpeed = mSettings.shouldTellSpeed();
        mShouldTellFasterSlower = mSettings.shouldTellFasterSlower();
        notifyListener();
    }

    public void setTts(TextToSpeech tts) {
        mTts = tts;
    }

    public void setDesiredSpeed(float desiredSpeed) {
        mDesiredSpeed = desiredSpeed;
    }
    public  void  notifyListener() {
        mListener.valueChanged(mSpeed);
    }

    public void paceChanged(int value){
        mSpeed = value * mStepLength /100000f * 60f; //centimetres/min -> KPH
        //mSpeed = value * mStepLength /63360f * 60f; // miles per hr
        mSpeed = exponentialMovingAverage.average(mSpeed);
        tellFasterSlower();
        notifyListener();
    }

    private void tellFasterSlower()
    {
        if(mShouldTellFasterSlower && (mTts != null)){
            long now = System.currentTimeMillis();
            if(now - mSpokenAt > 3000 && !mTts.isSpeaking()){

                float little = 0.10f;
                float normal = 0.3f;
                float much = 0.5f;

                boolean spoken = true;

                if(mSpeed > mSettings.getDesiredSpeed()*(1 + much)){
                    mTts.speak("You are walking too fast, you may hit an obstacle", 0, null);
                    Log.d("Speed:",""+mSpeed+"");
                }
                else if(mSpeed > mSettings.getDesiredPace()*(1+normal)){
                    mTts.speak(" Please walk a bit slower. Walking Slower may decrease your chance of hitting an obstacle", 0, null);
                }
                else{
                    spoken = false;
                }
                if (spoken){
                    mSpokenAt = now;
                }
            }
        }
    }

    @Override
    public void passValue(){
        // not used
    }

    @Override
    public void speak(){
        if(mSettings.shouldTellSpeed() && mTts != null){
            if (mSpeed >= .01f){
                mTts.speak((""+(mSpeed+0.0000001f)).substring(0,4)
                        + (mIsMetric ? "Kilometers Per Hour":"miles per hour"), 1,null );

            }
        }
    }

    class ExponentialMovingAverage{

        private double alpha;
        private Double oldValue;

        public ExponentialMovingAverage(double value){
            //Damping factor;
            this.alpha = value;
        }
        public float average (double value) {

            if (oldValue == null) {
                oldValue = value;
            }
            double newValue = oldValue + alpha * (value - oldValue);
            oldValue = newValue;
            return (float)newValue;
        }
    }
}
