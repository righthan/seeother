package com.seeother.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.seeother.R;

import java.lang.ref.WeakReference;
import java.time.LocalTime;
import java.util.Calendar;

public class SettingsSecureUtil {
    private static final String TAG = "SettingsSecureUtil";
    private static SettingsSecureUtil instance;
    private final ContentResolver contentResolver;
    private final WeakReference<Context> contextRef;

    private final String serviceName = "com.seeother/.service.MyAccessibilityService";

    private SettingsSecureUtil(ContentResolver contentResolver, Context context) {
        this.contentResolver = contentResolver;
        // 使用WeakReference避免内存泄漏，并使用ApplicationContext
        this.contextRef = new WeakReference<>(context.getApplicationContext());
    }

    public static SettingsSecureUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SettingsSecureUtil 未初始化，请先调用 init() 方法");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SettingsSecureUtil(context.getContentResolver(), context);
        }
    }

    /**
     * 获取Context，如果WeakReference中的Context已被回收则返回null
     */
    private Context getContext() {
        return contextRef != null ? contextRef.get() : null;
    }

    /**
     * 检查是否获得WRITE_SECURE_SETTINGS权限
     * 进入设置或者在设置中设置ColorOS导航方式就会自动授予无障碍权限...
     * 也就是调用的地方都会开启无障碍权限
     */
    public boolean hasWriteSecureSettingsPermission() {
        try {
            Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, serviceName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "检查WRITE_SECURE_SETTINGS权限失败", e);
            return false;
        }
    }

    /**
     * 开启无障碍服务
     */
    public void enableAccessibilityService() {
        try {
            // 先移除然后刷新授权状态, 然后再添加
            disableAccessibilityService();
            new android.os.Handler().postDelayed(() -> {
                Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, serviceName);
            }, 500);
        } catch (Exception e) {
            Log.e(TAG, "开启无障碍服务失败", e);
            Context context = getContext();
            if (context != null) {
                GlobalToast.showShort(context, "请授予ADB权限");
            }
        }
    }

    public void disableAccessibilityService() {
        try {
            String enabledServices = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );

            if (enabledServices != null && enabledServices.contains(serviceName)) {
                // Remove the service name
                String updatedServices = enabledServices.replace(serviceName, "").trim();

                // Update the system setting
                Settings.Secure.putString(
                        contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        updatedServices
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "关闭无障碍服务失败", e);
            Context context = getContext();
            if (context != null) {
                GlobalToast.showShort(context, "请授予ADB权限");
            }
        }
    }

    // 启用颜色空间
    public void enableColorSpace() {
        try {
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 1);
        } catch (Exception e) {
            Log.e(TAG, "启用颜色空间失败", e);
            Context context = getContext();
            if (context != null) {
                GlobalToast.showShort(context, "请授予ADB权限");
            }
        }
    }

    // 禁用颜色空间
    public void disableColorSpace() {
        try {
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 0);
        } catch (Exception e) {
            Log.e(TAG, "禁用颜色空间失败", e);
            Context context = getContext();
            if (context != null) {
                GlobalToast.showShort(context, "请授予ADB权限");
            }
        }
    }

    // 启用高对比度文字
    public void enableHighContrastText() {
        try {
            Settings.Secure.putInt(contentResolver, "high_text_contrast_enabled", 1);
        } catch (Exception e) {
            Log.e(TAG, "启用高对比度文字失败", e);
            Context context = getContext();
            if (context != null) {
                GlobalToast.showShort(context, "请授予ADB权限");
            }
        }
    }

    // 禁用高对比度文字
    public void disableHighContrastText() {
        try {
            Settings.Secure.putInt(contentResolver, "high_text_contrast_enabled", 0);
        } catch (Exception e) {
            Log.e(TAG, "禁用高对比度文字失败", e);
            Context context = getContext();
            if (context != null) {
                GlobalToast.showShort(context, "请授予ADB权限");
            }
        }
    }

    /**
     * 检查当前时间是否在勿扰模式时段内
     *
     * @return true表示在勿扰时段内，应该禁用功能
     */
    public boolean isInDoNotDisturbTime() {
        Context context = getContext();
        if (context == null) {
            return false;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // 获取当前时间和星期
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentTimeInMinutes = currentHour * 60 + currentMinute;

        // 转换为周一到周日的索引 (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ...)
        String[] weekdays = {"", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
        String todayKey = weekdays[dayOfWeek];

        // 检查今天是否启用勿扰模式
        boolean isEnabled = preferences.getBoolean("dnd_" + todayKey + "_enabled", false);
        if (!isEnabled) {
            return false;
        }

        // 获取开始和结束时间
        String startTimeStr = preferences.getString("dnd_" + todayKey + "_start", "22:00");
        String endTimeStr = preferences.getString("dnd_" + todayKey + "_end", "08:00");

        try {
            // 解析时间字符串
            String[] startParts = startTimeStr.split(":");
            String[] endParts = endTimeStr.split(":");

            int startTimeInMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endTimeInMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);

            // 处理跨天的情况
            if (startTimeInMinutes > endTimeInMinutes) {
                // 跨天情况：如 22:00 - 08:00
                return currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes;
            } else {
                // 同一天：如 08:00 - 22:00
                return currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes;
            }
        } catch (Exception e) {
            Log.e(TAG, "解析勿扰时间失败", e);
            return false;
        }
    }

    /**
     * 获取勿扰模式的状态描述
     *
     * @return 勿扰模式状态描述
     */
    public String getDoNotDisturbStatus() {
        Context context = getContext();
        if (context == null) {
            return "无法获取状态";
        }

        if (isInDoNotDisturbTime()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String[] weekdays = {"", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
            String todayKey = weekdays[dayOfWeek];

            String startTime = preferences.getString("dnd_" + todayKey + "_start", "22:00");
            String endTime = preferences.getString("dnd_" + todayKey + "_end", "08:00");

            return "勿扰模式已启用 (" + startTime + " - " + endTime + ")";
        } else {
            return "勿扰模式未启用";
        }
    }
}
