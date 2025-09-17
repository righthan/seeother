package com.seeother.ui.monitor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.seeother.common.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class MonitorViewModel extends ViewModel {
    private final MutableLiveData<List<AppInfo>> appList = new MutableLiveData<>();

    public LiveData<List<AppInfo>> getAppList() {
        return appList;
    }

    public void loadInstalledApps(Context context) {
        new Thread(() -> {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppInfo> apps = new ArrayList<>();

            for (ApplicationInfo packageInfo : packages) {
                    String appName = pm.getApplicationLabel(packageInfo).toString();
                    Drawable appIcon = pm.getApplicationIcon(packageInfo);
                    apps.add(new AppInfo(packageInfo.packageName, appName, appIcon));
            }

            // 按应用名称排序
            apps.sort((a1, a2) ->
                    a1.getAppName().compareToIgnoreCase(a2.getAppName()));

            appList.postValue(apps);
        }).start();
    }
}