package com.pedromaironi.workmanager.services;

import android.app.Application;

public class DownloadJson extends Application {

    public static DownloadJson mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static DownloadJson getJson() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new DownloadJson();
            mInstance.onCreate();
            return mInstance;
        }
    }
}
