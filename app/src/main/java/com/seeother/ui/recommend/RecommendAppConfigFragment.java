package com.seeother.ui.recommend;

import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.seeother.data.db.RecommendAppDao;
import com.seeother.data.entity.RecommendApp;
import com.seeother.databinding.FragmentRecommendAppConfigBinding;

public class RecommendAppConfigFragment extends Fragment {
    private static final String ARG_PKG_NAME = "pkgName";
    private FragmentRecommendAppConfigBinding binding;
    private RecommendAppDao recommendAppDao;
    private RecommendApp recommendApp;
    private RecommendDataManager dataManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recommendAppDao = new RecommendAppDao(this.getContext());
        dataManager = RecommendDataManager.getInstance();
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

            recommendApp = recommendAppDao.getAppByPkgName(pkgName);
            recommendApp.setAppName(pm.getApplicationLabel(appInfo).toString());
            recommendApp.setAppIcon(pm.getApplicationIcon(pkgName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecommendAppConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置应用信息
        binding.ivAppIcon.setImageDrawable(recommendApp.getAppIcon());
        binding.tvAppName.setText(recommendApp.getAppName());

        // 初始化推荐权重
        binding.etRecommendWeight.setText(String.valueOf(recommendApp.getWeight()));
        binding.etRecommendWeight.addTextChangedListener(new TextWatcher() {
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
                        int weight = Integer.parseInt(s.toString());
                        recommendApp.setWeight(weight);
                        saveConfig();
                    } catch (NumberFormatException e) {
                        // 忽略无效输入
                    }
                }
            }
        });

        // 添加删除按钮的点击事件
        binding.btnDeleteApp.setOnClickListener(v -> {
            showDeleteAppConfirmDialog();
        });
    }


    private void saveConfig() {
        // 保存到数据库
        new Thread(() -> {
            int result = recommendAppDao.update(recommendApp);
            if (result > 0) {
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
     * 删除应用及其相关数据
     */
    private void deleteApp() {
        new Thread(() -> {
            // 删除应用本身
            long appId = recommendApp.getId();
            int result = recommendAppDao.delete(appId);

            requireActivity().runOnUiThread(() -> {
                if (result > 0) {
                    Toast.makeText(requireContext(), "应用已删除", Toast.LENGTH_SHORT).show();
                    // 通知数据发生变化
                    dataManager.notifyDataChanged();
                    // 返回上一页
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
