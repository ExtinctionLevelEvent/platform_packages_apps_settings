/*
 * Copyright (C) 2012 CyanogenMod
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "SystemSettings";

    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_NOTIFICATION_DRAWER = "notification_drawer";
    private static final String KEY_NOTIFICATION_DRAWER_TABLET = "notification_drawer_tablet";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";
    private static final String KEY_LOCK_CLOCK = "lock_clock";

    private PreferenceScreen mPhoneDrawer;
    private PreferenceScreen mTabletDrawer;

    private final Configuration mCurConfig = new Configuration();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_settings);

        mPhoneDrawer = (PreferenceScreen) findPreference(KEY_NOTIFICATION_DRAWER);
        mTabletDrawer = (PreferenceScreen) findPreference(KEY_NOTIFICATION_DRAWER_TABLET);

        /*if (Utils.isTablet(getActivity())) {
            if (mPhoneDrawer != null) {
                getPreferenceScreen().removePreference(mPhoneDrawer);
            }
        } else*/ {
            if (mTabletDrawer != null) {
                getPreferenceScreen().removePreference(mTabletDrawer);
            }
        }

        // Dont display the lock clock preference if its not installed
        removePreferenceIfPackageNotInstalled(findPreference(KEY_LOCK_CLOCK));

        IWindowManager windowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            if (!windowManager.hasNavigationBar()) {
                Preference naviBar = findPreference(KEY_NAVIGATION_BAR);
                if (naviBar != null) {
                    getPreferenceScreen().removePreference(naviBar);
                }
            } else {
                Preference hardKeys = findPreference(KEY_HARDWARE_KEYS);
                if (hardKeys != null) {
                    getPreferenceScreen().removePreference(hardKeys);
                }
            }
        } catch (RemoteException e) {
        }
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateState() {
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();

        return true;
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri=((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName=matcher.find()?matcher.group(1):null;
        if(packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG,"package "+packageName+" not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }
}
