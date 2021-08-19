package com.mcool.sai.installerx.splitmeta.config;


import androidx.annotation.Nullable;

import com.mcool.sai.installerx.splitmeta.SplitMeta;
import com.mcool.sai.utils.TextUtils;

import java.util.Map;

public abstract class ConfigSplitMeta extends SplitMeta {

    private String mModule;

    public ConfigSplitMeta(Map<String, String> manifestAttrs) {
        super(manifestAttrs);

        mModule = TextUtils.getNullIfEmpty(manifestAttrs.get("configForSplit"));
    }

    @Nullable
    public String module() {
        return mModule;
    }

    public boolean isForModule() {
        return module() != null;
    }
}
