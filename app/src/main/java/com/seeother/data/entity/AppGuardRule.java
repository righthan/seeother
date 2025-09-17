package com.seeother.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 应用守卫规则实体
 */
@Entity(tableName = "app_guard_rules")
public class AppGuardRule {
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * 应用包名
     */
    private String packageName;

    /**
     * 事件触发类型: S(TYPE_VIEW_SCROLLED) 或 C(TYPE_WINDOW_CONTENT_CHANGED)
     */
    private String eventType;

    /**
     * Activity名称，如果为空则匹配所有Activity
     */
    private String activityName;

    /**
     * 组件ID，用于获取内容
     */
    private String viewId;

    /**
     * 是否需要通过特殊符号判断（如@符号）
     */
    private boolean useSpecialSymbol;

    /**
     * 特殊符号（默认为@）
     */
    private String specialSymbol;

    /**
     * 是否启用此规则
     */
    private boolean enabled;

    /**
     * 事件触发间隔（毫秒）
     */
    private long broadcastInterval;

    /**
     * 备注
     */
    private String remark;

    /**
     * 浏览个数阈值（触发守卫的滑动数量）
     */
    private int scrollCount;

    // 构造函数
    public AppGuardRule() {
        this.eventType = "S"; // 默认为TYPE_VIEW_SCROLLED
        this.useSpecialSymbol = false;
        this.specialSymbol = ""; // 默认为空，只有需要的应用才设置
        this.enabled = true;
        this.broadcastInterval = 500L; // 默认500ms间隔
        this.scrollCount = 5; // 默认5个滑动量触发
    }

    public AppGuardRule(String packageName, String eventType, String activityName,
                        String viewId, boolean useSpecialSymbol, String specialSymbol,
                        boolean enabled, long broadcastInterval, String remark) {
        this.packageName = packageName;
        this.eventType = eventType;
        this.activityName = activityName;
        this.viewId = viewId;
        this.useSpecialSymbol = useSpecialSymbol;
        this.specialSymbol = specialSymbol;
        this.enabled = enabled;
        this.broadcastInterval = broadcastInterval;
        this.remark = remark;
        this.scrollCount = 5; // 默认5个滑动量触发
    }

    public AppGuardRule(String packageName, String eventType, String activityName,
                        String viewId, boolean useSpecialSymbol, String specialSymbol,
                        boolean enabled, long broadcastInterval, String remark, int scrollCount) {
        this.packageName = packageName;
        this.eventType = eventType;
        this.activityName = activityName;
        this.viewId = viewId;
        this.useSpecialSymbol = useSpecialSymbol;
        this.specialSymbol = specialSymbol;
        this.enabled = enabled;
        this.broadcastInterval = broadcastInterval;
        this.remark = remark;
        this.scrollCount = scrollCount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public boolean isUseSpecialSymbol() {
        return useSpecialSymbol;
    }

    public void setUseSpecialSymbol(boolean useSpecialSymbol) {
        this.useSpecialSymbol = useSpecialSymbol;
    }

    public String getSpecialSymbol() {
        return specialSymbol;
    }

    public void setSpecialSymbol(String specialSymbol) {
        this.specialSymbol = specialSymbol;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getBroadcastInterval() {
        return broadcastInterval;
    }

    public void setBroadcastInterval(long broadcastInterval) {
        this.broadcastInterval = broadcastInterval;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getScrollCount() {
        return scrollCount;
    }

    public void setScrollCount(int scrollCount) {
        this.scrollCount = scrollCount;
    }

    /**
     * 检查事件类型是否匹配
     */
    public boolean matchesEventType(int accessibilityEventType) {
        switch (eventType) {
            case "S":
                return accessibilityEventType == android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED;
            case "C":
                return accessibilityEventType == android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
            default:
                return false;
        }
    }

    /**
     * 检查Activity是否匹配
     */
    public boolean matchesActivity(String currentActivity) {
        if (activityName == null || activityName.isEmpty()) {
            return true; // 如果没有指定Activity，则匹配所有
        }
        return activityName.equals(currentActivity);
    }

    /**
     * 是否有ViewId可以使用
     */
    public boolean hasViewId() {
        return viewId != null && !viewId.isEmpty();
    }
} 