package com.mcool.sai.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.aefyr.sai.BuildConfig;
import com.aefyr.sai.R;
import com.mcool.sai.analytics.AnalyticsProvider;
import com.mcool.sai.analytics.DefaultAnalyticsProvider;
import com.mcool.sai.shell.SuShell;
import com.mcool.sai.ui.activities.AboutActivity;
import com.mcool.sai.ui.activities.ApkActionViewProxyActivity;
import com.mcool.sai.ui.activities.BackupSettingsActivity;
import com.mcool.sai.ui.activities.DonateActivity;
import com.mcool.sai.ui.dialogs.DarkLightThemeSelectionDialogFragment;
import com.mcool.sai.ui.dialogs.SimpleAlertDialogFragment;
import com.mcool.sai.ui.dialogs.SingleChoiceListDialogFragment;
import com.mcool.sai.ui.dialogs.ThemeSelectionDialogFragment;
import com.mcool.sai.ui.dialogs.base.BaseBottomSheetDialogFragment;
import com.mcool.sai.utils.AlertsUtils;
import com.mcool.sai.utils.PermissionsUtils;
import com.mcool.sai.utils.PreferencesHelper;
import com.mcool.sai.utils.PreferencesKeys;
import com.mcool.sai.utils.PreferencesValues;
import com.mcool.sai.utils.Theme;
import com.mcool.sai.utils.Utils;

import java.util.Objects;

import rikka.shizuku.Shizuku;

public class PreferencesFragment extends PreferenceFragmentCompat implements SingleChoiceListDialogFragment.OnItemSelectedListener, BaseBottomSheetDialogFragment.OnDismissListener, SharedPreferences.OnSharedPreferenceChangeListener, DarkLightThemeSelectionDialogFragment.OnDarkLightThemesChosenListener, Shizuku.OnRequestPermissionResultListener {

    private PreferencesHelper mHelper;
    private AnalyticsProvider mAnalyticsProvider;
    private PackageManager mPm;

    private Preference mHomeDirPref;
    private Preference mFilePickerSortPref;
    private Preference mInstallerPref;
    private Preference mThemePref;
    private SwitchPreference mAutoThemeSwitch;
    private Preference mAutoThemePicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mHelper = PreferencesHelper.getInstance(requireContext());
        mAnalyticsProvider = DefaultAnalyticsProvider.getInstance(requireContext());
        mPm = requireContext().getPackageManager();

        //Inject some prefs
        //Inject current auto theme status since it isn't managed by PreferencesKeys.AUTO_THEME key
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(PreferencesKeys.AUTO_THEME, Theme.getInstance(requireContext()).getThemeMode() == Theme.Mode.AUTO_LIGHT_DARK).apply();

        //Inject apk proxy activity state since there's no guarantee preference value matches actual state
        int apkProxyActivityState = mPm.getComponentEnabledSetting(ApkActionViewProxyActivity.getComponentName(requireContext()));
        boolean isApkProxyActivityEnabled;
        switch (apkProxyActivityState) {
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                isApkProxyActivityEnabled = true;
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
                isApkProxyActivityEnabled = false;
                break;
            default:
                throw new IllegalStateException(String.format("ApkProxyActivity state is %d", apkProxyActivityState));
        }
        prefsEditor.putBoolean(PreferencesKeys.ENABLE_APK_ACTION_VIEW, isApkProxyActivityEnabled);

        prefsEditor.apply();


