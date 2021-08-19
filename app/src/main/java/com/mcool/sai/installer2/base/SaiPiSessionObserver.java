package com.mcool.sai.installer2.base;

import com.mcool.sai.installer2.base.model.SaiPiSessionState;

public interface SaiPiSessionObserver {

    void onSessionStateChanged(SaiPiSessionState state);

}
