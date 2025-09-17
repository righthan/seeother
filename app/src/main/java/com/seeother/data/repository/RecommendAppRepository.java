package com.seeother.data.repository;

import android.content.Context;

import com.seeother.data.dao.RecommendAppRoomDao;
import com.seeother.data.db.AppDatabase;
import com.seeother.data.entity.RecommendApp;

import java.util.List;

public class RecommendAppRepository {
    private final RecommendAppRoomDao dao;

    public RecommendAppRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        dao = database.recommendAppDao();
    }

    // 插入单个应用
    public long insert(RecommendApp app) {
        return dao.insert(app);
    }

    // 批量插入应用
    public void insertBatch(List<RecommendApp> apps) {
        dao.insertBatch(apps);
    }

    // 更新应用
    public int update(RecommendApp app) {
        return dao.update(app);
    }

    // 删除应用
    public int delete(long id) {
        return dao.deleteById(id);
    }

    // 获取所有推荐应用
    public List<RecommendApp> getAllApps() {
        return dao.getAllApps();
    }

    // 根据ID获取应用
    public RecommendApp getAppById(long id) {
        return dao.getAppById(id);
    }

    // 根据包名获取应用
    public RecommendApp getAppByPkgName(String pkgName) {
        return dao.getAppByPkgName(pkgName);
    }

    // 按权重排序获取应用
    public List<RecommendApp> getAppsByWeight() {
        return dao.getAppsByWeight();
    }

    // 清空所有数据
    public void deleteAll() {
        dao.deleteAll();
    }
} 