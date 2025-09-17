package com.seeother.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.seeother.data.entity.RecommendApp;

import java.util.List;

@Dao
public interface RecommendAppRoomDao {

    @Insert
    long insert(RecommendApp app);

    @Insert
    void insertBatch(List<RecommendApp> apps);

    @Update
    int update(RecommendApp app);

    @Delete
    int delete(RecommendApp app);

    @Query("DELETE FROM recommend_app WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM recommend_app")
    List<RecommendApp> getAllApps();

    @Query("SELECT * FROM recommend_app WHERE id = :id")
    RecommendApp getAppById(long id);

    @Query("SELECT * FROM recommend_app WHERE pkgName = :pkgName")
    RecommendApp getAppByPkgName(String pkgName);

    @Query("SELECT * FROM recommend_app ORDER BY weight DESC")
    List<RecommendApp> getAppsByWeight();

    @Query("DELETE FROM recommend_app")
    void deleteAll();
} 