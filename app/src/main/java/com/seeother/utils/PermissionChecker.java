package com.seeother.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.provider.Settings;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * 权限检查工具类
 */
public class PermissionChecker {

    /**
     * 检查悬浮窗权限
     */
    public static boolean hasOverlayPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    /**
     * 检查无障碍权限是否已启用
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (serviceInfo.getResolveInfo().serviceInfo.packageName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查使用情况访问权限
     */
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * 检查WRITE_SECURE_SETTINGS权限
     */
    public static boolean hasWriteSecureSettingsPermission() {
        try {
            return SettingsSecureUtil.getInstance().hasWriteSecureSettingsPermission();
        } catch (Exception e) {
            // 如果SettingsSecureUtil未初始化，返回false
            return false;
        }
    }

}
