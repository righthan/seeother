package com.seeother.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.seeother.manager.AppGuardManager;
import com.seeother.manager.SettingsManager;
import com.seeother.utils.GlobalToast;
import com.seeother.utils.SettingsSecureUtil;

import java.lang.ref.WeakReference;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";

    private static WeakReference<MyAccessibilityService> instance;
    private static String foregroundPackage;
    private static String foregroundActivity;
    private long lastVolumeDownClickTime = 0;
    private long lastVolumeUpClickTime = 0;

    // 应用守卫管理器
    private AppGuardManager appGuardManager;
    private SettingsManager settingsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = new WeakReference<>(this);
        appGuardManager = new AppGuardManager(this);
        settingsManager = new SettingsManager(this);

        // 确保SettingsSecureUtil已初始化（防止在无障碍服务启动时未初始化的边缘情况）
        try {
            SettingsSecureUtil.getInstance();
        } catch (IllegalStateException e) {
            // 如果未初始化，则在此处初始化
            SettingsSecureUtil.init(this);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;

        info.eventTypes |= AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_VIEW_SCROLLED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                | AccessibilityEvent.TYPE_WINDOWS_CHANGED;

        info.feedbackType |= AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    public static MyAccessibilityService getInstance() {
        return instance != null ? instance.get() : null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 2048: TYPE_WINDOW_CONTENT_CHANGED, 4096: TYPE_VIEW_SCROLLED
//        Log.d(TAG, "收到事件: " + event.getEventType());
//        Log.d(TAG, "包名: " + event.getPackageName());
//        Log.d(TAG, "类名: " + event.getClassName());

        String appName = (event.getPackageName() != null) ? event.getPackageName().toString() : null;
        String activityClassName = (event.getClassName() != null) ? event.getClassName().toString() : null;
        if (appName == null || activityClassName == null) return;
        // 当前应用不发送广播, 否则可能导致应用跳转成功之后悬浮窗不被移除
        if (appName.contains("systemui") || appName.contains("misound")
                || appName.equals("com.seeother") || appName.contains("securitycenter"))
            return;
        // 不响应输入法
        if (activityClassName.contains("input")) return;

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 判断是真的是桌面, 否则打开应用时，可能导致收到桌面的广播, 突然退出灰度模式
            if (appName.equals("com.miui.home")) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId("com.miui.home:id/drag_layer_background");
                    if (nodes != null && !nodes.isEmpty()) {
                        Log.d(TAG, "找到目标元素");
                        for (AccessibilityNodeInfo node : nodes) {
                            node.recycle();
                        }
                    } else {
                        return;
                    }
                }
            }
            // coloros桌面
            if (appName.equals("com.android.launcher")) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    List<AccessibilityNodeInfo> nodes = rootNode
                            .findAccessibilityNodeInfosByViewId("com.android.launcher:id/page_indicator");
                    if (nodes != null && !nodes.isEmpty()) {
                        Log.d(TAG, "找到目标元素");
                        for (AccessibilityNodeInfo node : nodes) {
                            node.recycle();
                        }
                    } else {
                        return;
                    }
                }
            }
            foregroundPackage = appName;
            foregroundActivity = activityClassName;
            // 检查服务是否运行，如果没有运行则启动
            if (!isServiceRunning()) {
                Intent serviceIntent = new Intent(this, UsageMonitorService.class);
                startService(serviceIntent);
            }
            // 发送广播
            Intent intent = new Intent("com.seeother.action.WINDOW_STATE_CHANGED");
            intent.putExtra("packageName", appName);
            intent.putExtra("activityName", activityClassName);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        // 使用应用守卫管理器处理事件
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            String currentPackage = (event.getPackageName() != null) ? event.getPackageName().toString() : null;

