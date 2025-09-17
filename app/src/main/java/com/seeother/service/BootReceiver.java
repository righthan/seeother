package com.seeother.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.seeother.utils.SettingsSecureUtil;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_REBOOT.equals(intent.getAction())) {

            // 初始化SettingsSecureUtil单例，避免在服务中调用时出现未初始化错误
            SettingsSecureUtil.init(context);

            Intent serviceIntent = new Intent(context, UsageMonitorService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}