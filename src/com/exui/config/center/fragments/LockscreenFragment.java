/*
 * Copyright (C) 2019 ExtendedUI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exui.config.center.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.*;
import android.hardware.fingerprint.FingerprintManager;

import com.android.internal.logging.nano.MetricsProto; 

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.exui.config.center.preferences.SystemSettingSwitchPreference;

public class LockscreenFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "LockscreenFragment";
    private static final String KEY_SCREEN_OFF_FOD = "screen_off_fod";
    private static final String KEY_SCREEN_OFF_FOD_ICON = "screen_off_fod_icon";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";

    private ContentResolver mResolver;
    private SwitchPreference mScreenOffFOD;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SystemSettingSwitchPreference mScreenOffFODIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config_center_lockscreen_category);

        PreferenceScreen prefScreen = getPreferenceScreen();

         mResolver = getActivity().getContentResolver();

        if (!getResources().getBoolean(com.android.internal.R.bool.config_supportsInDisplayFingerprint)) {
            prefScreen.removePreference(findPreference("fod_category"));
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        boolean mScreenOffFODValue = Settings.System.getInt(mResolver,
                Settings.System.SCREEN_OFF_FOD, 0) != 0;

        mScreenOffFOD = (SwitchPreference) findPreference(KEY_SCREEN_OFF_FOD);
        if (mScreenOffFOD != null) {
            mScreenOffFOD.setChecked(mScreenOffFODValue);
            mScreenOffFOD.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        }
        if (preference == mScreenOffFOD) {
            int mScreenOffFODValue = (Boolean) newValue ? 1 : 0;
            Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_FOD, mScreenOffFODValue);
            Settings.Secure.putInt(mResolver, Settings.Secure.DOZE_ALWAYS_ON, mScreenOffFODValue);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
