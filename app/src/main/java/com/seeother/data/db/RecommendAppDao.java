package com.seeother.data.db;

import android.content.Context;

import com.seeother.data.entity.RecommendApp;
import com.seeother.data.repository.RecommendAppRepository;

import java.util.List;

public class RecommendAppDao {
    private final RecommendAppRepository repository;

    public RecommendAppDao(Context context) {
        repository = new RecommendAppRepository(context);
    }

    // Insert a single app
    public long insert(RecommendApp app) {
        return repository.insert(app);
    }

    // Insert multiple apps
    public void insertBatch(List<RecommendApp> apps) {
        repository.insertBatch(apps);
    }

    // Update an app
    public int update(RecommendApp app) {
        return repository.update(app);
    }

    // Delete a single app
    public int delete(long id) {
        return repository.delete(id);
    }

    // Delete multiple apps
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            repository.delete(id);
        }
    }

    // Delete app by pkgName
    public void deleteByPkgName(String pkgName) {
        RecommendApp app = repository.getAppByPkgName(pkgName);
        if (app != null) {
            repository.delete(app.getId());
        }
    }

    // Get all recommended apps
    public List<RecommendApp> getAllApps() {
        return repository.getAllApps();
    }

    // Get app by ID
    public RecommendApp getAppById(long id) {
        return repository.getAppById(id);
    }

    // Get app by pkgName
    public RecommendApp getAppByPkgName(String pkgName) {
        return repository.getAppByPkgName(pkgName);
    }
}