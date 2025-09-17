package com.seeother.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.seeother.data.entity.AppGuardRule;

import java.util.List;

/**
 * 应用守卫规则 Room DAO
 */
@Dao
public interface AppGuardRuleRoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppGuardRule rule);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AppGuardRule> rules);

    @Update
    void update(AppGuardRule rule);

    @Delete
    void delete(AppGuardRule rule);

    @Query("DELETE FROM app_guard_rules")
    void deleteAll();

    @Query("SELECT * FROM app_guard_rules ORDER BY packageName ASC")
    LiveData<List<AppGuardRule>> getAllRules();

    @Query("SELECT * FROM app_guard_rules ORDER BY packageName ASC")
    List<AppGuardRule> getAllRulesSync();

    @Query("SELECT * FROM app_guard_rules WHERE packageName = :packageName")
    LiveData<List<AppGuardRule>> getRulesForPackage(String packageName);

    @Query("SELECT * FROM app_guard_rules WHERE packageName = :packageName")
    List<AppGuardRule> getRulesForPackageSync(String packageName);

    @Query("SELECT * FROM app_guard_rules WHERE enabled = 1")
    LiveData<List<AppGuardRule>> getEnabledRules();

    @Query("SELECT * FROM app_guard_rules WHERE enabled = 1")
    List<AppGuardRule> getEnabledRulesSync();

    @Query("UPDATE app_guard_rules SET enabled = :enabled WHERE packageName = :packageName")
    void setPackageEnabled(String packageName, boolean enabled);

    @Query("UPDATE app_guard_rules SET scrollCount = :scrollCount WHERE packageName = :packageName")
    void setPackageScrollCount(String packageName, int scrollCount);

    @Query("UPDATE app_guard_rules SET broadcastInterval = :interval WHERE packageName = :packageName")
    void setPackageBroadcastInterval(String packageName, long interval);

    @Query("SELECT DISTINCT packageName FROM app_guard_rules ORDER BY packageName ASC")
    LiveData<List<String>> getAllPackages();

    @Query("SELECT * FROM app_guard_rules WHERE id = :id")
    AppGuardRule getRuleById(int id);

    @Query("SELECT COUNT(*) FROM app_guard_rules")
    int getRuleCount();
} 