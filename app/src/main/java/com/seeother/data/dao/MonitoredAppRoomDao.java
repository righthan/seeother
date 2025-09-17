package com.seeother.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.seeother.data.entity.MonitoredApp;

import java.util.List;

@Dao
public interface MonitoredAppRoomDao {

    @Insert
    long insert(MonitoredApp app);

    @Insert
    void insertBatch(List<MonitoredApp> apps);

    @Update
    int update(MonitoredApp app);

    @Delete
    int delete(MonitoredApp app);

    @Query("DELETE FROM monitored_app WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM monitored_app")
    List<MonitoredApp> getAllApps();

    @Query("SELECT * FROM monitored_app WHERE id = :id")
    MonitoredApp getAppById(long id);

    @Query("SELECT * FROM monitored_app WHERE pkgName = :pkgName")
    MonitoredApp getAppByPkgName(String pkgName);

    @Query("DELETE FROM monitored_app")
    void deleteAll();
}