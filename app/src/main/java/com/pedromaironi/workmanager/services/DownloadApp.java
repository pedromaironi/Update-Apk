package com.pedromaironi.workmanager.services;

import android.app.Application;

public class DownloadApp extends Application {

    public static DownloadApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }


    public static DownloadApp getApp() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new DownloadApp();
            mInstance.onCreate();
            return mInstance;
        }
    }

}
