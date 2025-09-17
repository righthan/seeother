package com.seeother.ui.recommend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.seeother.R;
import com.seeother.common.AppInfo;
import com.seeother.data.db.RecommendAppDao;
import com.seeother.data.entity.RecommendApp;

import java.util.ArrayList;
import java.util.List;

public class RecommendAppsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private View ivEmpty;
    private View tvEmpty;
    private View progressBar;
    
    private RecommendViewModel recommendViewModel;
    private RecommendAppDao recommendAppDao;
    private RecommendAppListAdapter adapter;
    private boolean isFirstLoad = true;
    private RecommendDataManager dataManager;
    private boolean isVisibleToUser = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recommendAppDao = new RecommendAppDao(requireContext());
        dataManager = RecommendDataManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend_apps, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupViewModel();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_apps);
        fabAdd = view.findViewById(R.id.fab_add);
        ivEmpty = view.findViewById(R.id.iv_empty);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        adapter = new RecommendAppListAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_add_recommend_app)
        );
    }

    private void setupViewModel() {
        recommendViewModel = new ViewModelProvider(this).get(RecommendViewModel.class);
        observeData();
        observeDataChanges();
        recommendViewModel.loadInstalledApps(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 除了首次加载外，每次页面恢复时都刷新数据，确保从其他页面返回时能看到最新数据
        if (!isFirstLoad) {
            refreshData();
        }
        isFirstLoad = false;
        
        // 检查是否有待处理的数据变化
        checkPendingDataChanges();
    }
    
    /**
     * 检查是否有待处理的数据变化
     */
    private void checkPendingDataChanges() {
        Boolean dataChanged = dataManager.getDataChanged().getValue();
        if (dataChanged != null && dataChanged) {
            refreshData();
            dataManager.resetDataChanged();
        }
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        // 当Fragment对用户可见时刷新数据
        if (isVisibleToUser && isResumed() && !isFirstLoad) {
            refreshData();
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.isVisibleToUser = !hidden;
        // 当Fragment从隐藏状态变为可见时刷新数据
        if (!hidden && !isFirstLoad) {
            refreshData();
        }
    }

    private void refreshData() {
        progressBar.setVisibility(View.VISIBLE);
        recommendViewModel.refreshApps(requireContext());
    }
    
    /**
     * 强制刷新数据（供外部调用）
     */
    public void forceRefreshData() {
        refreshData();
    }

    private void observeData() {
        recommendViewModel.getAppList().observe(getViewLifecycleOwner(), apps -> {
            progressBar.setVisibility(View.GONE);
            List<RecommendApp> recommendedAppList = new ArrayList<>();
            
            for (AppInfo app : apps) {
                RecommendApp recommendApp = recommendAppDao.getAppByPkgName(app.getPackageName());
                if (recommendApp != null) {
                    recommendApp.setAppIcon(app.getAppIcon());
                    recommendApp.setAppName(app.getAppName());
                    recommendedAppList.add(recommendApp);
                }
            }
            
            adapter.setAppList(recommendedAppList);

            // 显示或隐藏空状态视图
            if (recommendedAppList.isEmpty()) {
                ivEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                ivEmpty.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void observeDataChanges() {
        dataManager.getDataChanged().observe(getViewLifecycleOwner(), dataChanged -> {
            if (dataChanged != null && dataChanged && (isVisibleToUser || getUserVisibleHint())) {
                // 数据发生变化且当前Fragment可见时，刷新数据
                refreshData();
                dataManager.resetDataChanged();
            }
        });
    }
} 