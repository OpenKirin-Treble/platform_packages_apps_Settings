/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.settings.core.PreferenceController;

import com.android.settings.R;

import java.lang.String;
import java.lang.System;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseSecurityPatchPreferenceController extends PreferenceController {

    private static final String KEY_BASE_SECURITY_PATCH = "base_security_patch";
    private static final String TAG = "BaseSecurityPatchPref";

    private final String mPatch;
    private final PackageManager mPackageManager;

    public BaseSecurityPatchPreferenceController(Context context) {
        super(context);
        mPackageManager = mContext.getPackageManager();
        mPatch = getSecurityPatch();
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(mPatch);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BASE_SECURITY_PATCH;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(KEY_BASE_SECURITY_PATCH);
        if (pref != null) {
	    Resources res = mContext.getResources();
	    String orig_title = res.getString(R.string.security_patch);
	    String new_title = orig_title.replace("Android", "Base");

	    pref.setTitle(new_title);
            pref.setSummary(mPatch);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_BASE_SECURITY_PATCH)) {
            return false;
        }
        if (mPackageManager.queryIntentActivities(preference.getIntent(), 0).isEmpty()) {
            // Don't send out the intent to stop crash
            Log.w(TAG, "Stop click action on " + KEY_BASE_SECURITY_PATCH + ": "
                    + "queryIntentActivities() returns empty");
            return true;
        }
        return false;
    }

    public static String getSecurityPatch() {
        String patch = SystemProperties.get("ro.build.version.security_patch", "");
        if (!"".equals(patch)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(patch);
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                patch = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {
                // broken parse; fall through and use the raw string
            }
            return patch;
        } else {
            return null;
        }
    }
}
