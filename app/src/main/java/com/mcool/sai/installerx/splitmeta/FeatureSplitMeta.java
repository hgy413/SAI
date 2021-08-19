package com.mcool.sai.installerx.splitmeta;

import com.mcool.sai.utils.TextUtils;

import java.util.Map;

public class FeatureSplitMeta extends SplitMeta {

    private String mModule;

    public FeatureSplitMeta(Map<String, String> manifestAttrs) {
        super(manifestAttrs);
        mModule = TextUtils.requireNonEmpty(manifestAttrs.get("split"));
    }

    public String module() {
        return mModule;
    }

}
