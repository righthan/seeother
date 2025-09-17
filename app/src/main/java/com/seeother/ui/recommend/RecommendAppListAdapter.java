package com.seeother.ui.recommend;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.seeother.R;
import com.seeother.data.db.RecommendAppDao;
import com.seeother.data.entity.RecommendApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecommendAppListAdapter extends RecyclerView.Adapter<RecommendAppListAdapter.ViewHolder> {
    private List<RecommendApp> appList = new ArrayList<>();
    private final RecommendAppDao recommendAppDao;
    private Set<String> recommendedPackages; // 用于存储推荐跳转的包名


    public RecommendAppListAdapter(Context context) {
        this.recommendAppDao = new RecommendAppDao(context);
        loadRecommendedApps(); // 加载添加到推荐跳转中的应用
    }

    private void loadRecommendedApps() {
        recommendedPackages = new HashSet<>();
        List<RecommendApp> recommendApps = recommendAppDao.getAllApps();
        for (RecommendApp app : recommendApps) {
            recommendedPackages.add(app.getPkgName());
        }
    }

    public void setAppList(List<RecommendApp> apps) {
        this.appList = apps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendApp recommendApp = appList.get(position);
        holder.ivIcon.setImageDrawable(recommendApp.getAppIcon());
        holder.tvName.setText(recommendApp.getAppName());
        holder.tvWeight.setText("推荐权重:" + recommendApp.getWeight());
        Bundle args = new Bundle();
        args.putString("pkgName", recommendApp.getPkgName());
        // 设置整个项的点击事件（可选）
        holder.itemView.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.recommendAppConfigFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    // 刷新监控状态（在数据库更改后调用）
    public void refreshMonitoredStatus() {
        loadRecommendedApps();
//        for (RecommendApp app : appList) {
//            app.setChecked(recommendedPackages.contains(app.getPackageName()));
//        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvWeight;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.iv_app_icon);
            tvName = view.findViewById(R.id.tv_app_name);
            tvWeight = view.findViewById(R.id.tv_app_weight);
        }
    }
}