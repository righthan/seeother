package com.seeother.data.db;

import android.content.Context;

import com.seeother.data.entity.MonitoredApp;
import com.seeother.data.repository.MonitoredAppRepository;

import java.util.List;

public class MonitoredAppDao {
    private final MonitoredAppRepository repository;

    public MonitoredAppDao(Context context) {
        repository = new MonitoredAppRepository(context);
    }

    // Insert a single app
    public long insert(MonitoredApp app) {
        return repository.insert(app);
    }

    // Insert multiple apps
    public void insertBatch(List<MonitoredApp> apps) {
        repository.insertBatch(apps);
    }

    // Update an app
    public int update(MonitoredApp app) {
        return repository.update(app);
    }

    // Delete a single app
    public int delete(long id) {
        return repository.delete(id);
    }

    // Delete multiple apps
    public void deleteBatch(List<Long> ids) {
        repository.deleteBatch(ids);
    }

    // Get all monitored apps
    public List<MonitoredApp> getAllApps() {
        return repository.getAllApps();
    }

    // Get app by ID
    public MonitoredApp getAppById(long id) {
        return repository.getAppById(id);
    }

    // Get app by pkgName
    public MonitoredApp getAppByPkgName(String pkgName) {
        return repository.getAppByPkgName(pkgName);
    }
}