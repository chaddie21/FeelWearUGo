

/**
 * Created by Chadwick on 3/15/2015.
 */

package com.example.chadwick.feelwearugo;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

import java.util.ArrayList;



@SuppressWarnings("deprecation")
public class StepDetector implements SensorListener {

    private int mLimit = 30;
    private float mLastValues[] = new float[3*2];
    private float mScale[] = new float[2];
    private float mYOffset;

    private float mLastDirections[] = new float[3*2];
    private float mLastExtremes [] [] = {new float[3*2], new float[3*2]};
    private float mLastDiff[] = new float[3*2];
    private int   mLastMatch = -1;

    private float[] matrixR = new float[9];
    private float[] matrixI = new float[9];
    private float[] matrixValues = new float[3];
    private float[] valuesAccelerometer = new float[3];
    private float[] valuesMagneticField = new float[3];
    public int degree;



    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    public StepDetector(){
        int h = 480;    //TODO take this out
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f *(1.0f /(SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f/SensorManager.MAGNETIC_FIELD_EARTH_MAX));
        degree = 0;
    }

    public void setSensitivity(int sensitivity){
        mLimit = sensitivity;
    }

    public int getHeading(){
        return degree;
    }

    public void addStepListener(StepListener sl){
        mStepListeners.add(sl);
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
        int j;
        synchronized (this){
            if(sensor== SensorManager.SENSOR_ORIENTATION ){
                degree =  Math.round(values[0]);
            }
            else{

                if(sensor == SensorManager.SENSOR_MAGNETIC_FIELD) j=1;
                else j = 0;


                   if(j==0){
                    // ----------------Must be TYPE_ACCELEROMETER---------------
                    float vSum =0;
                    for(int i= 0; i<3; i++){
                        valuesAccelerometer[i] = values[i];
                        final float v = mYOffset + values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum/3;

                    float direction = (v > mLastValues[k]? 1: (v < mLastValues[k] ? -1:0));

                    if (direction == - mLastDirections[k]){

                        // the Direction has changed
                        int exType = (direction > 0 ? 0:1); // min or max?
                        mLastExtremes[exType][k] = mLastValues[k];

                        float diff = Math.abs(mLastExtremes[exType][k] - mLastExtremes[1-exType][k]);

                        if (diff > mLimit){

                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2/3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                            boolean isNotContra = (mLastMatch!= (1- exType));

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra){
                                for (StepListener stepListener : mStepListeners){
                                    stepListener.onStep();
                                }
                                mLastMatch = exType;
                            }
                            else {
                              mLastMatch = -1;
                            }
                        }
                        mLastDiff [k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;

                }
            }

        }

    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {

    }

}


