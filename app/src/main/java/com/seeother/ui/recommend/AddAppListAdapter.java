package com.seeother.ui.recommend;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.seeother.R;
import com.seeother.common.AppInfo;
import com.seeother.data.db.RecommendAppDao;
import com.seeother.data.entity.RecommendApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddAppListAdapter extends RecyclerView.Adapter<AddAppListAdapter.ViewHolder> {
    private List<AppInfo> appList = new ArrayList<>();
    private OnAppClickListener listener;
    private final RecommendAppDao recommendAppDao;
    private final Context context;
    private Set<String> recommendedPackages;
    private final RecommendDataManager dataManager;

    public interface OnAppClickListener {
        void onAppClick(AppInfo app);
    }

    public AddAppListAdapter(Context context) {
        this.context = context;
        this.recommendAppDao = new RecommendAppDao(context);
        this.dataManager = RecommendDataManager.getInstance();
        loadMonitoredApps();
    }

    private void loadMonitoredApps() {
        recommendedPackages = new HashSet<>();
        List<RecommendApp> recommendedApps = recommendAppDao.getAllApps();
        for (RecommendApp app : recommendedApps) {
            recommendedPackages.add(app.getPkgName());
        }
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.listener = listener;
    }

    public void setAppList(List<AppInfo> apps) {
        this.appList = apps;
        // 设置每个应用的初始监控状态
        for (AppInfo app : this.appList) {
            app.setChecked(recommendedPackages.contains(app.getPackageName()));
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
                // 添加到推荐列表
                RecommendApp recommendApp = new RecommendApp();
                recommendApp.setPkgName(app.getPackageName());
                recommendApp.setWeight(1);     // 默认值

                new Thread(() -> {
                    long id = recommendAppDao.insert(recommendApp);
                    if (id != -1) {
                        // 在主线程更新UI
                        if (context instanceof androidx.fragment.app.FragmentActivity) {
                            ((androidx.fragment.app.FragmentActivity) context).runOnUiThread(() -> {
                                app.setChecked(true);
                                recommendedPackages.add(app.getPackageName());
                                // 通知数据发生变化
                                dataManager.notifyDataChanged();
                                Intent intent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
                                if (intent != null) {
                                    new AlertDialog.Builder(context)
                                            .setTitle("打开测试提示")
                                            .setMessage("为了保证能够打开该应用，将进行一次打开测试。\n如果弹出系统弹窗，请同意 SeeOther 打开该应用。")
                                            .setPositiveButton("我知道了", (dialog, which) -> {
                                                context.startActivity(intent);
                                            })
                                            .setNegativeButton("稍后同意", null)
                                            .show();
                                }
                            });
                        }
                    }
                }).start();
            } else {
                // 显示确认对话框
                new AlertDialog.Builder(context)
                        .setTitle("取消推荐")
                        .setMessage("确定要取消推荐 " + app.getAppName() + " 吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 从数据库中删除
                            new Thread(() -> {
                                RecommendApp existingApp = recommendAppDao.getAppByPkgName(app.getPackageName());
                                if (existingApp != null) {
                                    int result = recommendAppDao.delete(existingApp.getId());
                                    if (result > 0) {
                                        // 在主线程更新UI
                                        if (context instanceof androidx.fragment.app.FragmentActivity) {
                                            ((androidx.fragment.app.FragmentActivity) context).runOnUiThread(() -> {
                                                app.setChecked(false);
                                                recommendedPackages.remove(app.getPackageName());
                                                // 通知数据发生变化
                                                dataManager.notifyDataChanged();
                                            });
                                        }
                                    }
                                }
                            }).start();
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