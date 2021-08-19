package com.mcool.sai.backup2;

import com.mcool.sai.model.common.PackageMeta;

public interface BackupApp {

    PackageMeta packageMeta();

    boolean isInstalled();

    BackupStatus backupStatus();
}
