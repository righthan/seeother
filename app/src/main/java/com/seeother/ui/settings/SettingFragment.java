package com.seeother.ui.settings;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.seeother.R;
import com.seeother.manager.SettingsManager;
import com.seeother.utils.SettingsSecureUtil;

import java.util.List;
import java.util.Objects;

public class SettingFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference pauseModeStatusPref;
    private Preference feedbackAndProblemsPref;
    private Preference overlayPermissionPref;
    private Preference usageStatsPermissionPref;
    private Preference accessibilityPermissionPref;
    private Preference backgroundPermissionPref;
    private Preference writeSecureSettingsPref;
    private Preference lockAppPermissionPref;
    private Preference resetToDefaultPref;

    private EditTextPreference pauseDurationPref;
    private EditTextPreference popupThresholdPref;
    private SwitchPreferenceCompat hideFromRecentPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        initializePreferences();
        setupPermissionPreferences();
        setupSwitchListeners();
        setupEditTextValidation();
        updatePermissionStatus();
        updateEditTextSummaries();
    }

    private void initializePreferences() {
        // 暂停模式状态
        pauseModeStatusPref = findPreference("pause_mode_status");
        
        // 问题与反馈
        feedbackAndProblemsPref = findPreference("feedback_and_problems");
        
        // 权限相关
        overlayPermissionPref = findPreference("overlay_permission");
        usageStatsPermissionPref = findPreference("usage_stats_permission");
        accessibilityPermissionPref = findPreference("accessibility_permission");
        backgroundPermissionPref = findPreference("background_permission");
        writeSecureSettingsPref = findPreference("write_secure_settings");
        lockAppPermissionPref = findPreference("lock_app_permission");

        // 恢复默认值
        resetToDefaultPref = findPreference("reset_to_default");

        // 紧急场景相关
        pauseDurationPref = findPreference("pause_duration_minutes");
        popupThresholdPref = findPreference("popup_threshold_seconds");

        // 应用行为相关
        hideFromRecentPref = findPreference("hide_from_recent");
    }

    private void setupPermissionPreferences() {
        if (feedbackAndProblemsPref != null) {
            feedbackAndProblemsPref.setOnPreferenceClickListener(preference -> {
                openFeedbackWebsite();
                return true;
            });
        }
        
        if (overlayPermissionPref != null) {
            overlayPermissionPref.setOnPreferenceClickListener(preference -> {
                if (!Settings.canDrawOverlays(requireContext())) {
                    requestOverlayPermission();
                }
                return true;
            });
        }

        if (usageStatsPermissionPref != null) {
            usageStatsPermissionPref.setOnPreferenceClickListener(preference -> {
                if (!hasUsageStatsPermission()) {
                    requestUsageStatsPermission();
                }
                return true;
            });
        }

        if (accessibilityPermissionPref != null) {
            accessibilityPermissionPref.setOnPreferenceClickListener(preference -> {
                if (!isAccessibilityServiceEnabled()) {
                    requestAccessibilityPermission();
                }
                return true;
            });
        }

        if (backgroundPermissionPref != null) {
            backgroundPermissionPref.setOnPreferenceClickListener(preference -> {
                requestBackgroundPermission();
                return true;
            });
        }

        if (writeSecureSettingsPref != null) {
            writeSecureSettingsPref.setOnPreferenceClickListener(preference -> {
                showAdbCommand();
                return true;
            });
        }

        if (lockAppPermissionPref != null) {
            lockAppPermissionPref.setOnPreferenceClickListener(preference -> {
                requestLockAppPermission();
                return true;
            });
        }

        if (resetToDefaultPref != null) {
            resetToDefaultPref.setOnPreferenceClickListener(preference -> {
                showResetConfirmDialog();
                return true;
            });
        }
    }

    private void setupSwitchListeners() {
        // 隐藏任务列表开关监听事件
        if (hideFromRecentPref != null) {
            hideFromRecentPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isChecked = (Boolean) newValue;
                ActivityManager systemService = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
                int size = appTasks.size();
                if (size > 0) {
                    appTasks.get(0).setExcludeFromRecents(isChecked); // 设置activity是否隐藏
                }
                return true;
            });
        }
    }

    private void setupEditTextValidation() {
        // 暂停时间验证（1-999分钟）
        if (pauseDurationPref != null) {
            pauseDurationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String value = newValue.toString().trim();
                try {
                    int minutes = Integer.parseInt(value);
                    if (minutes < 1 || minutes > 999) {
                        Toast.makeText(requireContext(), "请输入1-999之间的数字", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    // 更新summary显示
                    pauseDurationPref.setSummary("当前设置：" + minutes + " 分钟");
                    return true;
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        // 弹窗阈值验证（1-9999秒）
        if (popupThresholdPref != null) {
            popupThresholdPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String value = newValue.toString().trim();
                try {
                    int seconds = Integer.parseInt(value);
                    if (seconds < 1 || seconds > 9999) {
                        Toast.makeText(requireContext(), "请输入1-9999之间的数字", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    // 更新summary显示
                    popupThresholdPref.setSummary("当前设置：" + seconds + " 秒");
                    return true;
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionStatus();
        updateEditTextSummaries();
        updatePauseStatus();
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private boolean hasOverlayPermission() {
        return Settings.canDrawOverlays(requireContext());
    }

    // 检查"使用情况访问"权限
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) requireContext()
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // 检查无障碍权限是否已启用
    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (serviceInfo.getResolveInfo().serviceInfo.packageName.equals(requireContext().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // 检查WRITE_SECURE_SETTINGS权限
    private boolean hasWriteSecureSettingsPermission() {
        try {
            return SettingsSecureUtil.getInstance().hasWriteSecureSettingsPermission();
        } catch (Exception e) {
            // 如果SettingsSecureUtil未初始化，返回false
            return false;
        }
    }

    // 更新权限状态显示
    private void updatePermissionStatus() {
        if (overlayPermissionPref != null) {
            boolean hasOverlay = hasOverlayPermission();
            overlayPermissionPref.setSummary(hasOverlay ? "已获取悬浮窗权限" : "点击获取悬浮窗权限");
            overlayPermissionPref.setEnabled(!hasOverlay);
        }

        if (usageStatsPermissionPref != null) {
            boolean hasUsageStats = hasUsageStatsPermission();
            usageStatsPermissionPref.setSummary(hasUsageStats ? "已获取使用情况访问权限" : "点击获取使用情况访问权限");
            usageStatsPermissionPref.setEnabled(!hasUsageStats);
        }

        if (accessibilityPermissionPref != null) {
            boolean hasAccessibility = isAccessibilityServiceEnabled();
            accessibilityPermissionPref.setSummary(hasAccessibility ? "已获取无障碍权限" : "点击获取无障碍权限");
            accessibilityPermissionPref.setEnabled(!hasAccessibility);
        }

        if (backgroundPermissionPref != null) {
            // 后台运行权限无法直接检测，只显示提示信息
            backgroundPermissionPref.setSummary("点击跳转到应用信息页面手动设置");
        }

        if (writeSecureSettingsPref != null) {
            boolean hasWriteSecureSettings = hasWriteSecureSettingsPermission();
            writeSecureSettingsPref.setSummary(hasWriteSecureSettings ? "已获取ADB权限" : "点击查看ADB命令");
            writeSecureSettingsPref.setEnabled(!hasWriteSecureSettings);
        }
    }

    // 请求悬浮窗权限
    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
        startActivity(intent);
    }

    // 请求使用情况访问权限
    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    // 请求无障碍权限
    private void requestAccessibilityPermission() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    // 请求后台运行权限
    private void requestBackgroundPermission() {
        new AlertDialog.Builder(requireContext())
                .setTitle("允许后台运行")
                .setMessage("在应用设置页面将省电策略或耗电管理管理中, 设置为无限制/完全允许后台行为, 防止后台服务被结束")
                .setPositiveButton("确认", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .show();
    }

    // 请求锁定应用权限
    private void requestLockAppPermission() {
        Toast.makeText(requireContext(), "请在最近任务中锁定本应用", Toast.LENGTH_SHORT).show();
    }

    // 显示ADB命令
    private void showAdbCommand() {
        // 检查是否已有权限
        if (hasWriteSecureSettingsPermission()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("ADB权限状态")
                    .setMessage("您已经拥有ADB权限，可以正常使用相关功能。")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        String adbCommand = "adb shell pm grant " + requireContext().getPackageName() +
                " android.permission.WRITE_SECURE_SETTINGS";

        new AlertDialog.Builder(requireContext())
                .setTitle("ADB命令")
                .setMessage("请在电脑上连接手机并执行以下命令：\n\n" + adbCommand + "\n\n执行完成后请重新打开应用以刷新权限状态。")
                .setPositiveButton("复制命令", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                            requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("ADB命令", adbCommand);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "ADB命令已复制到剪贴板", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    // 显示恢复默认值确认对话框
    private void showResetConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("恢复默认设置")
                .setMessage("确定要将所有设置恢复为默认值吗？此操作不可撤销。")
                .setPositiveButton("确定", (dialog, which) -> resetToDefault())
                .setNegativeButton("取消", null)
                .show();
    }

    // 恢复默认设置
    private void resetToDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = prefs.edit();

        // 清除所有设置，这样会回到默认值
        editor.clear();
        editor.apply();

        // 重新加载preferences
        getPreferenceScreen().removeAll();
        onCreatePreferences(null, null);

        Toast.makeText(requireContext(), "设置已恢复为默认值", Toast.LENGTH_SHORT).show();
    }

    // 更新EditTextPreference的summary显示
    private void updateEditTextSummaries() {
        // 更新暂停时间设置的summary
        if (pauseDurationPref != null) {
            String value = pauseDurationPref.getText();
            if (value == null || value.isEmpty()) {
                value = "15";  // 默认值
            }
            pauseDurationPref.setSummary("当前设置：" + value + " 分钟");
        }

        // 更新弹窗阈值设置的summary
        if (popupThresholdPref != null) {
            String value = popupThresholdPref.getText();
            if (value == null || value.isEmpty()) {
                value = "60";  // 默认值
            }
            popupThresholdPref.setSummary("当前设置：" + value + " 秒");
        }
    }

    // 更新暂停模式状态显示
    private void updatePauseStatus() {
        if (pauseModeStatusPref != null) {
            SettingsManager settingsManager = new SettingsManager(requireContext());
            boolean isPaused = settingsManager.getPauseEnabled();
            
            if (isPaused) {
                long timestamp = settingsManager.getPauseUntilTimestamp();
                if (timestamp == -1) {
                    // 手动暂停，直到重新开启
                    pauseModeStatusPref.setTitle("应用状态：已暂停");
                    pauseModeStatusPref.setSummary("功能已手动暂停，需要重新开启");
                } else {
                    // 定时暂停
                    long currentTime = System.currentTimeMillis();
                    long remainingTime = timestamp - currentTime;
                    if (remainingTime > 0) {
                        long minutes = remainingTime / (60 * 1000);
                        long seconds = (remainingTime % (60 * 1000)) / 1000;
                        pauseModeStatusPref.setTitle("应用状态：已暂停");
                        if (minutes > 0) {
                            pauseModeStatusPref.setSummary(String.format("功能已暂停，剩余 %d 分钟 %d 秒", minutes, seconds));
                        } else {
                            pauseModeStatusPref.setSummary(String.format("功能已暂停，剩余 %d 秒", seconds));
                        }
                    } else {
                        // 时间已过期，应该已经自动恢复
                        pauseModeStatusPref.setTitle("应用状态");
                        pauseModeStatusPref.setSummary("功能正常运行");
                    }
                }
            } else {
                pauseModeStatusPref.setTitle("应用状态");
                pauseModeStatusPref.setSummary("功能正常运行");
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        // 如果暂停相关的设置改变，更新状态显示
        if ("pause_until_timestamp".equals(key)) {
            updatePauseStatus();
        }
    }

    /**
     * 打开反馈网站
     */
    private void openFeedbackWebsite() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://app.seeother.me/problems.html"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "无法打开浏览器", Toast.LENGTH_SHORT).show();
        }
    }
}
