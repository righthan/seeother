package com.seeother.data.repository;

import android.content.Context;

import com.seeother.data.dao.MonitoredAppRoomDao;
import com.seeother.data.db.AppDatabase;
import com.seeother.data.entity.MonitoredApp;

import java.util.List;

public class MonitoredAppRepository {
    private final MonitoredAppRoomDao dao;

    public MonitoredAppRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        dao = database.monitoredAppDao();
    }

    // 插入单个应用
    public long insert(MonitoredApp app) {
        return dao.insert(app);
    }

    // 批量插入应用
    public void insertBatch(List<MonitoredApp> apps) {
        dao.insertBatch(apps);
    }

    // 更新应用
    public int update(MonitoredApp app) {
        return dao.update(app);
    }

    // 删除应用
    public int delete(long id) {
        return dao.deleteById(id);
    }

    // 批量删除应用
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            dao.deleteById(id);
        }
    }

    // 获取所有监控应用
    public List<MonitoredApp> getAllApps() {
        return dao.getAllApps();
    }

    // 根据ID获取应用
    public MonitoredApp getAppById(long id) {
        return dao.getAppById(id);
    }

    // 根据包名获取应用
    public MonitoredApp getAppByPkgName(String pkgName) {
        return dao.getAppByPkgName(pkgName);
    }

    // 清空所有数据
    public void deleteAll() {
        dao.deleteAll();
    }
} 