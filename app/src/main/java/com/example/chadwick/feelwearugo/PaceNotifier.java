package com.example.chadwick.feelwearugo;

import android.speech.tts.TextToSpeech;

import java.util.ArrayList;

/**
 * Created by Chadwick on 3/15/2015.
 */
public class PaceNotifier implements StepListener, SpeakingTimer.Listener {

    public interface Listener {
        public void paceChanged(int value);
        public void passValue();
    }
    private ArrayList<Listener> mListeners = new ArrayList<>();

    int mCounter = 0;

    private long mLastStepTime = 0;
    private long[] mLastStepDeltas = {-1, -1, -1, -1};
    private int mLastStepDeltasIndex = 0;
    private long mPace = 0;

    PedometerSettings mSettings;
    TextToSpeech mTts;

    /** Desired pace may vary according to walkway traffic conditions */
    int mDesiredPace;

    /** Should we speak? */
    boolean mShouldTellFasterSlower;

    /** When did the TTS speak last time */
    private long mSpokenAt = 0;

    public PaceNotifier(PedometerSettings settings, TextToSpeech tts) {
        mTts = tts;
        mSettings = settings;
        mDesiredPace = mSettings.getDesiredPace();
        reloadSettings();
    }

    public void setPace(int pace) {
        mPace = pace;
        int avg = (int)(60*1000.0 / mPace);
        for (int i = 0; i < mLastStepDeltas.length; i++) {
            mLastStepDeltas[i] = avg;
        }
        notifyListener();
    }
    public void reloadSettings() {
        mShouldTellFasterSlower =
                mSettings.shouldTellFasterSlower();
       notifyListener();
    }
    public void addListener(Listener l) {
        mListeners.add(l);
    }

    public void setDesiredPace(int desiredPace) {
        mDesiredPace = desiredPace;
    }

    public void setTts(TextToSpeech tts) {
        mTts = tts;
    }

    @Override
    public void onStep() {
        mCounter++;

        if(mLastStepTime>0){
            long now = System.currentTimeMillis();
            long delta = now - mLastStepTime;

            mLastStepDeltas[mLastStepDeltasIndex] = delta;
            mLastStepDeltasIndex = (mLastStepDeltasIndex + 1)% mLastStepDeltas.length;

            long sum = 0;
            boolean isMeaningFull = true;
            for (int i= 0; i < mLastStepDeltas.length; i++){
                if(mLastStepDeltas[i] < 0){
                    isMeaningFull = false;
                    break;

                }
                sum += mLastStepDeltas[i];
            }
            if(isMeaningFull){
                long avg = sum/ mLastStepDeltas.length;
                mPace = (60*1000) / avg;

                if (mShouldTellFasterSlower && mTts != null){
                    if  ( (now - mSpokenAt) > 3000 && !mTts.isSpeaking()){
                        //float little = 0.10f;
                        float normal = 0.3f;
                        float much = 0.5f;

                        boolean spoken = true;

                        if(mPace >120){
                            mTts.speak("Your pace is too fast, you may hit an obstacle", 0, null);
                        }
                        else if(mPace > mSettings.getDesiredPace()*(1+normal)){
                            mTts.speak(" Please walk a bit slower. Walking Slower may decrease your chances of hitting an obstacle", 0, null);
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
            else{
                mPace =-1;
            }

        }
        mLastStepTime = System.currentTimeMillis();
        notifyListener();

    }

    private void notifyListener() {
        for (Listener listener : mListeners) {
            listener.paceChanged((int)mPace);
        }
    }

    @Override
    public void passValue() {

    }


    //-----------------------------------------------------
    // Speaking
    @Override
    public void speak() {
        if (mSettings.shouldTellPace() && mTts != null) {
            if (mPace > 0) {
                mTts.speak(mPace + " steps per minute", 1, null);
            }
        }
    }

}
