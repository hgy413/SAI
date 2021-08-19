package com.mcool.sai.installer2.base.model;

import androidx.annotation.NonNull;

import com.mcool.sai.model.apksource.ApkSource;

public class SaiPiSessionParams {

    private ApkSource mApkSource;

    public SaiPiSessionParams(@NonNull ApkSource apkSource) {
        mApkSource = apkSource;
    }

    @NonNull
    public ApkSource apkSource() {
        return mApkSource;
    }

}
