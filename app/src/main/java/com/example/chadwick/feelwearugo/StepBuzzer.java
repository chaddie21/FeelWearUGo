package com.example.chadwick.feelwearugo;

/**
 * Created by Chadwick on 3/21/2015.
 */

import android.content.Context;
import android.os.Vibrator;

// Vibrate whenever a step is detected

public class StepBuzzer implements StepListener {

    private Context mContext;
    private Vibrator mVibrator;

    public StepBuzzer (Context context){
        mContext = context;
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);

    }
    public void onStep(){
        buzz();
    }
    public void passValue(){

    }
    private void buzz(){
        mVibrator.vibrate(50);
    }
}
