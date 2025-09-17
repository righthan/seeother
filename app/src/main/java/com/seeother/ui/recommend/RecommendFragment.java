package com.seeother.ui.recommend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.seeother.R;
import com.seeother.databinding.FragmentRecommendBinding;

public class RecommendFragment extends Fragment {

    private FragmentRecommendBinding binding;
    private RecommendPagerAdapter pagerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecommendBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupViewPager();
        setupTabs();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupViewPager() {
        pagerAdapter = new RecommendPagerAdapter(requireActivity());
        binding.viewPager.setAdapter(pagerAdapter);
        
        // 添加页面切换监听器
        binding.viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 当切换到推荐应用页面时，检查是否需要刷新数据
                if (position == 0) {
                    RecommendDataManager dataManager = RecommendDataManager.getInstance();
                    Boolean dataChanged = dataManager.getDataChanged().getValue();
                    if (dataChanged != null && dataChanged) {
                        // 直接通过适配器获取 RecommendAppsFragment 并刷新
                        RecommendAppsFragment appsFragment = pagerAdapter.getRecommendAppsFragment();
                        if (appsFragment != null) {
                            appsFragment.forceRefreshData();
                        }
                        dataManager.resetDataChanged();
                    }
                }
            }
        });
    }

    private void setupTabs() {
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("推荐应用");
                            break;
                        case 1:
                            tab.setText("推荐链接");
                            break;
                    }
                }
        ).attach();
    }
}