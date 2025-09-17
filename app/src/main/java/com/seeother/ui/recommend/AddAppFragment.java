package com.seeother.ui.recommend;

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
import com.seeother.databinding.FragmentRecommendAddAppBinding;

import java.util.ArrayList;
import java.util.List;

public class AddAppFragment extends Fragment {

    private FragmentRecommendAddAppBinding binding;
    private AddAppViewModel viewModel;
    private AddAppListAdapter adapter;
    private MonitoredAppDao monitoredAppDao;
    private RecommendAppDao recommendAppDao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecommendAddAppBinding.inflate(inflater, container, false);
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
        adapter = new AddAppListAdapter(this.getContext());
        binding.rvRecommendApps.setAdapter(adapter);
        
        // 设置应用点击监听器
        adapter.setOnAppClickListener(app -> {
            // 应用状态改变时的回调，这里可以添加额外的处理逻辑
            // 例如显示提示信息等
        });
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
            List<AppInfo> appList = new ArrayList<>();
            binding.progressBar.setVisibility(View.GONE);
            for (AppInfo app : apps) {
                MonitoredApp monitoredApp = monitoredAppDao.getAppByPkgName(app.getPackageName());
                if(monitoredApp!=null) continue;
                RecommendApp recommendApp = recommendAppDao.getAppByPkgName(app.getPackageName());
                if(recommendApp !=null) {
                    app.setChecked(true);
                }
                appList.add(app);
            }
            adapter.setAppList(appList);
        });
    }

}
