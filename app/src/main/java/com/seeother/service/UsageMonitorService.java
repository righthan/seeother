package com.seeother.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.seeother.MainActivity;
import com.seeother.R;
import com.seeother.data.db.MonitoredAppDao;
import com.seeother.data.db.RecommendAppDao;
import com.seeother.data.entity.MonitoredApp;
import com.seeother.data.entity.RecommendApp;
import com.seeother.manager.RecommendLinkManager;
import com.seeother.manager.SettingsManager;
import com.seeother.utils.SettingsSecureUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class UsageMonitorService extends Service {
    private static final String TAG = "UsageMonitorService";
    private static boolean isServiceRunning = false; // 添加静态标志位
    private MonitoredApp monitoredApp;
    private Handler handler;
    private WindowManager windowManager;
    private View floatingView;
    private String currentPackage = "";
    private MonitoredAppDao monitoredAppDao;
    private RecommendAppDao recommendAppDao;
    private SettingsManager settingsManager;
    private RecommendLinkManager linkManager;

    // 广播接收器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.seeother.action.WINDOW_STATE_CHANGED".equals(action)) {
                String packageName = intent.getStringExtra("packageName");
                String activityName = intent.getStringExtra("activityName");
                checkCurrentApp(packageName, activityName);
            } else if ("com.seeother.action.SHOW_FLOATING_WINDOW".equals(action)) {
                if (checkPauseTime()) return;
                showFloatingWindow();
            } else if ("com.seeother.action.DISABLE_FLOATING_WINDOW".equals(action)) {
                // 如果当前有悬浮窗，立即移除
                removeFloatingView();
                disableGrayMode();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        monitoredAppDao = new MonitoredAppDao(this);
        recommendAppDao = new RecommendAppDao(this);

        isServiceRunning = true; // 设置服务运行状态

        // 确保SettingsSecureUtil已初始化（防止在BootReceiver启动时未初始化的边缘情况）
        try {
            SettingsSecureUtil.getInstance();
        } catch (IllegalStateException e) {
            // 如果未初始化，则在此处初始化
            SettingsSecureUtil.init(this);
        }

        settingsManager = new SettingsManager(this);
        linkManager = RecommendLinkManager.getInstance(this);
        // 注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.seeother.action.WINDOW_STATE_CHANGED");
        filter.addAction("com.seeother.action.SHOW_FLOATING_WINDOW"); // 添加新的 action
        filter.addAction("com.seeother.action.DISABLE_FLOATING_WINDOW"); // 添加禁用悬浮窗的广播
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void removeFloatingView() {
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }

    private void openRecommendedApp() {
        List<RecommendApp> recommendApps = recommendAppDao.getAllApps();
        if (recommendApps.isEmpty()) return;

        // 计算总权重
        int totalWeight = 0;
        for (RecommendApp app : recommendApps) {
            totalWeight += app.getWeight();
        }

        // 生成一个随机值，范围是[0, totalWeight)
        int randomWeight = new Random().nextInt(totalWeight);

        // 遍历累加权重，当随机值小于累加和时，返回当前应用
        int currentWeight = 0;
        for (RecommendApp app : recommendApps) {
            currentWeight += app.getWeight();
            if (randomWeight < currentWeight) {
                String recommendedPackage = app.getPkgName();
                // size>1修复只有一个应用时, 陷入无限递归
                if (recommendedPackage != null && recommendedPackage.equals(currentPackage) && recommendApps.size() > 1) {
                    openRecommendedApp(); // 递归调用重新选择
                    return;
                }
                // 其他应用正常打开
                Intent intent = getPackageManager().getLaunchIntentForPackage(app.getPkgName());
                if (intent != null) {
                    startActivity(intent);
                    break;
                } else {
                    Log.e(TAG, "推荐应用无法打开: " + app.getPkgName());
                    // 创建对话框提示用户
                    new Handler(Looper.getMainLooper()).post(() -> {
                        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents))
                                .setTitle("应用无法打开")
                                .setMessage("应用 " + app.getPkgName() + " 可能已被卸载或不支持，是否从推荐列表中移除？")
                                .setPositiveButton("确定", (dialog1, which) -> {
                                    recommendAppDao.deleteByPkgName(app.getPkgName());
                                    removeFloatingView();
                                })
                                .setNegativeButton("取消", null)
                                .create();
                        if (dialog.getWindow() != null) {
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                            dialog.show();
                        }
                    });
                }
            }
        }
    }

    private void openRecommendedLink() {
        String randomLink = linkManager.getRandomLink();
        if (randomLink != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(randomLink));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.d(TAG, "打开推荐链接: " + randomLink);

            } catch (Exception e) {
                Log.e(TAG, "打开推荐链接失败: " + randomLink, e);
                // 如果打开链接失败，回退到打开推荐应用
                openRecommendedApp();
            }
        } else {
            Log.w(TAG, "没有可用的推荐链接，回退到打开推荐应用");
            // 如果没有链接，回退到打开推荐应用
            openRecommendedApp();
        }
    }

    private void checkCurrentApp(String packageName, String activityName) {
        if (activityName == null) return;

        if (!packageName.equals(currentPackage)) {
            currentPackage = packageName;
            monitoredApp = monitoredAppDao.getAppByPkgName(packageName);

            // 检查是否需要启用灰度模式，包含多种情况：
            // 1. 监控应用设置了启用灰度模式
            // 2. 非推荐应用灰度模式开关启用且符合条件
            boolean shouldEnableGrayMode = false;

            try {
                // 检查是否在勿扰时段内
                boolean isInDoNotDisturbTime = SettingsSecureUtil.getInstance().isInDoNotDisturbTime();

                if (!isInDoNotDisturbTime) {
                    // 情况1：监控应用设置了启用灰度模式
                    if (monitoredApp != null && monitoredApp.getEnableGrayMode()) {
                        shouldEnableGrayMode = true;
                    }
                    // 情况2：非推荐应用灰度模式
                    else if (shouldEnableGrayModeForNonRecommendApp(packageName)) {
                        shouldEnableGrayMode = true;
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "SettingsSecureUtil 未初始化，无法检查勿扰模式", e);
                // 如果无法检查勿扰模式，只检查监控应用的设置
                if (monitoredApp != null && monitoredApp.getEnableGrayMode()) {
                    shouldEnableGrayMode = true;
                }
            }

            if (shouldEnableGrayMode) {
                enableGrayMode();
            } else {
                disableGrayMode();
            }
        }
    }

    private void showFloatingWindow() {
        if (floatingView != null) return;
        if (settingsManager.getPauseEnabled()) return;

        // 获取当前是否为夜间模式
        int nightModeFlags = getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        // 使用适合当前模式的主题
        Context contextThemeWrapper;
        if (isNightMode) {
            // 夜间模式使用DarkActionBar主题
            contextThemeWrapper = new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_DarkActionBar);
        } else {
            // 日间模式使用Light主题
            contextThemeWrapper = new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents_Light);
        }

        floatingView = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.floating_window_fullscreen, null);

        // 设置全屏悬浮窗的动画和按钮
        LottieAnimationView mainAnimation = floatingView.findViewById(R.id.animation_view);
        LottieAnimationView loadingAnimation = floatingView.findViewById(R.id.loading_animation);
        MaterialButton tryLuckButton = floatingView.findViewById(R.id.btn_try_luck);

        // 加载动画
        try {
            mainAnimation.setAnimation("tired.lottie");
            loadingAnimation.setAnimation("loading.lottie");
        } catch (Exception e) {
            Log.e(TAG, "加载Lottie动画失败", e);
        }

        // 设置按钮点击事件
        tryLuckButton.setOnClickListener(v -> {
            // 根据概率决定打开链接还是推荐应用
            if (linkManager.shouldOpenLink()) {
                openRecommendedLink();
            } else {
                openRecommendedApp();
            }

            // 延迟移除, 防止看到吸引人的内容
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeFloatingView();
                }
            }, 100);
        });

        // 设置关闭按钮点击事件
        MaterialButton closeButton = floatingView.findViewById(R.id.btn_close_page);
        closeButton.setOnClickListener(v -> {
            removeFloatingView();
        });
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(floatingView, params);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (floatingView == null) return;
                    View target = floatingView.findViewById(R.id.btn_try_luck);
                    if (target != null) {
                        target.performClick();
                    }
                }
            }, 0);
        } catch (Exception e) {
            Log.e(TAG, "Could not show floating window", e);
        }
    }

    private void enableGrayMode() {
        if (settingsManager.getPauseEnabled()) return;
        try {
            SettingsSecureUtil.getInstance().enableColorSpace();
            if (monitoredApp != null && monitoredApp.getEnableHighContrast()) {
                SettingsSecureUtil.getInstance().enableHighContrastText();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "SettingsSecureUtil 未初始化，无法启用灰度模式", e);
            // 尝试在此处初始化
            SettingsSecureUtil.init(this);
        }
    }

    private void disableGrayMode() {
        try {
            SettingsSecureUtil.getInstance().disableColorSpace();
            SettingsSecureUtil.getInstance().disableHighContrastText();
        } catch (IllegalStateException e) {
            Log.e(TAG, "SettingsSecureUtil 未初始化，无法禁用灰度模式", e);
            // 尝试在此处初始化
            SettingsSecureUtil.init(this);
        }
    }

    private boolean checkPauseTime() {
        long pauseUntil = settingsManager.getPauseUntilTimestamp();
        long currentTime = System.currentTimeMillis();
        if (pauseUntil > 0 && currentTime >= pauseUntil) {
            settingsManager.clearPause();
            // GlobalToast.showShort(this, "暂停时间结束, 将恢复功能");
            return true;
        }
        // 如果currentTIme<pauseUntil, 设置了暂停时间返回false
        // 没设置暂停时间(pauseUntil是0)返回true
        return pauseUntil > 0;
    }

    /**
     * 检查是否应该为非推荐应用启用灰度模式
     * 条件：1. 开关已启用 2. 非勿扰时段 3. 当前应用不是推荐应用
     *
     * @param packageName 当前应用包名
     * @return true表示应该启用灰度模式
     */
    private boolean shouldEnableGrayModeForNonRecommendApp(String packageName) {
        // 检查开关是否启用
        if (!settingsManager.isGrayModeForNonRecommendAppsEnabled()) {
            return false;
        }

        // 检查是否在勿扰时段
        try {
            if (SettingsSecureUtil.getInstance().isInDoNotDisturbTime()) {
                return false;
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "SettingsSecureUtil 未初始化，无法检查勿扰模式", e);
            return false;
        }

        // 检查当前应用是否为推荐应用
        RecommendApp recommendApp = recommendAppDao.getAppByPkgName(packageName);
        return recommendApp == null; // 如果不是推荐应用，返回true
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        removeFloatingView();
        // 注销广播
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        isServiceRunning = false;
    }

    // 添加静态方法获取服务状态
    public static boolean isRunning() {
        return isServiceRunning;
    }
}
