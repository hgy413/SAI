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

    public boolean shouldExtractArchives() {
        return false;
    }

    public boolean shouldUseZipFileApi() {
        return false;
    }

    public void setInstaller(int installer) {
        mPrefs.edit().putInt(PreferencesKeys.INSTALLER, installer).apply();
    }

    public int getInstaller() {
        return mPrefs.getInt(PreferencesKeys.INSTALLER, PreferencesValues.INSTALLER_ROOTLESS);
    }

    public int getInstallLocation() {
        String rawInstallLocation = mPrefs.getString(PreferencesKeys.INSTALL_LOCATION, "0");
        try {
            return Integer.parseInt(rawInstallLocation);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean showInstallerDialogs() {
        return mPrefs.getBoolean(PreferencesKeys.SHOW_INSTALLER_DIALOGS, true);
    }

    public boolean shouldShowAppFeatures() {
        return mPrefs.getBoolean(PreferencesKeys.SHOW_APP_FEATURES, true);
    }

    public boolean isInstallerXEnabled() {
        return mPrefs.getBoolean(PreferencesKeys.USE_INSTALLERX, true);
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
