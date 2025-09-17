package com.seeother.data.entity;

import android.graphics.drawable.Drawable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "monitored_app")
public class MonitoredApp {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private boolean enableGrayMode;
    private boolean enableHighContrast;
    private String pkgName;

    // These fields are not stored in database
    @Ignore
    private String appName;
    @Ignore
    private Drawable appIcon;

    // Default constructor for Room
    public MonitoredApp() {
    }

    // Constructor with original parameters
    @Ignore
    public MonitoredApp(boolean enableGrayMode, String pkgName) {
        this.enableGrayMode = enableGrayMode;
        this.enableHighContrast = false;
        this.pkgName = pkgName;
    }

    // New constructor including appName and appIcon
    @Ignore
    public MonitoredApp(boolean enableGrayMode, String pkgName, String appName, Drawable appIcon) {
        this.enableGrayMode = enableGrayMode;
        this.enableHighContrast = false;
        this.pkgName = pkgName;
        this.appName = appName;
        this.appIcon = appIcon;
    }

    // Original Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public boolean getEnableGrayMode() {
        return enableGrayMode;
    }

    public void setEnableGrayMode(boolean enableGrayMode) {
        this.enableGrayMode = enableGrayMode;
    }

    public boolean getEnableHighContrast() {
        return enableHighContrast;
    }

    public void setEnableHighContrast(boolean enableHighContrast) {
        this.enableHighContrast = enableHighContrast;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}