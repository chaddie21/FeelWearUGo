package com.example.chadwick.feelwearugo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Photonovation on 08/08/2016.
 */
public class FeelWearUGoPreferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
