package com.seeother.data.entity;

import android.graphics.drawable.Drawable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "recommend_app")
public class RecommendApp {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private int weight;
    private String pkgName;

    // These fields are not stored in database
    @Ignore
    private String appName;
    @Ignore
    private Drawable appIcon;

    // Default constructor for Room
    public RecommendApp() {}

    // Constructor with parameters
    @Ignore
    public RecommendApp(int weight, String pkgName, String appName, Drawable appIcon) {
        this.weight = weight;
        this.pkgName = pkgName;
        this.appName = appName;
        this.appIcon = appIcon;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
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