package com.example.chadwick.feelwearugo;

/**
 * Created by Chadwick on 3/15/2015.
 */

public class PedometerSettings {



    public static int M_NONE = 1;
    public static int M_PACE = 2;
    public static int M_SPEED = 3;

    String unit;
    float stepLength;
    int desiredPace;
    float desiredSpeed;
    float speakInterval;
    boolean shouldSpeak;
    boolean shouldTellStep;
    boolean shouldTellPace;
    boolean shouldTellDistance;
    boolean shouldTellSpeed;
    boolean shouldTellFasterSlower;
    boolean shouldBuzz;


    public  PedometerSettings(String mUnit, float mStepLength, float mDesiredSpeed,boolean mShouldSpeak,boolean shouldTellFasterSlower){

        this.unit = mUnit;
        this.stepLength = mStepLength;
        this.desiredSpeed = mDesiredSpeed;
        this.desiredPace = 120;
        this.shouldSpeak = mShouldSpeak;
        this.speakInterval = 1.0f;
        this.shouldTellStep = false;
        this.shouldTellPace = false;
        this.shouldTellDistance = false;
        this.shouldTellSpeed = true;
        this.shouldTellFasterSlower = shouldTellFasterSlower;
        this.shouldBuzz = true;

    }

    public boolean isMetric() {
        boolean state;
        if(unit.equals("m")) state =true;
        else state = false;
        return state;
    }

    public void setStepLength(float sl){
        stepLength = sl;
    }


    public float getStepLength() {
        return stepLength;
    }



    public int getDesiredPace() {
        return desiredPace;
    }

    public void setDesiredSpeed(float s){
        desiredSpeed = s;
    }

    public float getDesiredSpeed() {
        return desiredSpeed;
    }


    //-------------------------------------------------------------------
    // Speaking:

    public boolean shouldSpeak() {
        return shouldSpeak;
    }

    public void setShouldSpeak(boolean mShouldSpeak){
        shouldSpeak = mShouldSpeak;
    }

    public float getSpeakingInterval() {
       return speakInterval;
    }
    public boolean shouldTellSteps() {
        return shouldTellStep;
    }

    public boolean shouldTellPace() {
        return shouldTellPace;
    }
    public void setShouldTellPace(boolean ts)
    {
        shouldTellPace = ts;
    }

    public boolean shouldTellDistance() {
        return shouldTellDistance;
    }
    public void setShouldTellDistance(boolean td){
        shouldTellDistance = td;
    }

    public boolean shouldTellSpeed() {
        return shouldTellSpeed;
    }

    public boolean shouldTellFasterSlower() {
        return shouldTellFasterSlower;
    }

}
