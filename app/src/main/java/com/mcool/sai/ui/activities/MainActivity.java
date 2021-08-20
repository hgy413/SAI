package com.mcool.sai.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.aefyr.sai.R;
import com.mcool.sai.backup2.impl.DefaultBackupManager;
import com.mcool.sai.ui.fragments.BackupFragment;
import com.mcool.sai.ui.fragments.Installer2Fragment;
import com.mcool.sai.ui.fragments.InstallerFragment;
import com.mcool.sai.utils.FragmentNavigator;
import com.mcool.sai.utils.MiuiUtils;
import com.mcool.sai.utils.PreferencesKeys;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends ThemedActivity implements BottomNavigationView.OnNavigationItemSelectedListener, FragmentNavigator.FragmentFactory {

    private BottomNavigationView mBottomNavigationView;

    private FragmentNavigator mFragmentNavigator;

    private InstallerFragment mInstallerFragment;

    private boolean mIsNavigationEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO is this ok?
        DefaultBackupManager.getInstance(this);

        showMiuiWarning();


        mBottomNavigationView = findViewById(R.id.bottomnav_main);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        mFragmentNavigator = new FragmentNavigator(savedInstanceState, getSupportFragmentManager(), R.id.container_main, this);
        mInstallerFragment = mFragmentNavigator.findFragmentByTag("installer");
        if (savedInstanceState == null)
            mFragmentNavigator.switchTo("installer");

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            deliverActionViewUri(intent.getData());
            getIntent().setData(null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            deliverActionViewUri(intent.getData());
        }
    }

    private void deliverActionViewUri(Uri uri) {
        if (!mIsNavigationEnabled) {
            Toast.makeText(this, R.string.main_navigation_disabled, Toast.LENGTH_SHORT).show();
            return;
        }
        mBottomNavigationView.getMenu().getItem(0).setChecked(true);
        mFragmentNavigator.switchTo("installer");
        getInstallerFragment().handleActionView(uri);
    }

    private void showMiuiWarning() {
        if (MiuiUtils.isMiui() && !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesKeys.MIUI_WARNING_SHOWN, false)) {
            startActivity(new Intent(this, MiActivity.class));
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_installer:
                mFragmentNavigator.switchTo("installer");
                break;
            case R.id.menu_backup:
                mFragmentNavigator.switchTo("backup");
                break;
        }

        return true;
    }

    @Override
    public Fragment createFragment(String tag) {
        switch (tag) {
            case "installer":
                return getInstallerFragment();
            case "backup":
                return new BackupFragment();
        }

        throw new IllegalArgumentException("Unknown fragment tag: " + tag);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentNavigator.writeStateToBundle(outState);
    }

    private InstallerFragment getInstallerFragment() {
        if (mInstallerFragment == null)
            mInstallerFragment = new Installer2Fragment();
        return mInstallerFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
