package com.seeother.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.seeother.data.dao.AppGuardRuleRoomDao;
import com.seeother.data.db.AppDatabase;
import com.seeother.data.entity.AppGuardRule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用守卫规则仓库
 */
public class AppGuardRuleRepository {
    private final AppGuardRuleRoomDao appGuardRuleDao;
    private final ExecutorService executor;

    public AppGuardRuleRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        appGuardRuleDao = database.appGuardRuleDao();
        executor = Executors.newFixedThreadPool(4);
    }

    // 插入单个规则
    public void insert(AppGuardRule rule) {
        executor.execute(() -> appGuardRuleDao.insert(rule));
    }

    // 批量插入规则
    public void insertAll(List<AppGuardRule> rules) {
        executor.execute(() -> appGuardRuleDao.insertAll(rules));
    }

    // 更新规则
    public void update(AppGuardRule rule) {
        executor.execute(() -> appGuardRuleDao.update(rule));
    }

    // 删除规则
    public void delete(AppGuardRule rule) {
        executor.execute(() -> appGuardRuleDao.delete(rule));
    }

    // 删除所有规则
    public void deleteAll() {
        executor.execute(appGuardRuleDao::deleteAll);
    }

    // 获取所有规则（LiveData）
    public LiveData<List<AppGuardRule>> getAllRules() {
        return appGuardRuleDao.getAllRules();
    }

    // 获取所有规则（同步）
    public List<AppGuardRule> getAllRulesSync() {
        return appGuardRuleDao.getAllRulesSync();
    }

    // 获取指定包名的规则（LiveData）
    public LiveData<List<AppGuardRule>> getRulesForPackage(String packageName) {
        return appGuardRuleDao.getRulesForPackage(packageName);
    }

    // 获取指定包名的规则（同步）
    public List<AppGuardRule> getRulesForPackageSync(String packageName) {
        return appGuardRuleDao.getRulesForPackageSync(packageName);
    }


    // 获取所有包名
    public LiveData<List<String>> getAllPackages() {
        return appGuardRuleDao.getAllPackages();
    }

    // 根据ID获取规则
    public AppGuardRule getRuleById(int id) {
        return appGuardRuleDao.getRuleById(id);
    }

    // 获取规则数量
    public int getRuleCount() {
        return appGuardRuleDao.getRuleCount();
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
} 