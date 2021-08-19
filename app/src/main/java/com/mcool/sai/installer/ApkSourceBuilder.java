package com.mcool.sai.installer;

import android.content.Context;
import android.net.Uri;

import com.mcool.sai.model.apksource.ApkSource;
import com.mcool.sai.model.apksource.DefaultApkSource;
import com.mcool.sai.model.apksource.FilterApkSource;
import com.mcool.sai.model.apksource.SignerApkSource;
import com.mcool.sai.model.apksource.ZipApkSource;
import com.mcool.sai.model.apksource.ZipBackedApkSource;
import com.mcool.sai.model.apksource.ZipFileApkSource;
import com.mcool.sai.model.filedescriptor.ContentUriFileDescriptor;
import com.mcool.sai.model.filedescriptor.FileDescriptor;
import com.mcool.sai.model.filedescriptor.NormalFileDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApkSourceBuilder {

    private Context mContext;

    private boolean mSourceSet;
    private List<File> mApkFiles;
    private File mZipFile;
    private Uri mZipUri;
    private List<Uri> mApkUris;

    private boolean mSigningEnabled;
    private boolean mReadZipViaZipFileEnabled;

    private Set<String> mFilteredApks;
    private boolean mBlacklist;

    public ApkSourceBuilder(Context c) {
        mContext = c;
    }

    public ApkSourceBuilder fromApkFiles(List<File> apkFiles) {
        ensureSourceSetOnce();
        mApkFiles = apkFiles;
        return this;
    }

    public ApkSourceBuilder fromZipFile(File zipFile) {
        ensureSourceSetOnce();
        mZipFile = zipFile;
        return this;
    }

    public ApkSourceBuilder fromZipContentUri(Uri zipUri) {
        ensureSourceSetOnce();
        mZipUri = zipUri;
        return this;
    }

    public ApkSourceBuilder fromApkContentUris(List<Uri> uris) {
        ensureSourceSetOnce();
        mApkUris = uris;
        return this;
    }

    public ApkSourceBuilder setSigningEnabled(boolean enabled) {
        mSigningEnabled = enabled;
        return this;
    }

    public ApkSourceBuilder setReadZipViaZipFileEnabled(boolean enabled) {
        mReadZipViaZipFileEnabled = enabled;
        return this;
    }

    public ApkSourceBuilder filterApksByLocalPath(Set<String> filteredApks, boolean blacklist) {
        mFilteredApks = filteredApks;
        mBlacklist = blacklist;
        return this;
    }

    public ApkSource build() {
        ApkSource apkSource;

        if (mApkFiles != null) {
            List<FileDescriptor> apkFileDescriptors = new ArrayList<>(mApkFiles.size());
            for (File apkFile : mApkFiles)
                apkFileDescriptors.add(new NormalFileDescriptor(apkFile));

            apkSource = new DefaultApkSource(apkFileDescriptors);
        } else if (mZipFile != null) {
            ZipBackedApkSource zipBackedApkSource;
            if (mReadZipViaZipFileEnabled)
                zipBackedApkSource = new ZipFileApkSource(mContext, new NormalFileDescriptor(mZipFile));
            else
                zipBackedApkSource = new ZipApkSource(mContext, new NormalFileDescriptor(mZipFile));

            apkSource = zipBackedApkSource;
        } else if (mZipUri != null) {
            ZipBackedApkSource zipBackedApkSource;
            if (mReadZipViaZipFileEnabled)
                zipBackedApkSource = new ZipFileApkSource(mContext, new ContentUriFileDescriptor(mContext, mZipUri));
            else
                zipBackedApkSource = new ZipApkSource(mContext, new ContentUriFileDescriptor(mContext, mZipUri));

            apkSource = zipBackedApkSource;
        } else if (mApkUris != null) {
            List<FileDescriptor> apkUriDescriptors = new ArrayList<>(mApkUris.size());
            for (Uri apkUri : mApkUris)
                apkUriDescriptors.add(new ContentUriFileDescriptor(mContext, apkUri));

            apkSource = new DefaultApkSource(apkUriDescriptors);
        } else {
            throw new IllegalStateException("No source set");
        }

        if (mSigningEnabled)
            apkSource = new SignerApkSource(mContext, apkSource);

        if (mFilteredApks != null)
            apkSource = new FilterApkSource(apkSource, mFilteredApks, mBlacklist);

        return apkSource;
    }

    private void ensureSourceSetOnce() {
        if (mSourceSet)
            throw new IllegalStateException("Source can be only be set once");
        mSourceSet = true;
    }
}
