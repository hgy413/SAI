package com.mcool.sai.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mcool.sai.installer.ApkSourceBuilder;
import com.mcool.sai.installer2.base.SaiPiSessionObserver;
import com.mcool.sai.installer2.base.model.SaiPiSessionParams;
import com.mcool.sai.installer2.base.model.SaiPiSessionState;
import com.mcool.sai.installer2.impl.FlexSaiPackageInstaller;
import com.mcool.sai.model.apksource.ApkSource;
import com.mcool.sai.utils.Event2;
import com.mcool.sai.utils.PreferencesHelper;
import com.mcool.sai.utils.PreferencesValues;

import java.io.File;
import java.util.List;

public class InstallerViewModel extends AndroidViewModel implements SaiPiSessionObserver {
    public static final String EVENT_PACKAGE_INSTALLED = "package_installed";

    /**
     * Payload is a String[2] with shortError at 0 and fullError or null at 1
     */
    public static final String EVENT_INSTALLATION_FAILED = "installation_failed";

    private FlexSaiPackageInstaller mInstaller;
    private PreferencesHelper mPrefsHelper;

    private MutableLiveData<List<SaiPiSessionState>> mSessions = new MutableLiveData<>();

    private MutableLiveData<Event2> mEvents = new MutableLiveData<>();

    public InstallerViewModel(@NonNull Application application) {
        super(application);
        mPrefsHelper = PreferencesHelper.getInstance(getApplication());

        mInstaller = FlexSaiPackageInstaller.getInstance(getApplication());
        mInstaller.registerSessionObserver(this);
    }

    public LiveData<Event2> getEvents() {
        return mEvents;
    }

    public LiveData<List<SaiPiSessionState>> getSessions() {
        return mSessions;
    }

    public void installPackages(List<File> apkFiles) {
        ApkSource apkSource = new ApkSourceBuilder(getApplication())
                .fromApkFiles(apkFiles)
                .setSigningEnabled(mPrefsHelper.shouldSignApks())
                .build();

        install(apkSource);
    }

    public void installPackagesFromZip(List<File> zipWithApkFiles) {
        for (File zipFile : zipWithApkFiles) {
            ApkSource apkSource = new ApkSourceBuilder(getApplication())
                    .fromZipFile(zipFile)
                    .setReadZipViaZipFileEnabled(mPrefsHelper.shouldUseZipFileApi())
                    .setSigningEnabled(mPrefsHelper.shouldSignApks())
                    .build();

            install(apkSource);
        }
    }

    public void installPackagesFromContentProviderZip(Uri zipContentUri) {
        ApkSource apkSource = new ApkSourceBuilder(getApplication())
                .fromZipContentUri(zipContentUri)
                .setReadZipViaZipFileEnabled(mPrefsHelper.shouldUseZipFileApi())
                .setSigningEnabled(mPrefsHelper.shouldSignApks())
                .build();

        install(apkSource);
    }

    public void installPackagesFromContentProviderUris(List<Uri> apkUris) {
        ApkSource apkSource = new ApkSourceBuilder(getApplication())
                .fromApkContentUris(apkUris)
                .setSigningEnabled(mPrefsHelper.shouldSignApks())
                .build();

        install(apkSource);
    }

    private void install(ApkSource apkSource) {
        mInstaller.enqueueSession(mInstaller.createSessionOnInstaller(PreferencesValues.INSTALLER_ROOTLESS, new SaiPiSessionParams(apkSource)));
    }

    @Override
    protected void onCleared() {
        mInstaller.unregisterSessionObserver(this);
    }

    @Override
    public void onSessionStateChanged(SaiPiSessionState state) {
        switch (state.status()) {
            case INSTALLATION_SUCCEED:
                mEvents.setValue(new Event2(EVENT_PACKAGE_INSTALLED, state.packageName()));
                break;
            case INSTALLATION_FAILED:
                mEvents.setValue(new Event2(EVENT_INSTALLATION_FAILED, new String[]{state.shortError(), state.fullError()}));
                break;
        }

        mSessions.setValue(mInstaller.getSessions());
    }
}