//            printTextNodesAndIds(event);
            
            // 首先检查是否在勿扰时段内
            boolean shouldSkipProcessing = false;
            try {
                shouldSkipProcessing = SettingsSecureUtil.getInstance().isInDoNotDisturbTime();
            } catch (IllegalStateException e) {
                Log.e(TAG, "SettingsSecureUtil 未初始化，无法检查勿扰模式", e);
                // 如果SettingsSecureUtil未初始化，继续正常处理
                shouldSkipProcessing = false;
            }
            
            // 如果不在勿扰时段内，才检查是否应该处理此事件
            if (!shouldSkipProcessing && appGuardManager.shouldProcessEvent(event.getEventType(), currentPackage, foregroundActivity)) {
                // 检查服务是否运行，如果没有运行则启动
                if (!isServiceRunning()) {
                    Intent serviceIntent = new Intent(this, UsageMonitorService.class);
                    startService(serviceIntent);
                }

                // 获取根节点并处理事件
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    try {
                        appGuardManager.processEvent(event.getEventType(), currentPackage, foregroundActivity, rootNode);
                    } finally {
                        rootNode.recycle();
                    }
                }
            }
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // Log.d("AccessibilityService", "收到按键事件: " + event.getKeyCode() + ";类型:" + event.getAction());

        // 读取设置
        long clickTime = System.currentTimeMillis();
        long doubleClickInterval = settingsManager.getVolumeClickInterval();
        // 同时按下的判断时间间隔（毫秒）
        long simultaneousPressInterval = 500;

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                long _lastVolumeDownClickTime = lastVolumeDownClickTime;
                lastVolumeDownClickTime = clickTime;
                // 检查是否同时按下音量加减键
                if (clickTime - lastVolumeUpClickTime < simultaneousPressInterval
                        && settingsManager.isVolumeBothPauseTimeEnabled()) {
                    // 同时按下音量加减键的处理逻辑
                    handleSimultaneousVolumeKeys();
                    return true;
                }

                // 检查是否启用了双击音量下键功能
                if (!settingsManager.isVolumeDownQuickRestoreEnabled()) {
                    return super.onKeyEvent(event);
                }

                if (clickTime - _lastVolumeDownClickTime < doubleClickInterval) {
                    // 关闭颜色空间
                    removeAllEffects();
                    // 显示简单提示
                    GlobalToast.showShort(
                            getApplicationContext(),
                            "已短暂停止功能"
                    );

                    return true;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                // 检查长按音量下键功能
                if (settingsManager.isVolumeDownLongPressEnabled()) {
                    long pressDuration = clickTime - lastVolumeDownClickTime;
                    // 长按时间超过1秒, 使用>而不是>=, 因为双击音量键的最大判定间隔是1000ms
                    if (pressDuration > 1000) {
                        handleVolumeDownLongPress();
                        return true;
                    }
                }
            }
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                // 检查是否同时按下音量加减键
                if (clickTime - lastVolumeDownClickTime < simultaneousPressInterval
                        && settingsManager.isVolumeBothPauseTimeEnabled()) {
                    // 同时按下音量加减键的处理逻辑
                    handleSimultaneousVolumeKeys();
                    return true;
                }

                lastVolumeUpClickTime = clickTime;
            }
        }

        return super.onKeyEvent(event);
    }

    /**
     * 发送广播消除影响
     */
    private void removeAllEffects() {
        try {
            SettingsSecureUtil.getInstance().disableColorSpace();
            SettingsSecureUtil.getInstance().disableHighContrastText();
        } catch (IllegalStateException e) {
            Log.e(TAG, "SettingsSecureUtil 未初始化", e);
        }

        // 检查服务是否运行，如果没有运行则启动
        if (!isServiceRunning()) {
            Intent serviceIntent = new Intent(this, UsageMonitorService.class);
            startService(serviceIntent);
        }

        // 发送广播通知UsageMonitorService暂停悬浮窗
        Intent intent = new Intent("com.seeother.action.DISABLE_FLOATING_WINDOW");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    /**
     * 处理同时按下音量加减键的逻辑
     */
    private void handleSimultaneousVolumeKeys() {
        // 执行与双击音量下键相同的操作, 立刻停止当前的影响
        removeAllEffects();

        // 如果设置了暂停时间, 则清除
        if (settingsManager.getPauseEnabled()) {
            settingsManager.clearPause();
            GlobalToast.showShort(
                    getApplicationContext(),
                    "已恢复功能"
            );
            return;
        }

        // 暂停指定时间
        int pauseMinutes = settingsManager.getPauseDurationMinutes();
        long pauseUntilTime = System.currentTimeMillis() + (pauseMinutes * 60 * 1000L);
        settingsManager.setPauseUntilTimestamp(pauseUntilTime);

        GlobalToast.showShort(
                getApplicationContext(),
                String.format("已暂停功能 %d 分钟", pauseMinutes)
        );
    }

    /**
     * 处理长按音量下键的逻辑
     */
    private void handleVolumeDownLongPress() {
        // 执行与双击音量下键相同的操作, 立刻停止当前的影响
        removeAllEffects();

        // 如果设置了暂停时间, 则清除
        if (settingsManager.getPauseEnabled()) {
            settingsManager.clearPause();
            GlobalToast.showShort(
                    getApplicationContext(),
                    "已恢复功能"
            );
            return;
        }

        settingsManager.setPauseUntilTimestamp(-1);

        GlobalToast.showShort(
                getApplicationContext(),
                "已暂停功能直到手动开启"
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清空状态
        if (instance != null) {
            instance.clear();
            instance = null;
        }
        foregroundPackage = null;
        foregroundActivity = null;

        // 释放应用守卫管理器资源
        if (appGuardManager != null) {
            appGuardManager.destroy();
            appGuardManager = null;
        }

        // 发送广播通知其他组件无障碍服务, 清空包名和活动名称信息
        Intent intent = new Intent("com.seeother.action.WINDOW_STATE_CHANGED");
        intent.putExtra("packageName", foregroundPackage);
        intent.putExtra("activityName", foregroundActivity);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

//        Log.d("MyAccessibilityService", "无障碍服务已关闭");
        // 没有无障碍权限, 关闭颜色空间(因为无法判断当前的应用)
        try {
            SettingsSecureUtil.getInstance().disableColorSpace();  // 关闭颜色空间
            SettingsSecureUtil.getInstance().disableHighContrastText();
        } catch (IllegalStateException e) {
            Log.e(TAG, "SettingsSecureUtil 未初始化，无法在服务销毁时移除视觉效果", e);
        }
    }

    @Override
    public void onInterrupt() {
        // 无障碍服务被中断的处理逻辑(不知为何, 不会触发, 只能在UsageMonitorService中通过getForegroundAppPackage和getForegroundActivity判断
        foregroundPackage = null;
        foregroundActivity = null;
    }

    // 修改检查服务是否运行的方法
    private boolean isServiceRunning() {
        return UsageMonitorService.isRunning();
    }

    /**
     * 递归打印节点及其子节点的文本和ID
     *
     * @param node  当前节点
     * @param depth 当前节点深度
     */
    private void printTextNodesRecursive(AccessibilityNodeInfo node, int depth) {
//        if(depth>20) return;
        if (node == null)
            return;

        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            prefix.append("  ");
        }

        // 获取节点文本
        CharSequence text = node.getText();
        CharSequence contentDesc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        CharSequence className = node.getClassName();

        // 如果节点包含文本或内容描述，打印出来
        if ((text != null && !text.toString().isEmpty()) ||
                (contentDesc != null && !contentDesc.toString().isEmpty())) {
            StringBuilder info = new StringBuilder();
            info.append(prefix).append("节点: ").append(className);

            if (text != null && !text.toString().isEmpty()) {
                info.append(", 文本: ").append(text);
            }

            if (contentDesc != null && !contentDesc.toString().isEmpty()) {
                info.append(", 内容描述: ").append(contentDesc);
            }

            if (viewId != null) {
                info.append(", ID: ").append(viewId);
            }

            Log.d("TextNodes", info.toString());
        }

        // 递归处理子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                printTextNodesRecursive(childNode, depth + 1);
                childNode.recycle(); // 回收子节点
            }
        }
    }

    /**
     * 打印包含文本内容的节点及其对应ID
     *
     * @param event 可访问性事件
     */
    private void printTextNodesAndIds(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null)
            return;

        try {
            Log.d("TextNodes", "开始打印文本节点和ID信息 -----");
            printTextNodesRecursive(rootNode, 0);
            Log.d("TextNodes", "文本节点和ID信息打印结束 -----");
        } finally {
            rootNode.recycle();
        }
    }
}
