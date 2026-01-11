package com.seeother.manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * 设置管理工具类
 * 提供所有配置项的读取方法，方便其他模块访问
 */
public class SettingsManager {

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // 应用行为设置
    public boolean isHideFromRecent() {
        return preferences.getBoolean("hide_from_recent", false);
    }

    public boolean isGrayModeForNonRecommendAppsEnabled() {
        return preferences.getBoolean("enable_gray_mode_for_non_recommend_apps", false);
    }

    public long getPauseUntilTimestamp() {
        return preferences.getLong("pause_until_timestamp", 0);
    }

    // 暂停功能相关
    public void setPauseUntilTimestamp(long timestamp) {
        preferences.edit().putLong("pause_until_timestamp", timestamp).apply();
    }

    /**
     * 检查是否处于功能暂停状态
     *
     * @return 当设置了暂停时间且未过期, 或者暂停直到手动开启(时间被设置为-1)
     */
    public boolean getPauseEnabled() {
        long pauseUntil = getPauseUntilTimestamp();
        if (pauseUntil == -1) {
            return true; // 暂停直到手动开启
        }
        return pauseUntil > 0 && System.currentTimeMillis() < pauseUntil;
    }

    public void clearPause() {
        preferences.edit().remove("pause_until_timestamp").apply();
    }

    // 紧急场景设置
    public boolean isVolumeDownQuickRestoreEnabled() {
        return preferences.getBoolean("volume_down_quick_restore", true);
    }

    public int getVolumeClickInterval() {
        return preferences.getInt("volume_click_interval", 300);
    }

    public boolean isVolumeBothPauseTimeEnabled() {
        return preferences.getBoolean("volume_both_pause_time_enabled", true);
    }

    public boolean isVolumeDownLongPressEnabled() {
        return preferences.getBoolean("volume_down_long_press_enabled", true);
    }

    public int getPauseDurationMinutes() {
        try {
            String value = preferences.getString("pause_duration_minutes", "15");
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    // 推荐应用设置
    public boolean isRecommendOnLessUsedAppEnabled() {
        return preferences.getBoolean("recommend_on_less_used_app", false);
    }

    public int getRecommendIntervalMinutes() {
        try {
            String value = preferences.getString("recommend_interval_minutes", "1");
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // 上次推荐时间记录
    public long getLastRecommendTime() {
        return preferences.getLong("last_recommend_time", 0);
    }

    public void setLastRecommendTime(long timestamp) {
        preferences.edit().putLong("last_recommend_time", timestamp).apply();
    }

    // 雷打不动时间设置
    /**
     * 保存雷打不动时间段列表
     * 格式: "HH:mm-HH:mm;HH:mm-HH:mm" 例如: "22:00-23:59;06:00-07:30"
     */
    public void setUnshakableTimePeriods(String timePeriods) {
        preferences.edit().putString("unshakable_time_periods", timePeriods).apply();
    }

    /**
     * 获取雷打不动时间段列表
     */
    public String getUnshakableTimePeriods() {
        return preferences.getString("unshakable_time_periods", "");
    }

    /**
     * 检查当前时间是否在雷打不动时间段内
     * @return true表示在雷打不动时间段内
     */
    public boolean isInUnshakableTime() {
        String timePeriods = getUnshakableTimePeriods();
        if (timePeriods == null || timePeriods.trim().isEmpty()) {
            return false;
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(java.util.Calendar.MINUTE);
        int currentTimeInMinutes = currentHour * 60 + currentMinute;

        // 分割多个时间段
        String[] periods = timePeriods.split(";");
        for (String period : periods) {
            period = period.trim();
            if (period.isEmpty()) {
                continue;
            }

            try {
                // 分割开始和结束时间
                String[] times = period.split("-");
                if (times.length != 2) {
                    continue;
                }

                // 解析开始时间
                String[] startParts = times[0].trim().split(":");
                if (startParts.length != 2) {
                    continue;
                }
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int startTimeInMinutes = startHour * 60 + startMinute;

                // 解析结束时间
                String[] endParts = times[1].trim().split(":");
                if (endParts.length != 2) {
                    continue;
                }
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);
                int endTimeInMinutes = endHour * 60 + endMinute;

                // 检查当前时间是否在该时间段内
                if (startTimeInMinutes <= endTimeInMinutes) {
                    // 正常情况：不跨天
                    if (currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes) {
                        return true;
                    }
                } else {
                    // 跨天情况：例如 22:00-02:00
                    if (currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes) {
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略格式错误的时间段
                continue;
            }
        }

        return false;
    }
}