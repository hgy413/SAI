package com.mcool.sai.model.backup;

import com.mcool.sai.model.common.AppFeature;

public class SimpleAppFeature implements AppFeature {

    private String mText;

    public SimpleAppFeature(String text) {
        mText = text;
    }

    @Override
    public CharSequence toText() {
        return mText;
    }
}
