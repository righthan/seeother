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
    
    // 守卫相关配置
    private boolean guardEnabled;           // 是否启用守卫
    private int scrollCount;               // 滑动次数阈值
    private long broadcastInterval;        // 检测间隔（毫秒）

    // These fields are not stored in database
    @Ignore
    private String appName;
    @Ignore
    private Drawable appIcon;

    // Default constructor for Room
    public MonitoredApp() {
        this.guardEnabled = false;         // 默认不启用守卫
        this.scrollCount = 5;             // 默认5个滑动触发
        this.broadcastInterval = 500L;    // 默认500ms间隔
    }

    // Constructor with original parameters
    @Ignore
    public MonitoredApp(boolean enableGrayMode, String pkgName) {
        this();
        this.enableGrayMode = enableGrayMode;
        this.enableHighContrast = false;
        this.pkgName = pkgName;
    }

    // New constructor including appName and appIcon
    @Ignore
    public MonitoredApp(boolean enableGrayMode, String pkgName, String appName, Drawable appIcon) {
        this();
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

    // 守卫相关字段的 getter 和 setter
    public boolean isGuardEnabled() {
        return guardEnabled;
    }

    public void setGuardEnabled(boolean guardEnabled) {
        this.guardEnabled = guardEnabled;
    }

    public int getScrollCount() {
        return scrollCount;
    }

    public void setScrollCount(int scrollCount) {
        this.scrollCount = scrollCount;
    }

    public long getBroadcastInterval() {
        return broadcastInterval;
    }

    public void setBroadcastInterval(long broadcastInterval) {
        this.broadcastInterval = broadcastInterval;
    }
}