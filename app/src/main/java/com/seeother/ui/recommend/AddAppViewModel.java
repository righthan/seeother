package com.seeother.ui.recommend;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.seeother.common.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddAppViewModel extends ViewModel {
    private final MutableLiveData<List<AppInfo>> appList = new MutableLiveData<>();
    private List<AppInfo> allApps = new ArrayList<>();
    private String currentQuery = "";

    public LiveData<List<AppInfo>> getAppList() {
        return appList;
    }

    public void loadInstalledApps(Context context) {
        new Thread(() -> {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppInfo> apps = new ArrayList<>();

            for (ApplicationInfo packageInfo : packages) {
                // 只获取有启动器图标的应用(过滤系统应用)
                if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                    String appName = pm.getApplicationLabel(packageInfo).toString();
                    Drawable appIcon = pm.getApplicationIcon(packageInfo);
                    apps.add(new AppInfo(packageInfo.packageName, appName, appIcon));
                }
            }

            // 按应用名称排序
            Collections.sort(apps, (a1, a2) ->
                    a1.getAppName().compareToIgnoreCase(a2.getAppName()));

            allApps = apps;
            filterApps();
        }).start();
    }

    public void searchApps(String query) {
        currentQuery = query.toLowerCase().trim();
        filterApps();
    }

    private void filterApps() {
        if (currentQuery.isEmpty()) {
            appList.postValue(allApps);
            return;
        }

        List<AppInfo> filteredList = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (app.getAppName().toLowerCase().contains(currentQuery) ||
                    app.getPackageName().toLowerCase().contains(currentQuery)) {
                filteredList.add(app);
            }
        }
        appList.postValue(filteredList);
    }
}