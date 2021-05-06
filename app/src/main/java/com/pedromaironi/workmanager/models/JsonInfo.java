package com.pedromaironi.workmanager.models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.pedromaironi.workmanager.ui.DownloadApk;
import com.pedromaironi.workmanager.ui.MainActivity;

public class JsonInfo {
    String nameApp;
    String downloadUrl;
    String currentVersionCode;
    String currentVersionName;
    String oldVersionCode;
    String oldVersionName;
    SharedPreferences data;
    SharedPreferences.Editor edit;
    private final String TAG = "CHECK_JSON";
    private AppInfo mAppInfo;
    private AlertDialog mDialogDownloadApp;

    public JsonInfo(){
        mAppInfo = new AppInfo();
    }

    public JsonInfo(String nameApp, String downloadUrl, String currentVersionCode, String currentVersionName, String oldVersionCode, String oldVersionName) {
        this.nameApp = nameApp;
        this.downloadUrl = downloadUrl;
        this.currentVersionCode = currentVersionCode;
        this.currentVersionName = currentVersionName;
        this.oldVersionCode = oldVersionCode;
        this.oldVersionName = oldVersionName;
    }

    public String getNameApp() {
        return nameApp;
    }

    public void setNameApp(String nameApp) {
        this.nameApp = nameApp;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getCurrentVersionCode() {
        return currentVersionCode;
    }

    public void setCurrentVersionCode(String currentVersionCode) {
        this.currentVersionCode = currentVersionCode;
    }

    public String getCurrentVersionName() {
        return currentVersionName;
    }

    public void setCurrentVersionName(String currentVersionName) {
        this.currentVersionName = currentVersionName;
    }

    public String getOldVersionCode() {
        return oldVersionCode;
    }

    public void setOldVersionCode(String oldVersionCode) {
        this.oldVersionCode = oldVersionCode;
    }

    public String getOldVersionName() {
        return oldVersionName;
    }

    public void setOldVersionName(String oldVersionName) {
        this.oldVersionName = oldVersionName;
    }




}
