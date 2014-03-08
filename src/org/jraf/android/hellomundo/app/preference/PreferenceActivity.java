/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2009-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.hellomundo.app.preference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.common.BasePreferenceActivity;
import org.jraf.android.latoureiffel.R;

import com.actionbarsherlock.view.MenuItem;

public class PreferenceActivity extends BasePreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
        updateSummary();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        super.onStop();
    }


    /*
     * Action bar.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setResult(RESULT_OK);
            updateSummary();
        }
    };

    private void updateSummary() {
        int value = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));
        int labelIndex = 3;
        switch (value) {
            case (int) Constants.INTERVAL_1_MINUTE:
                labelIndex = 0;
                break;
            case (int) Constants.INTERVAL_10_MINUTES:
                labelIndex = 1;
                break;
            case (int) Constants.INTERVAL_20_MINUTES:
                labelIndex = 2;
                break;
            case (int) Constants.INTERVAL_30_MINUTES:
                labelIndex = 3;
                break;
            case (int) Constants.INTERVAL_1_HOUR:
                labelIndex = 4;
                break;
        }
        String[] stringArray = getResources().getStringArray(R.array.preferences_updateInterval_labels_title);
        findPreference(Constants.PREF_UPDATE_INTERVAL).setSummary(stringArray[labelIndex]);
    }

}
