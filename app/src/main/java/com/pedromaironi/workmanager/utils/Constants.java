package com.pedromaironi.workmanager.utils;

import android.os.Environment;

import java.io.File;

public class Constants {
    /*Download Constants*/
    public static final String protocolKitKatMinus = "http:";
    public static final String protocolKitKatPlus = "https:";
    public static final String DOWNLOAD_FILE_URL = "//updateapk.pedromaironi.com/checkversion.json";
    public static final String DOWNLOAD_FILE_JSON_NAME = "InformationVersion";
    public static final String TAG_WORKER_THREAD = "dowloadThread";
    public static final String Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator;

    public static final String JSON_EXTENSION = ".json";
    public static final String DOWNLOAD_FOLDER_NAME = "fv";

    public static final String INTENT_LOCAL_BROADCAST = "DownloadStatus";
    public static final String INTENT_KEY = "percent";

    public static final String TITLE_NOTIFICATION = "Download Progress";
    public static final String SHARED_PREF_KEY = "downloaderAppKey";

    /*Check version constants*/
    public static final String nameApp = "nameApp";
    public static final String downloadUrl = "downloadUrl";
    public static final String currentVersionCode = "currentVersionCode";
    public static final String currentVersionName = "currentVersionName";
    public static final String oldVersionCode = "oldVersionCode";
    public static final String oldVersionName = "oldVersionName";
}
