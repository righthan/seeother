package com.seeother.ui.monitor;

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
import com.seeother.data.db.MonitoredAppDao;
import com.seeother.data.entity.MonitoredApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonitoredAppListAdapter extends RecyclerView.Adapter<MonitoredAppListAdapter.ViewHolder> {
    private List<MonitoredApp> appList = new ArrayList<>();
    private final MonitoredAppDao monitoredAppDao;
    private Set<String> monitoredPackages;

    public MonitoredAppListAdapter(Context context) {
        this.monitoredAppDao = new MonitoredAppDao(context);
        loadMonitoredApps();
    }

    private void loadMonitoredApps() {
        monitoredPackages = new HashSet<>();
        List<MonitoredApp> monitoredApps = monitoredAppDao.getAllApps();
        for (MonitoredApp app : monitoredApps) {
            monitoredPackages.add(app.getPkgName());
        }
    }

    public void setAppList(List<MonitoredApp> apps) {
        this.appList = apps;
        // 设置每个应用的初始监控状态
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.monitored_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonitoredApp app = appList.get(position);
        holder.ivIcon.setImageDrawable(app.getAppIcon());
        holder.tvName.setText(app.getAppName());
        Bundle args = new Bundle();
        args.putString("pkgName", app.getPkgName());
        // 设置整个项的点击事件（可选）
        holder.itemView.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.monitorAppConfigFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvLimitTime;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.iv_app_icon);
            tvName = view.findViewById(R.id.tv_app_name);
            tvLimitTime = view.findViewById(R.id.tv_app_limit_time);
        }
    }
}