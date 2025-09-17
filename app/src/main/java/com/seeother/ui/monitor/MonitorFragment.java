package com.seeother.ui.monitor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.seeother.R;
import com.seeother.common.AppInfo;
import com.seeother.data.db.MonitoredAppDao;
import com.seeother.data.entity.MonitoredApp;
import com.seeother.databinding.FragmentMonitorBinding;

import java.util.ArrayList;
import java.util.List;

public class MonitorFragment extends Fragment {

    private FragmentMonitorBinding binding;
    private MonitorViewModel monitorViewModel;
    private MonitoredAppDao monitoredAppDao;
    private MonitoredAppListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        monitorViewModel = new ViewModelProvider(this).get(MonitorViewModel.class);

        binding = FragmentMonitorBinding.inflate(inflater, container, false);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.fabAdd.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_add_app)
        );

        View root = binding.getRoot();

        monitoredAppDao = new MonitoredAppDao(this.getContext());

        setupRecyclerView();
        observeData();

        monitorViewModel.loadInstalledApps(requireContext());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new MonitoredAppListAdapter(this.getContext());
        binding.rvApps.setAdapter(adapter);
    }

    private void observeData() {
        monitorViewModel.getAppList().observe(getViewLifecycleOwner(), apps -> {
            binding.progressBar.setVisibility(View.GONE);
            List<MonitoredApp> monitoredAppList = new ArrayList<>();
            for (AppInfo app : apps) {
                MonitoredApp monitoredApp = monitoredAppDao.getAppByPkgName(app.getPackageName());
                if(monitoredApp!=null){
                    app.setChecked(true);
                    monitoredApp.setAppIcon(app.getAppIcon());
                    monitoredApp.setAppName(app.getAppName());
                    monitoredAppList.add(monitoredApp);
                }
            }
            adapter.setAppList(monitoredAppList);

            // 显示或隐藏空状态视图
            if (monitoredAppList.isEmpty()) {
                binding.ivEmpty.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvApps.setVisibility(View.GONE);
            } else {
                binding.ivEmpty.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvApps.setVisibility(View.VISIBLE);
            }
        });
    }
}