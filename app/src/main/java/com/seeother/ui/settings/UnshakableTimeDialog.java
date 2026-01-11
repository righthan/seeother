package com.seeother.ui.settings;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.seeother.R;
import com.seeother.manager.SettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 雷打不动时间设置对话框
 * 支持添加、删除多个时间段
 */
public class UnshakableTimeDialog {
    
    private final Context context;
    private final SettingsManager settingsManager;
    private final List<TimePeriod> timePeriods;
    private TimePeriodsAdapter adapter;
    
    public UnshakableTimeDialog(Context context) {
        this.context = context;
        this.settingsManager = new SettingsManager(context);
        this.timePeriods = new ArrayList<>();
        loadTimePeriods();
    }
    
    /**
     * 从设置中加载时间段
     */
    private void loadTimePeriods() {
        timePeriods.clear();
        String periodsStr = settingsManager.getUnshakableTimePeriods();
        
        if (periodsStr != null && !periodsStr.trim().isEmpty()) {
            String[] periods = periodsStr.split(";");
            for (String period : periods) {
                period = period.trim();
                if (period.isEmpty()) {
                    continue;
                }
                
                try {
                    String[] times = period.split("-");
                    if (times.length == 2) {
                        String startTime = times[0].trim();
                        String endTime = times[1].trim();
                        timePeriods.add(new TimePeriod(startTime, endTime));
                    }
                } catch (Exception e) {
                    // 忽略格式错误的时间段
                }
            }
        }
    }
    
    /**
     * 保存时间段到设置
     */
    private void saveTimePeriods() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < timePeriods.size(); i++) {
            TimePeriod period = timePeriods.get(i);
            sb.append(period.startTime).append("-").append(period.endTime);
            if (i < timePeriods.size() - 1) {
                sb.append(";");
            }
        }
        settingsManager.setUnshakableTimePeriods(sb.toString());
    }
    
    /**
     * 显示对话框
     */
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_unshakable_time, null);
        
        ListView listView = dialogView.findViewById(R.id.time_periods_list);
        MaterialButton addButton = dialogView.findViewById(R.id.btn_add_period);
        MaterialButton saveButton = dialogView.findViewById(R.id.btn_save);
        MaterialButton cancelButton = dialogView.findViewById(R.id.btn_cancel);
        
        // 设置列表适配器
        adapter = new TimePeriodsAdapter(context, timePeriods);
        listView.setAdapter(adapter);
        
        // 添加时间段
        addButton.setOnClickListener(v -> showAddTimePeriodDialog());
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        // 保存按钮
        saveButton.setOnClickListener(v -> {
            saveTimePeriods();
            Toast.makeText(context, "雷打不动时间已保存", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        // 取消按钮
        cancelButton.setOnClickListener(v -> {
            loadTimePeriods(); // 重新加载，取消修改
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    /**
     * 显示添加时间段对话框
     */
    private void showAddTimePeriodDialog() {
        final String[] startTime = {"00:00"};
        final String[] endTime = {"00:00"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_time_period, null);
        
        TextView startTimeText = dialogView.findViewById(R.id.start_time_text);
        TextView endTimeText = dialogView.findViewById(R.id.end_time_text);
        Button selectStartTimeBtn = dialogView.findViewById(R.id.btn_select_start_time);
        Button selectEndTimeBtn = dialogView.findViewById(R.id.btn_select_end_time);
        
        startTimeText.setText(startTime[0]);
        endTimeText.setText(endTime[0]);
        
        // 选择开始时间
        selectStartTimeBtn.setOnClickListener(v -> {
            String[] parts = startTime[0].split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            TimePickerDialog timePicker = new TimePickerDialog(context,
                    (view, hourOfDay, minuteOfHour) -> {
                        startTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        startTimeText.setText(startTime[0]);
                    }, hour, minute, true);
            timePicker.show();
        });
        
        // 选择结束时间
        selectEndTimeBtn.setOnClickListener(v -> {
            String[] parts = endTime[0].split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            TimePickerDialog timePicker = new TimePickerDialog(context,
                    (view, hourOfDay, minuteOfHour) -> {
                        endTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        endTimeText.setText(endTime[0]);
                    }, hour, minute, true);
            timePicker.show();
        });
        
        builder.setView(dialogView)
                .setTitle("添加雷打不动时间段")
                .setPositiveButton("添加", (dialog, which) -> {
                    timePeriods.add(new TimePeriod(startTime[0], endTime[0]));
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    /**
     * 时间段数据类
     */
    private static class TimePeriod {
        String startTime;
        String endTime;
        
        TimePeriod(String startTime, String endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        @Override
        public String toString() {
            return startTime + " - " + endTime;
        }
    }
    
    /**
     * 时间段列表适配器
     */
    private class TimePeriodsAdapter extends ArrayAdapter<TimePeriod> {
        
        TimePeriodsAdapter(Context context, List<TimePeriod> periods) {
            super(context, 0, periods);
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.item_time_period, parent, false);
            }
            
            TimePeriod period = getItem(position);
            if (period != null) {
                TextView timeText = convertView.findViewById(R.id.time_period_text);
                Button deleteBtn = convertView.findViewById(R.id.btn_delete);
                
                timeText.setText(period.toString());
                
                deleteBtn.setOnClickListener(v -> {
                    timePeriods.remove(position);
                    notifyDataSetChanged();
                });
            }
            
            return convertView;
        }
    }
}
