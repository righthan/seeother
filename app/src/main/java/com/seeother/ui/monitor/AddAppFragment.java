package com.seeother.ui.monitor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seeother.R;
import com.seeother.common.AppInfo;
import com.seeother.data.db.MonitoredAppDao;
import com.seeother.data.db.RecommendAppDao;
import com.seeother.data.entity.MonitoredApp;
import com.seeother.data.entity.RecommendApp;
import com.seeother.databinding.FragmentMonitorAddAppBinding;

import java.util.ArrayList;
import java.util.List;

public class AddAppFragment extends Fragment {

    private FragmentMonitorAddAppBinding binding;
    private AddAppViewModel viewModel;
    private AppListAdapter adapter;
    private MonitoredAppDao monitoredAppDao;
    private RecommendAppDao recommendAppDao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMonitorAddAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddAppViewModel.class);
        monitoredAppDao = new MonitoredAppDao(this.getContext());
        recommendAppDao = new RecommendAppDao(this.getContext());

        // 隐藏底部导航栏
        View navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupSearchView();
        observeData();

        // 加载应用列表
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadInstalledApps(requireContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // 恢复底部导航栏的可见性
        View navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new AppListAdapter(this.getContext());
        binding.rvApps.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不需要实现
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 不需要实现
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.searchApps(s.toString());
            }
        });
    }

    private void observeData() {
        viewModel.getAppList().observe(getViewLifecycleOwner(), apps -> {
            binding.progressBar.setVisibility(View.GONE);
            List<AppInfo> appList = new ArrayList<>();
            for (AppInfo app : apps) {
                RecommendApp recommendApp = recommendAppDao.getAppByPkgName(app.getPackageName());
                if (recommendApp != null) continue;
                MonitoredApp monitoredApp = monitoredAppDao.getAppByPkgName(app.getPackageName());
                app.setChecked(monitoredApp != null);
                appList.add(app);
            }
            // 将选中的应用排在前面
            appList.sort((app1, app2) -> {
                if (app1.getChecked() && !app2.getChecked()) {
                    return -1; // app1排在前面
                } else if (!app1.getChecked() && app2.getChecked()) {
                    return 1; // app2排在前面
                } else {
                    return 0; // 保持原顺序
                }
            });
            adapter.setAppList(appList);
        });
    }
}
