/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2012 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
package org.jraf.android.worldtour.app.preference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.Constants;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferenceActivity extends SherlockPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
        updateSummary();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        super.onStop();
    }

    private final OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateSummary();
        }
    };

    private void updateSummary() {
        int value = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));
        int labelIndex = 2;
        switch (value) {
            case (int) Constants.INTERVAL_10_MINUTES:
                labelIndex = 0;
                break;
            case (int) Constants.INTERVAL_20_MINUTES:
                labelIndex = 1;
                break;
            case (int) Constants.INTERVAL_30_MINUTES:
                labelIndex = 2;
                break;
            case (int) Constants.INTERVAL_1_HOUR:
                labelIndex = 3;
                break;
        }
        String[] stringArray = getResources().getStringArray(R.array.preferences_updateInterval_labels_title);
        findPreference(Constants.PREF_UPDATE_INTERVAL).setSummary(stringArray[labelIndex]);
    }

}
