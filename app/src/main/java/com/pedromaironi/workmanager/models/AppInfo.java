package com.pedromaironi.workmanager.models;

public class AppInfo {
    String nameApp;
    String currentVersionCode;
    String currentVersionName;

    public AppInfo(){

    }
    public AppInfo(String nameApp, String currentVersionCode, String currentVersionName) {
        this.nameApp = nameApp;
        this.currentVersionCode = currentVersionCode;
        this.currentVersionName = currentVersionName;
    }

    public String getNameApp() {
        return nameApp;
    }

    public void setNameApp(String nameApp) {
        this.nameApp = nameApp;
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
}
