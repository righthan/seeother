package com.seeother.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 统计管理器
 * 负责管理少用应用打开次数和短视频浏览次数的统计
 */
public class StatisticsManager {
    private static final String TAG = "StatisticsManager";
    private static final String PREF_NAME = "statistics_pref";
    
    // 少用应用打开次数相关
    private static final String KEY_MONITORED_APP_OPEN_COUNT = "monitored_app_open_count";
    private static final String KEY_LAST_MONITORED_APP_CHECK_DATE = "last_monitored_app_check_date";
    
    // 短视频浏览次数相关
    private static final String KEY_SHORT_VIDEO_COUNT_TODAY = "short_video_count_today";
    private static final String KEY_SHORT_VIDEO_COUNT_MONTH = "short_video_count_month";
    private static final String KEY_LAST_VIDEO_CHECK_DATE = "last_video_check_date";
    private static final String KEY_LAST_MONTH_CHECK = "last_month_check";
    
    // 阈值
    private static final int MONITORED_APP_THRESHOLD = 10;
    private static final int SHORT_VIDEO_THRESHOLD = 10;
    
    private final SharedPreferences preferences;
    private final Context context;
    private final SettingsManager settingsManager;

    public StatisticsManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.settingsManager = new SettingsManager(context);
        checkAndResetIfNeeded();
    }

    /**
     * 检查是否需要重置数据（每天/每月）
     */
    private void checkAndResetIfNeeded() {
        String today = getTodayDate();
        String lastVideoCheckDate = preferences.getString(KEY_LAST_VIDEO_CHECK_DATE, "");
        
        // 检查是否需要重置每日数据
        if (!today.equals(lastVideoCheckDate)) {
            Log.d(TAG, "新的一天，重置每日短视频计数");
            preferences.edit()
                    .putInt(KEY_SHORT_VIDEO_COUNT_TODAY, 0)
                    .putString(KEY_LAST_VIDEO_CHECK_DATE, today)
                    .apply();
        }
        
        // 检查是否需要重置每月数据
        String currentMonth = getCurrentMonth();
        String lastMonthCheck = preferences.getString(KEY_LAST_MONTH_CHECK, "");
        
        if (!currentMonth.equals(lastMonthCheck)) {
            Log.d(TAG, "新的月份，重置每月数据");
            preferences.edit()
                    .putInt(KEY_MONITORED_APP_OPEN_COUNT, 0)
                    .putInt(KEY_SHORT_VIDEO_COUNT_MONTH, 0)
                    .putString(KEY_LAST_MONTH_CHECK, currentMonth)
                    .apply();
        }
    }

    /**
     * 获取今天的日期字符串 (yyyy-MM-dd)
     */
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 获取当前月份字符串 (yyyy-MM)
     */
    private String getCurrentMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 增加少用应用打开次数
     * @return 如果达到阈值返回true
     */
    public boolean incrementMonitoredAppOpenCount() {
        checkAndResetIfNeeded();
        
        int currentCount = preferences.getInt(KEY_MONITORED_APP_OPEN_COUNT, 0);
        currentCount++;
        
        preferences.edit()
                .putInt(KEY_MONITORED_APP_OPEN_COUNT, currentCount)
                .apply();
        
        Log.d(TAG, "少用应用打开次数: " + currentCount);
        
        // 每达到设置的阈值返回true
        int threshold = settingsManager.getMonitoredAppThreshold();
        if (currentCount % threshold == 0) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取少用应用打开次数
     */
    public int getMonitoredAppOpenCount() {
        return preferences.getInt(KEY_MONITORED_APP_OPEN_COUNT, 0);
    }

    /**
     * 增加短视频浏览次数
     * @return 如果达到阈值返回true
     */
    public boolean incrementShortVideoCount() {
        checkAndResetIfNeeded();
        
        int todayCount = preferences.getInt(KEY_SHORT_VIDEO_COUNT_TODAY, 0);
        int monthCount = preferences.getInt(KEY_SHORT_VIDEO_COUNT_MONTH, 0);
        
        todayCount++;
        monthCount++;
        
        preferences.edit()
                .putInt(KEY_SHORT_VIDEO_COUNT_TODAY, todayCount)
                .putInt(KEY_SHORT_VIDEO_COUNT_MONTH, monthCount)
                .apply();
        
        Log.d(TAG, "短视频浏览次数 - 今天: " + todayCount + ", 本月: " + monthCount);
        
        // 每达到设置的阈值返回true
        int threshold = settingsManager.getShortVideoThreshold();
        if (monthCount % threshold == 0) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取今天的短视频浏览次数
     */
    public int getShortVideoCountToday() {
        checkAndResetIfNeeded();
        return preferences.getInt(KEY_SHORT_VIDEO_COUNT_TODAY, 0);
    }

    /**
     * 获取本月的短视频浏览次数
     */
    public int getShortVideoCountMonth() {
        checkAndResetIfNeeded();
        return preferences.getInt(KEY_SHORT_VIDEO_COUNT_MONTH, 0);
    }

    /**
     * 将视频数量转换为时间（每个视频30秒）
     * @param count 视频数量
     * @return 格式化的时间字符串，如 "2h35min"
     */
    public String formatTimeFromVideoCount(int count) {
        int totalSeconds = count * 30;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        
        if (hours > 0) {
            return hours + "h" + minutes + "min";
        } else if (minutes > 0) {
            return minutes + "min";
        } else {
            return totalSeconds + "s";
        }
    }

    /**
     * 获取短视频统计信息
     */
    public VideoStatistics getVideoStatistics() {
        checkAndResetIfNeeded();
        int todayCount = getShortVideoCountToday();
        int monthCount = getShortVideoCountMonth();
        String todayTime = formatTimeFromVideoCount(todayCount);
        String monthTime = formatTimeFromVideoCount(monthCount);
        
        return new VideoStatistics(todayCount, monthCount, todayTime, monthTime);
    }

    /**
     * 短视频统计信息类
     */
    public static class VideoStatistics {
        public final int todayCount;
        public final int monthCount;
        public final String todayTime;
        public final String monthTime;

        public VideoStatistics(int todayCount, int monthCount, String todayTime, String monthTime) {
            this.todayCount = todayCount;
            this.monthCount = monthCount;
            this.todayTime = todayTime;
            this.monthTime = monthTime;
        }
    }
}
