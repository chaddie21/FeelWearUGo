package com.example.chadwick.feelwearugo;

import android.speech.tts.TextToSpeech;

import java.util.ArrayList;


/**
 * Created by Chadwick on 3/15/2015.
 */
public class SpeakingTimer implements StepListener {

    PedometerSettings mSettings;
    boolean mShouldSpeak;
    float mInterval;
    long mLastSpeakTime;

    public SpeakingTimer(PedometerSettings settings){
        mLastSpeakTime = System.currentTimeMillis();
        mSettings = settings;
        reloadSettings();
    }

    public void reloadSettings() {
        mShouldSpeak = mSettings.shouldSpeak();
        mInterval = mSettings.getSpeakingInterval();
    }

    @Override
    public void onStep() {
        long now = System.currentTimeMillis();
        long delta = now - mLastSpeakTime;

        if (delta/ 60000.0 >= mInterval){
            mLastSpeakTime = now;
            notifyListeners();
        }
    }

    @Override
    public void passValue() {
        // will be used another time :D
    }


//-------------------------------LISTENER------------------------------------------//

    public interface Listener{
     public void speak();
    }

    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener (Listener l){
        mListeners.add(l);
    }
    public void notifyListeners(){
        for (Listener listener : mListeners){
            listener.speak();
        }
    }


//------------------------------Speaking-------------------------------------------------

    TextToSpeech mTts;

    public void setTts(TextToSpeech tts){
        mTts = tts;
    }
    public boolean isSpeaking(){
        return mTts.isSpeaking();
    }


}
