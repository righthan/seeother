package com.seeother.ui.monitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.seeother.R;
import com.seeother.common.AppInfo;
import com.seeother.data.db.MonitoredAppDao;
import com.seeother.data.entity.MonitoredApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private List<AppInfo> appList = new ArrayList<>();
    private OnAppClickListener listener;
    private final MonitoredAppDao monitoredAppDao;
    private final Context context;
    private Set<String> monitoredPackages; // 用于存储被监控的包名

    public interface OnAppClickListener {
        void onAppClick(AppInfo app);
    }

    public AppListAdapter(Context context) {
        this.context = context;
        this.monitoredAppDao = new MonitoredAppDao(context);
        loadMonitoredApps(); // 加载被监控的应用列表
    }

    private void loadMonitoredApps() {
        monitoredPackages = new HashSet<>();
        List<MonitoredApp> monitoredApps = monitoredAppDao.getAllApps();
        for (MonitoredApp app : monitoredApps) {
            monitoredPackages.add(app.getPkgName());
        }
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.listener = listener;
    }

    public void setAppList(List<AppInfo> apps) {
        this.appList = apps;
        // 设置每个应用的初始监控状态
        for (AppInfo app : this.appList) {
            app.setChecked(monitoredPackages.contains(app.getPackageName()));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.ivIcon.setImageDrawable(app.getAppIcon());
        holder.tvName.setText(app.getAppName());

        // 设置CheckBox的初始状态，并避免触发监听器
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(app.getChecked());

        // 设置 CheckBox 的点击监听器
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 添加到监控列表
                MonitoredApp monitoredApp = new MonitoredApp();
                monitoredApp.setPkgName(app.getPackageName());
                monitoredApp.setEnableGrayMode(false); // 默认值

                long id = monitoredAppDao.insert(monitoredApp);
                if (id != -1) {
                    app.setChecked(true);
                    monitoredPackages.add(app.getPackageName());
                    if (listener != null) {
                        listener.onAppClick(app);
                    }
                }
            } else {
                // 显示确认对话框
                new AlertDialog.Builder(context)
                        .setTitle("删除应用")
                        .setMessage("确定要删除 " + app.getAppName() + " 吗？将删除与之关联的配置")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 删除应用本身
                            MonitoredApp existingApp = monitoredAppDao.getAppByPkgName(app.getPackageName());
                            if (existingApp != null) {
                                int ret = monitoredAppDao.delete(existingApp.getId());
                                if (ret > 0) {
                                    app.setChecked(false);
                                    monitoredPackages.remove(app.getPackageName());
                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            // 恢复 CheckBox 状态
                            holder.checkBox.setChecked(true);
                        })
                        .show();
            }
        });

        // 设置整个项的点击事件（可选）
        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.performClick();
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    // 刷新监控状态（在数据库更改后调用）
    public void refreshMonitoredStatus() {
        loadMonitoredApps();
        for (AppInfo app : appList) {
            app.setChecked(monitoredPackages.contains(app.getPackageName()));
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.iv_app_icon);
            tvName = view.findViewById(R.id.tv_app_name);
            checkBox = view.findViewById(R.id.cb_monitored);
        }
    }
}