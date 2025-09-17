package com.seeother;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.seeother.databinding.ActivityMainBinding;
import com.seeother.manager.AppGuardManager;
import com.seeother.manager.SettingsManager;
import com.seeother.service.UsageMonitorService;
import com.seeother.utils.SettingsSecureUtil;
import com.seeother.utils.PermissionChecker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // 定义顶层目的地ID集合，只有这些页面显示底部导航栏
    private final Set<Integer> TOP_LEVEL_DESTINATIONS = new HashSet<>(Arrays.asList(
            R.id.navigation_home,
            R.id.navigation_recommend,
            R.id.navigation_settings
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 应用从最近任务中隐藏
        applyHideFromRecentSetting();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_recommend, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 添加导航监听器，根据目的地ID控制底部导航栏的显示和隐藏
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (TOP_LEVEL_DESTINATIONS.contains(destination.getId())) {
                // 如果是顶层目的地，显示底部导航栏
                navView.setVisibility(View.VISIBLE);
            } else {
                // 如果不是顶层目的地，隐藏底部导航栏
                navView.setVisibility(View.GONE);
            }
        });

        // 初始化SettingsSecureUtil单例
        SettingsSecureUtil.init(this);

        // 尝试通过ADB权限设置无障碍权限
        SettingsSecureUtil.getInstance().enableAccessibilityService();

        // 初始化应用守卫管理器并执行初始化守卫规则
        AppGuardManager appGuardManager = new AppGuardManager(this);
        appGuardManager.initializeGuardRules();

        // 检查运行权限并跳转到设置页面
        checkPermissionsAndNavigate(navController);

        startService(new Intent(this, UsageMonitorService.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * 检查运行权限并跳转到设置页面
     */
    private void checkPermissionsAndNavigate(NavController navController) {
        boolean needsPermission = false;
        StringBuilder missingPermissions = new StringBuilder();

        // 检查悬浮窗权限
        if (!PermissionChecker.hasOverlayPermission(this)) {
            needsPermission = true;
            missingPermissions.append("悬浮窗权限、");
        }

        // 检查无障碍权限
        if (!PermissionChecker.isAccessibilityServiceEnabled(this)) {
            needsPermission = true;
            missingPermissions.append("无障碍权限、");
        }

        // 如果需要权限，跳转到设置页面
        if (needsPermission) {
            // 移除末尾的"、"
            if (missingPermissions.length() > 0) {
                missingPermissions.setLength(missingPermissions.length() - 1);
            }
            
            // 延迟跳转，确保界面已经初始化完成
            new android.os.Handler().postDelayed(() -> {
                navController.navigate(R.id.navigation_settings);
                
                // 显示提示信息
                android.widget.Toast.makeText(this, 
                    "请先开启以下权限：" + missingPermissions.toString(), 
                    android.widget.Toast.LENGTH_LONG).show();
            }, 500);
        }
    }

    /**
     * 应用从最近任务中隐藏 - 使用延迟任务
     */
    private void applyHideFromRecentSetting() {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(() -> {
            SettingsManager settingsManager = new SettingsManager(this);
            boolean hideFromRecent = settingsManager.isHideFromRecent();

            ActivityManager systemService = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
            int size = appTasks.size();
            if (size > 0) {
                appTasks.get(0).setExcludeFromRecents(hideFromRecent);
            }
        }, 1, TimeUnit.SECONDS); // 一秒后执行该任务
    }
}