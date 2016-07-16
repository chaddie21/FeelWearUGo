package com.example.chadwick.feelwearugo;

/**
 * Created by Chadwick on 3/15/2015.
 */

/**
 * Interface implement be classes that handle notification about steps
 * theses classes can be then passed to stepDetector.
 */
public interface StepListener {
    public void onStep();
    public void passValue();

}
