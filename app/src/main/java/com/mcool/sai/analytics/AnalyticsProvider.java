package com.mcool.sai.analytics;

public interface AnalyticsProvider {

    boolean supportsDataCollection();

    boolean isDataCollectionEnabled();

    void setDataCollectionEnabled(boolean enabled);

}
