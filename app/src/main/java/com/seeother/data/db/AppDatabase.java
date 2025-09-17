package com.seeother.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.seeother.data.dao.AppGuardRuleRoomDao;
import com.seeother.data.dao.MonitoredAppRoomDao;
import com.seeother.data.dao.RecommendAppRoomDao;
import com.seeother.data.entity.AppGuardRule;
import com.seeother.data.entity.MonitoredApp;
import com.seeother.data.entity.RecommendApp;

@Database(
        entities = {
                MonitoredApp.class,
                RecommendApp.class,
                AppGuardRule.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "room.db";
    private static volatile AppDatabase INSTANCE;


    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .allowMainThreadQueries() // 允许主线程查询，生产环境建议移除
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public abstract MonitoredAppRoomDao monitoredAppDao();

    public abstract RecommendAppRoomDao recommendAppDao();

    public abstract AppGuardRuleRoomDao appGuardRuleDao();
} 