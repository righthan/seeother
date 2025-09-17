package com.seeother.common;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private final String packageName;
    private final String appName;
    private final Drawable appIcon;


    private boolean checked;

    public AppInfo(String packageName, String appName, Drawable appIcon) {
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
    }

    // Getters
    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public Drawable getAppIcon() { return appIcon; }
    public boolean getChecked(){ return checked;}

    // Setters
    public void setChecked(boolean isChecked){
        checked = isChecked;
    }
}