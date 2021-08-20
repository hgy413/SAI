package com.mcool.sai.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.preference.PreferenceManager;

public class PreferencesHelper {
    private static PreferencesHelper sInstance;

    private SharedPreferences mPrefs;

    public static PreferencesHelper getInstance(Context c) {
        return sInstance != null ? sInstance : new PreferencesHelper(c);
    }

    private PreferencesHelper(Context c) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        sInstance = this;
    }

    public SharedPreferences getPrefs() {
        return mPrefs;
    }

    public boolean shouldSignApks() {
        return false;
    }

    public boolean shouldUseZipFileApi() {
        return true; // 必须为true才支持xapk的安装
    }

    public boolean showInstallerDialogs() {
        return true;
    }

    public boolean shouldShowAppFeatures() {
        return true;
    }

    public boolean isInstallerXEnabled() {
        return true;
    }

    public boolean isBruteParserEnabled() {
        return mPrefs.getBoolean(PreferencesKeys.USE_BRUTE_PARSER, true);
    }

    public boolean isInitialIndexingDone() {
        return mPrefs.getBoolean(PreferencesKeys.INITIAL_INDEXING_RUN, false);
    }

    public void setInitialIndexingDone(boolean done) {
        mPrefs.edit().putBoolean(PreferencesKeys.INITIAL_INDEXING_RUN, done).apply();
    }

    public boolean isSingleApkExportEnabled() {
        return mPrefs.getBoolean(PreferencesKeys.BACKUP_APK_EXPORT, false);
    }

    public void setSingleApkExportEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(PreferencesKeys.BACKUP_APK_EXPORT, enabled).apply();
    }
}
