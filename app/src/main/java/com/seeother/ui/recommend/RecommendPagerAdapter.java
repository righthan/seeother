package com.seeother.ui.recommend;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RecommendPagerAdapter extends FragmentStateAdapter {
    
    private RecommendAppsFragment recommendAppsFragment;
    private RecommendLinksFragment recommendLinksFragment;

    public RecommendPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                if (recommendAppsFragment == null) {
                    recommendAppsFragment = new RecommendAppsFragment();
                }
                return recommendAppsFragment; // 推荐应用页面
            case 1:
                if (recommendLinksFragment == null) {
                    recommendLinksFragment = new RecommendLinksFragment();
                }
                return recommendLinksFragment; // 推荐链接页面
            default:
                if (recommendAppsFragment == null) {
                    recommendAppsFragment = new RecommendAppsFragment();
                }
                return recommendAppsFragment;
        }
    }
    
    /**
     * 获取推荐应用Fragment的实例
     */
    public RecommendAppsFragment getRecommendAppsFragment() {
        return recommendAppsFragment;
    }

    @Override
    public int getItemCount() {
        return 2; // 两个Tab页
    }
} 