        if (Utils.apiIsAtLeast(Build.VERSION_CODES.M)) {
            Shizuku.addRequestPermissionResultListener(this);
        }

        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);

        mHomeDirPref = findPreference("home_directory");
        updateHomeDirPrefSummary();
        mHomeDirPref.setOnPreferenceClickListener((p) -> {
            return true;
        });

        mFilePickerSortPref = findPreference("file_picker_sort");
        updateFilePickerSortSummary();
        mFilePickerSortPref.setOnPreferenceClickListener((p) -> {
            SingleChoiceListDialogFragment.newInstance(getText(R.string.settings_main_file_picker_sort), R.array.file_picker_sort_variants, mHelper.getFilePickerRawSort()).show(getChildFragmentManager(), "sort");
            return true;
        });

        findPreference("about").setOnPreferenceClickListener((p) -> {
            startActivity(new Intent(getContext(), AboutActivity.class));
            return true;
        });

        Preference donatePref = Objects.requireNonNull(findPreference("donate"));
        donatePref.setOnPreferenceClickListener(p -> {
            startActivity(new Intent(requireContext(), DonateActivity.class));
            return true;
        });
        donatePref.setVisible(!BuildConfig.HIDE_DONATE_BUTTON);

        mInstallerPref = findPreference("installer");
        updateInstallerSummary();
        mInstallerPref.setOnPreferenceClickListener((p -> {
            SingleChoiceListDialogFragment.newInstance(getText(R.string.settings_main_installer), R.array.installers, mHelper.getInstaller()).show(getChildFragmentManager(), "installer");
            return true;
        }));

        findPreference(PreferencesKeys.BACKUP_SETTINGS).setOnPreferenceClickListener(p -> {
            startActivity(new Intent(requireContext(), BackupSettingsActivity.class));
            return true;
        });

        mThemePref = findPreference(PreferencesKeys.THEME);
        updateThemeSummary();
        mThemePref.setOnPreferenceClickListener(p -> {
            ThemeSelectionDialogFragment.newInstance(requireContext()).show(getChildFragmentManager(), "theme");
            return true;
        });
        if (Theme.getInstance(requireContext()).getThemeMode() != Theme.Mode.CONCRETE) {
            mThemePref.setVisible(false);
        }

        mAutoThemeSwitch = Objects.requireNonNull(findPreference(PreferencesKeys.AUTO_THEME));
        mAutoThemePicker = findPreference(PreferencesKeys.AUTO_THEME_PICKER);
        updateAutoThemePickerSummary();

        mAutoThemeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean value = (boolean) newValue;
            if (value) {
                if (!Utils.apiIsAtLeast(Build.VERSION_CODES.Q))
                    SimpleAlertDialogFragment.newInstance(requireContext(), R.string.settings_main_auto_theme, R.string.settings_main_auto_theme_pre_q_warning).show(getChildFragmentManager(), null);

                Theme.getInstance(requireContext()).setMode(Theme.Mode.AUTO_LIGHT_DARK);
            } else {
                Theme.getInstance(requireContext()).setMode(Theme.Mode.CONCRETE);
            }

            //Hack to not mess with hiding/showing preferences manually
            requireActivity().recreate();
            return true;
        });

        mAutoThemePicker.setOnPreferenceClickListener(pref -> {
            DarkLightThemeSelectionDialogFragment.newInstance().show(getChildFragmentManager(), null);
            return true;
        });

        if (Theme.getInstance(requireContext()).getThemeMode() != Theme.Mode.AUTO_LIGHT_DARK) {
            mAutoThemePicker.setVisible(false);
        }

        SwitchPreference analyticsPref = findPreference(PreferencesKeys.ENABLE_ANALYTICS);
        analyticsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            mAnalyticsProvider.setDataCollectionEnabled((boolean) newValue);
            return true;
        });
        if (!mAnalyticsProvider.supportsDataCollection())
            analyticsPref.setVisible(false);

        SwitchPreference enableApkActionViewPref = findPreference(PreferencesKeys.ENABLE_APK_ACTION_VIEW);
        enableApkActionViewPref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (boolean) newValue;
            mPm.setComponentEnabledSetting(ApkActionViewProxyActivity.getComponentName(requireContext()), enabled ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            return true;
        });


        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDividerHeight(0);
    }

    private void updateHomeDirPrefSummary() {
        mHomeDirPref.setSummary(getString(R.string.settings_main_home_directory_summary, mHelper.getHomeDirectory()));
    }

    private void updateFilePickerSortSummary() {
        mFilePickerSortPref.setSummary(getString(R.string.settings_main_file_picker_sort_summary, getResources().getStringArray(R.array.file_picker_sort_variants)[mHelper.getFilePickerRawSort()]));
    }

    private void updateInstallerSummary() {
        mInstallerPref.setSummary(getString(R.string.settings_main_installer_summary, getResources().getStringArray(R.array.installers)[mHelper.getInstaller()]));
    }

    private void updateThemeSummary() {
        mThemePref.setSummary(Theme.getInstance(requireContext()).getConcreteTheme().getName(requireContext()));
    }

    private void updateAutoThemePickerSummary() {
        Theme theme = Theme.getInstance(requireContext());
        mAutoThemePicker.setSummary(getString(R.string.settings_main_auto_theme_picker_summary, theme.getLightTheme().getName(requireContext()), theme.getDarkTheme().getName(requireContext())));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsUtils.REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED)
                AlertsUtils.showAlert(this, R.string.error, R.string.permissions_required_storage);
            else {
            }
        }

        if (requestCode == PermissionsUtils.REQUEST_CODE_SHIZUKU) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED)
                AlertsUtils.showAlert(this, R.string.error, R.string.permissions_required_shizuku);
            else {
                mHelper.setInstaller(PreferencesValues.INSTALLER_SHIZUKU);
                updateInstallerSummary();
            }
        }
    }

    @Override
    public void onItemSelected(String dialogTag, int selectedItemIndex) {
        switch (dialogTag) {
            case "sort":
                break;
            case "installer":
                boolean installerSet = false;
                switch (selectedItemIndex) {
                    case PreferencesValues.INSTALLER_ROOTLESS:
                        installerSet = true;
                        break;
                    case PreferencesValues.INSTALLER_ROOTED:
                        if (!SuShell.getInstance().requestRoot()) {
                            AlertsUtils.showAlert(this, R.string.error, R.string.settings_main_use_root_error);
                            return;
                        }
                        installerSet = true;
                        break;
                    case PreferencesValues.INSTALLER_SHIZUKU:
                        if (!Utils.apiIsAtLeast(Build.VERSION_CODES.M)) {
                            AlertsUtils.showAlert(this, R.string.error, R.string.settings_main_installer_error_shizuku_pre_m);
                            return;
                        }

                        if (!Shizuku.pingBinder()) {
                            AlertsUtils.showAlert(this, R.string.error, R.string.settings_main_installer_error_no_shizuku);
                            return;
                        }

                        if (!Shizuku.isPreV11() && Shizuku.getVersion() >= 11) {
                            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                                installerSet = true;
                            } else {
                                Shizuku.requestPermission(PermissionsUtils.REQUEST_CODE_SHIZUKU);
                            }
                        } else {
                            installerSet = PermissionsUtils.checkAndRequestShizukuPermissions(this);
                        }

                        break;
                }
                if (installerSet) {
                    mHelper.setInstaller(selectedItemIndex);
                    updateInstallerSummary();
                }
                break;
        }
    }

    @Override
    public void onDialogDismissed(@NonNull String dialogTag) {
        switch (dialogTag) {
            case "theme":
                updateThemeSummary();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        if (Utils.apiIsAtLeast(Build.VERSION_CODES.M)) {
            Shizuku.removeRequestPermissionResultListener(this);
        }
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(PreferencesKeys.USE_OLD_INSTALLER)) {
            prefs.edit().putBoolean(PreferencesKeys.USE_OLD_INSTALLER, prefs.getBoolean(PreferencesKeys.USE_OLD_INSTALLER, false)).commit();
            Utils.hardRestartApp(requireContext());
        }
    }

    @Override
    public void onThemesChosen(@Nullable String tag, Theme.ThemeDescriptor lightTheme, Theme.ThemeDescriptor darkTheme) {
        Theme theme = Theme.getInstance(requireContext());
        theme.setLightTheme(lightTheme);
        theme.setDarkTheme(darkTheme);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        switch (requestCode) {
            case PermissionsUtils.REQUEST_CODE_SHIZUKU:
                if (grantResult == PackageManager.PERMISSION_DENIED)
                    AlertsUtils.showAlert(this, R.string.error, R.string.permissions_required_shizuku);
                else {
                    mHelper.setInstaller(PreferencesValues.INSTALLER_SHIZUKU);
                    updateInstallerSummary();
                }
                break;
        }
    }
}
