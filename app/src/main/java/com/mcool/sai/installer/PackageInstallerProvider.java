package com.mcool.sai.installer;

import android.content.Context;

import com.mcool.sai.installer.rootless.RootlessSAIPackageInstaller;

public class PackageInstallerProvider {
    public static SAIPackageInstaller getInstaller(Context c) {
        return RootlessSAIPackageInstaller.getInstance(c); // 仅使用默认安装模式
    }
}
