/*
 *  Copyright (C) 2018 Havoc-OS
 *  Copyright (C) 2018 ExtendedUI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.exui.config.center;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Surface;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.Settings;

public class ConfigCenter extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";

    private ListPreference mHeadsUpTimeOut;
    private ListPreference mHeadsUpSnoozeTime;
    private Preference mChargingLeds;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.config_center);

        Resources systemUiResources;
        try {
            systemUiResources = getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null));
        mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
        int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_TIMEOUT, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);

        int defaultSnooze = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_default_snooze_length_ms", null, null));
        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnooze = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION_SNOOZE, defaultSnooze);
        mHeadsUpSnoozeTime.setValue(String.valueOf(headsUpSnooze));
        updateHeadsUpSnoozeTimeSummary(headsUpSnooze);

        PreferenceScreen prefScreen = getPreferenceScreen();
        mChargingLeds = (Preference) findPreference("charging_light");
        if (mChargingLeds != null
                && !getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(mChargingLeds);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_TIMEOUT,
                    headsUpTimeOut);
            updateHeadsUpTimeOutSummary(headsUpTimeOut);
            return true;
        } else if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnooze = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_NOTIFICATION_SNOOZE,
                    headsUpSnooze);
            updateHeadsUpSnoozeTimeSummary(headsUpSnooze);
            return true;
        }
        return false;
    }

    private void updateHeadsUpTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.heads_up_time_out_summary,
                value / 1000);
        mHeadsUpTimeOut.setSummary(summary);
    }

    private void updateHeadsUpSnoozeTimeSummary(int value) {
        if (value == 0) {
            mHeadsUpSnoozeTime.setSummary(getResources().getString(R.string.heads_up_snooze_disabled_summary));
        } else if (value == 60000) {
            mHeadsUpSnoozeTime.setSummary(getResources().getString(R.string.heads_up_snooze_summary_one_minute));
        } else {
            String summary = getResources().getString(R.string.heads_up_snooze_summary, value / 60 / 1000);
            mHeadsUpSnoozeTime.setSummary(summary);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    public static void lockCurrentOrientation(Activity activity) {
        int currentRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = activity.getResources().getConfiguration().orientation;
        int frozenRotation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        switch (currentRotation) {
            case Surface.ROTATION_0:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }
        activity.setRequestedOrientation(frozenRotation);
    }
}
