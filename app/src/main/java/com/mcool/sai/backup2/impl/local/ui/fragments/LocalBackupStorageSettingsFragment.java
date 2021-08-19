package com.mcool.sai.backup2.impl.local.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.aefyr.sai.R;
import com.mcool.sai.backup2.impl.local.LocalBackupStorageProvider;
import com.mcool.sai.backup2.impl.local.prefs.LocalBackupStoragePrefConstants;
import com.mcool.sai.model.common.PackageMeta;
import com.mcool.sai.ui.dialogs.NameFormatBuilderDialogFragment;
import com.mcool.sai.utils.BackupNameFormat;

import java.util.Objects;

public class LocalBackupStorageSettingsFragment extends PreferenceFragmentCompat implements NameFormatBuilderDialogFragment.OnFormatBuiltListener {

    private Preference mBackupNameFormatPref;
    private Preference mBackupDirPref;

    private LocalBackupStorageProvider mProvider;

    private PackageMeta mDemoMeta;
    private static final int REQUEST_CODE_SELECT_BACKUP_DIR = 1334;

    public static LocalBackupStorageSettingsFragment newInstance() {
        return new LocalBackupStorageSettingsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDividerHeight(0);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mProvider = LocalBackupStorageProvider.getInstance(requireContext());
        mDemoMeta = Objects.requireNonNull(PackageMeta.forPackage(requireContext(), requireContext().getPackageName()));

        PreferenceManager prefManager = getPreferenceManager();
        prefManager.setSharedPreferencesName(LocalBackupStoragePrefConstants.PREFS_NAME);

        addPreferencesFromResource(R.xml.preferences_lbs);

        mBackupNameFormatPref = findPreference("backup_file_name_format");
        updateBackupNameFormatSummary();
        mBackupNameFormatPref.setOnPreferenceClickListener((p) -> {
            NameFormatBuilderDialogFragment.newInstance(mProvider.getBackupNameFormat()).show(getChildFragmentManager(), "backup_name_format_builder");
            return true;
        });

        mBackupDirPref = findPreference(LocalBackupStoragePrefConstants.KEY_BACKUP_DIR_URI);
        updateBackupDirSummary();
        mBackupDirPref.setOnPreferenceClickListener(p -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.installer_pick_apks)), REQUEST_CODE_SELECT_BACKUP_DIR);
            return true;
        });
    }

    private void updateBackupNameFormatSummary() {
        mBackupNameFormatPref.setSummary(getString(R.string.settings_main_backup_file_name_format_summary, BackupNameFormat.format(mProvider.getBackupNameFormat(), mDemoMeta)));
    }

    private void updateBackupDirSummary() {
        mBackupDirPref.setSummary(getString(R.string.settings_main_backup_backup_dir_summary, mProvider.getBackupDirUri()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_BACKUP_DIR) {
            if (resultCode != Activity.RESULT_OK)
                return;

            Objects.requireNonNull(data);
            Uri backupDirUri = Objects.requireNonNull(data.getData());
            requireContext().getContentResolver().takePersistableUriPermission(backupDirUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            mProvider.setBackupDirUri(backupDirUri);
            updateBackupDirSummary();
        }
    }

    @Override
    public void onFormatBuilt(@Nullable String tag, @NonNull String format) {
        if (tag == null)
            return;

        switch (tag) {
            case "backup_name_format_builder":
                mProvider.setBackupNameFormat(format);
                updateBackupNameFormatSummary();
                break;
        }
    }
}
