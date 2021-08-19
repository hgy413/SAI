package com.mcool.sai.installer.shizuku;

import android.content.Context;

import com.aefyr.sai.R;
import com.mcool.sai.installer.ShellSAIPackageInstaller;
import com.mcool.sai.shell.Shell;
import com.mcool.sai.shell.ShizukuShell;

public class ShizukuSAIPackageInstaller extends ShellSAIPackageInstaller {
    private static ShizukuSAIPackageInstaller sInstance;

    public static ShizukuSAIPackageInstaller getInstance(Context c) {
        synchronized (ShizukuSAIPackageInstaller.class) {
            return sInstance != null ? sInstance : new ShizukuSAIPackageInstaller(c);
        }
    }

    private ShizukuSAIPackageInstaller(Context c) {
        super(c);
        sInstance = this;
    }

    @Override
    protected Shell getShell() {
        return ShizukuShell.getInstance();
    }

    @Override
    protected String getInstallerName() {
        return "Shizuku";
    }

    @Override
    protected String getShellUnavailableMessage() {
        return getContext().getString(R.string.installer_error_shizuku_unavailable);
    }
}
