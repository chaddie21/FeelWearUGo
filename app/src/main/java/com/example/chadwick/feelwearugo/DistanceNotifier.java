package com.example.chadwick.feelwearugo;

import android.speech.tts.TextToSpeech;

/**
 * Created by Chadwick on 3/15/2015.
 */
public class DistanceNotifier implements StepListener, SpeakingTimer.Listener {



    public interface Listener{
        public void valueChanged(float value);
        public void passValue();
    }

    private Listener mListener;

    private float mDistance = 0;

    private PedometerSettings mSettings;
    private TextToSpeech mTts;

    private boolean mIsMetric;
    private float mStepLength;

    public DistanceNotifier(Listener listener, PedometerSettings settings, TextToSpeech tts){
        mListener = listener;
        mTts = tts;
        mSettings = settings;
        reloadSettings();
    }
    public void setDistance(float distance){
        mDistance = distance;
        notifyListener();
    }
    public void setTts (TextToSpeech tts){
        mTts = tts;

    }
    public void reloadSettings(){
        mIsMetric = mSettings.isMetric();
        mStepLength = mSettings.getStepLength();
        notifyListener();
    }
    @Override
    public void onStep() {
        if (mIsMetric){

            //convert: centimeters -> meters

            mDistance += (float)(mStepLength/ 100.0); //centimeter -> meters
        }else{

            //convert: inches -> miles

            mDistance += (float)(mStepLength/ 63360.0);
        }

        notifyListener();
    }

    private void notifyListener(){
        mListener.valueChanged(mDistance);
    }
    @Override
    public void passValue() {

    }

//----------------------- SPEAK ---------------------------------------------------

    public void speak(){
        if (mSettings.shouldTellDistance() && mTts != null){
            if(mDistance> 0.001f){
                mTts.speak((""+(mDistance+0.000000001f)).substring(0,4)+ (mIsMetric?"meters": "miles"),1,null);
                //TODO format numbers(no"." at the end)
            }
        }
    }


}
