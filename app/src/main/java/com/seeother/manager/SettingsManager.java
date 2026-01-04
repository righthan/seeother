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
}