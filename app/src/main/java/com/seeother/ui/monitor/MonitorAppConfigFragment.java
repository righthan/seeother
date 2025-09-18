package com.seeother.ui.monitor;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.seeother.data.db.MonitoredAppDao;
import com.seeother.data.entity.AppGuardRule;
import com.seeother.data.entity.MonitoredApp;
import com.seeother.data.repository.AppGuardRuleRepository;
import com.seeother.databinding.FragmentMonitorAppConfigBinding;
import com.seeother.utils.PermissionChecker;
import com.seeother.R;

import java.util.List;

public class MonitorAppConfigFragment extends Fragment {
    private static final String ARG_PKG_NAME = "pkgName";
    private FragmentMonitorAppConfigBinding binding;
    private MonitoredApp monitoredApp;
    private MonitoredAppDao monitoredAppDao;
    private AppGuardRuleRepository appGuardRuleRepository;
    private List<AppGuardRule> guardRules;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        monitoredAppDao = new MonitoredAppDao(this.getContext());
        appGuardRuleRepository = new AppGuardRuleRepository(this.getContext());
        // 获取传递的参数
        if (getArguments() != null) {
            String pkgName = getArguments().getString(ARG_PKG_NAME);
            loadAppInfo(pkgName);
        }
    }

    private void loadAppInfo(String pkgName) {
        try {
            PackageManager pm = requireContext().getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);

            monitoredApp = monitoredAppDao.getAppByPkgName(pkgName);
            monitoredApp.setAppName(pm.getApplicationLabel(appInfo).toString());
            monitoredApp.setAppIcon(pm.getApplicationIcon(pkgName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMonitorAppConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置应用信息
        binding.ivAppIcon.setImageDrawable(monitoredApp.getAppIcon());
        binding.tvAppName.setText(monitoredApp.getAppName());

        // 设置灰度开关
        binding.switchForceStop.setChecked(monitoredApp.getEnableGrayMode());
        binding.switchForceStop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 用户尝试启用灰度模式，检查ADB权限
                if (!PermissionChecker.hasWriteSecureSettingsPermission()) {
                    // 没有ADB权限，恢复开关状态并显示权限提示
                    buttonView.setChecked(false);
                    showAdbPermissionDialog("灰度模式");
                    return;
                }
            }
            monitoredApp.setEnableGrayMode(isChecked);
            saveConfig();
        });

        // 设置高对比度开关
        binding.switchEnableHighcontrast.setChecked(monitoredApp.getEnableHighContrast());
        binding.switchEnableHighcontrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 用户尝试启用高对比度，检查ADB权限
                if (!PermissionChecker.hasWriteSecureSettingsPermission()) {
                    // 没有ADB权限，恢复开关状态并显示权限提示
                    buttonView.setChecked(false);
                    showAdbPermissionDialog("高对比度");
                    return;
                }
            }
            monitoredApp.setEnableHighContrast(isChecked);
            saveConfig();
        });

        // 设置滑动守卫配置
        setupGuardConfig();

        // 添加删除按钮的点击事件
        binding.btnDeleteApp.setOnClickListener(v -> {
            showDeleteAppConfirmDialog();
        });
    }

    private void setupGuardConfig() {
        // 在后台线程中检查守卫规则
        new Thread(() -> {
            guardRules = appGuardRuleRepository.getRulesForPackageSync(monitoredApp.getPkgName());

            requireActivity().runOnUiThread(() -> {
                if (guardRules != null && !guardRules.isEmpty()) {
                    // 支持滑动守卫，显示配置界面
                    binding.layoutGuardSupported.setVisibility(View.VISIBLE);
                    binding.layoutGuardNotSupported.setVisibility(View.GONE);

                    setupGuardConfigListeners();
                    loadGuardConfig();
                } else {
                    // 不支持滑动守卫，显示提示界面
                    binding.layoutGuardSupported.setVisibility(View.GONE);
                    binding.layoutGuardNotSupported.setVisibility(View.VISIBLE);

                    setupUnsupportedGuardListeners();
                }
            });
        }).start();
    }

    private void setupGuardConfigListeners() {
        // 启用守卫开关
        binding.switchGuardEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 更新所有规则的启用状态
            new Thread(() -> {
                appGuardRuleRepository.setPackageEnabled(monitoredApp.getPkgName(), isChecked);
                requireActivity().runOnUiThread(this::saveConfig);
            }).start();
        });

        // 滑动次数阈值
        binding.etScrollCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        int scrollCount = Integer.parseInt(s.toString());
                        if (scrollCount > 0) {
                            // 更新所有规则的滑动次数
                            new Thread(() -> {
                                appGuardRuleRepository.setPackageScrollCount(monitoredApp.getPkgName(), scrollCount);
                            }).start();
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无效输入
                    }
                }
            }
        });

        // 检查间隔
        binding.etCheckInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        long interval = Long.parseLong(s.toString());
                        if (interval > 0) {
                            // 更新所有规则的广播间隔
                            new Thread(() -> {
                                appGuardRuleRepository.setPackageBroadcastInterval(monitoredApp.getPkgName(), interval);
                            }).start();
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无效输入
                    }
                }
            }
        });

        // 反馈点击事件
        binding.tvGuardFeedback.setOnClickListener(v -> {
            openFeedbackWebsite();
        });
    }

    private void setupUnsupportedGuardListeners() {
        // 反馈适配按钮
        binding.btnFeedbackAdaptation.setOnClickListener(v -> {
            openFeedbackWebsite();
        });
    }

    private void loadGuardConfig() {
        if (guardRules == null || guardRules.isEmpty()) {
            return;
        }

        // 使用第一个规则的配置作为显示值（假设同一包名的规则配置相同）
        AppGuardRule firstRule = guardRules.get(0);

        // 检查是否有启用的规则
        boolean hasEnabledRule = false;
        for (AppGuardRule rule : guardRules) {
            if (rule.isEnabled()) {
                hasEnabledRule = true;
                break;
            }
        }

        // 暂时移除监听器，避免触发Toast
        binding.switchGuardEnabled.setOnCheckedChangeListener(null);
        binding.switchGuardEnabled.setChecked(hasEnabledRule);
        setupGuardConfigListeners();  // 重新设置监听器

        binding.etScrollCount.setText(String.valueOf(firstRule.getScrollCount()));
        binding.etCheckInterval.setText(String.valueOf(firstRule.getBroadcastInterval()));
    }


    private void saveConfig() {
        // 保存到数据库
        new Thread(() -> {
            long id = monitoredAppDao.update(monitoredApp);
            if (id > 0) {
                requireActivity().runOnUiThread(() -> {
                    Snackbar.make(binding.getRoot(), "已保存", Snackbar.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 显示删除应用确认对话框
     */
    private void showDeleteAppConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("删除确认")
                .setMessage("确定要删除这个应用吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    deleteApp();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示ADB权限提示对话框
     */
    private void showAdbPermissionDialog(String featureName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("需要ADB权限")
                .setMessage(featureName + "功能需要ADB权限才能正常工作。\n\n请前往设置页面查看ADB授权命令并执行授权。")
                .setPositiveButton("前往设置", (dialog, which) -> {
                    // 跳转到设置页面
                    Navigation.findNavController(requireView()).navigate(R.id.navigation_settings);
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
    }

    /**
     * 删除应用及其相关数据
     */
    private void deleteApp() {
        new Thread(() -> {
            // 删除应用本身
            long appId = monitoredApp.getId();
            int result = monitoredAppDao.delete(appId);

            requireActivity().runOnUiThread(() -> {
                if (result > 0) {
                    Toast.makeText(requireContext(), "应用已删除", Toast.LENGTH_SHORT).show();
                    // 返回上一页
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (appGuardRuleRepository != null) {
            appGuardRuleRepository.close();
        }
    }
}
