package com.seeother.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.seeother.R;

/**
 * 全局Toast提示工具类
 * 使用悬浮窗来显示Toast提示，可以在应用外显示
 */
public class GlobalToast {
    private static final String TAG = "GlobalToast";
    private static final int LENGTH_SHORT = 2000; // 2秒
    private static final int LENGTH_LONG = 3500;  // 3.5秒

    private static WindowManager windowManager;
    private static View toastView;
    private static Handler handler;
    private static Runnable hideRunnable;

    /**
     * 显示短时间Toast
     */
    public static void showShort(@NonNull Context context, @NonNull String message) {
        show(context, message, LENGTH_SHORT);
    }

    /**
     * 显示长时间Toast
     */
    public static void showLong(@NonNull Context context, @NonNull String message) {
        show(context, message, LENGTH_LONG);
    }

    /**
     * 显示自定义时长的Toast
     */
    public static void show(@NonNull Context context, @NonNull String message, int duration) {
        // 确保在主线程执行
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> show(context, message, duration));
            return;
        }

        // 隐藏之前的Toast
        hide();

        try {
            // 获取WindowManager
            if (windowManager == null) {
                windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            }

            // 创建Toast视图
            toastView = LayoutInflater.from(context).inflate(R.layout.global_toast, null);
            TextView textView = toastView.findViewById(R.id.toast_text);
            textView.setText(message);

            // 设置窗口参数
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.y = 200; // 距离底部的距离

            // 添加视图到窗口
            windowManager.addView(toastView, params);

            // 设置自动隐藏
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }

            hideRunnable = GlobalToast::hide;
            handler.postDelayed(hideRunnable, duration);

        } catch (Exception e) {
            android.util.Log.e(TAG, "显示全局Toast失败", e);
        }
    }

    /**
     * 隐藏Toast
     */
    public static void hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(GlobalToast::hide);
            return;
        }

        try {
            // 移除延迟隐藏的Runnable
            if (handler != null && hideRunnable != null) {
                handler.removeCallbacks(hideRunnable);
                hideRunnable = null;
            }

            // 移除视图
            if (toastView != null && windowManager != null) {
                windowManager.removeView(toastView);
                toastView = null;
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "隐藏全局Toast失败", e);
        }
    }
} 