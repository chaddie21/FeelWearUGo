package com.example.chadwick.feelwearugo;

import android.speech.tts.TextToSpeech;

import java.util.ArrayList;

/**
 * Created by Chadwick on 3/15/2015.
 */
public class StepDisplayer implements StepListener, SpeakingTimer.Listener{

    private int mCount = 0;
    private int mDegree =0;

    PedometerSettings mSettings;

    public StepDisplayer(PedometerSettings settings, TextToSpeech tts){
        mTts = tts;
        mSettings = settings;
        notifyListener();
    }

    public void setSteps (int steps){
        mCount = steps;
        notifyListener();
    }

    public void onStep() {
        mCount++;
        notifyListener();
    }

    public void reloadSettings(){
        notifyListener();
    }
    @Override
    public void passValue() {

    }


//---------------------------------LISTENER----------------------------------
    public interface Listener{
    public void stepsChanged(int value);
    public void passValue();
    }

    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener (Listener l){
        mListeners.add(l);
    }

    public void notifyListener() {
        for (Listener listener : mListeners){
            listener.stepsChanged(mCount);

        }
    }
 //-------------------------------SPEAKER------------------------------------------
    TextToSpeech mTts;

    public void setTts(TextToSpeech tts){
        mTts = tts;
    }
    public void speak(){
        if(mSettings.shouldTellSteps() && mTts != null){
            if (mCount > 0) {
                mTts.speak("" + mCount + "Steps", 1, null);
            }
        }
    }

}
