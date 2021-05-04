package com.pedromaironi.workmanager.models;

public class CheckJson {
    String downloadUrl;
    String currentVersionCode;
    String currentVersionName;
    String oldVersionCode;
    String oldVersionName;

    public CheckJson(){

    }
    public CheckJson(String downloadUrl, String currentVersionCode, String currentVersionName, String oldVersionCode, String oldVersionName) {
        this.downloadUrl = downloadUrl;
        this.currentVersionCode = currentVersionCode;
        this.currentVersionName = currentVersionName;
        this.oldVersionCode = oldVersionCode;
        this.oldVersionName = oldVersionName;
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
