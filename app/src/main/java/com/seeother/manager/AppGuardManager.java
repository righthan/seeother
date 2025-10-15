package com.seeother.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.RoomDatabase;

import com.seeother.data.entity.AppGuardRule;
import com.seeother.data.repository.AppGuardRuleRepository;
import com.seeother.data.entity.MonitoredApp;
import com.seeother.data.db.MonitoredAppDao;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 应用守卫管理器
 * 负责管理应用守卫规则和处理相关逻辑
 */
public class AppGuardManager {
    private static final String TAG = "AppGuardManager";
    private final Set<String> authorSet = new HashSet<>(); // 存储作者名称集合
    private final Context context;
    private final List<AppGuardRule> currentAppRules; // 当前应用的规则
    private long lastHandleTime = 0; // 全局的上次处理时间
    private final AppGuardRuleRepository repository;
    private String currentPackageName; // 当前加载规则的包名
    private final SettingsManager settingsManager;
    private final MonitoredAppDao monitoredAppDao;

    public AppGuardManager(Context context) {
        this.context = context;
        this.currentAppRules = new ArrayList<>();
        this.repository = new AppGuardRuleRepository(context);
        this.currentPackageName = "";
        this.settingsManager = new SettingsManager(context);
        this.monitoredAppDao = new MonitoredAppDao(context);
    }

    /**
     * 初始化守卫规则（从数据库加载或创建默认规则）
     */
    public void initializeGuardRules() {
        try {
            List<AppGuardRule> defaultRules = createDefaultRules();
            // 每次启动都清空现有规则并重新插入默认规则
            repository.deleteAll();
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    repository.insertAll(defaultRules);
                }
            }, 1000);
        } catch (Exception e) {
            Log.e(TAG, "初始化守卫规则失败", e);
        }
    }

    /**
     * 从数据库加载指定包名的规则
     */
    private void loadRulesForPackage(String packageName) {
        if (packageName == null || packageName.equals(currentPackageName)) {
            return; // 如果是相同包名，不重复加载
        }

        List<AppGuardRule> packageRules = repository.getRulesForPackageSync(packageName);

        synchronized (currentAppRules) {
            currentAppRules.clear();
            currentAppRules.addAll(packageRules);
            currentPackageName = packageName;
        }

        // 切换应用时清空作者集合
        synchronized (authorSet) {
            authorSet.clear();
            Log.d(TAG, "切换应用，清空作者集合");
        }

        Log.d(TAG, "为包名 " + packageName + " 加载了 " + packageRules.size() + " 个守卫规则");
    }

    /**
     * 创建默认的守卫规则
     */
    private List<AppGuardRule> createDefaultRules() {
        List<AppGuardRule> defaultRules = new ArrayList<>();

        // 微信视频号
        defaultRules.add(new AppGuardRule(
                "com.tencent.mm", "S",
                "com.tencent.mm.plugin.finder.ui.FinderHomeAffinityUI",
                "", false, "", "视频号主页"
        ));

        defaultRules.add(new AppGuardRule(
                "com.tencent.mm", "S",
                "com.tencent.mm.plugin.finder.ui.FinderShareFeedRelUI",
                "", false, "", "视频号分享"
        ));

        // 抖音 - 有ViewId，不需要特殊符号
        defaultRules.add(new AppGuardRule(
                "com.ss.android.ugc.aweme", "C",
                "",
                "com.ss.android.ugc.aweme:id/title",
                false, "", "抖音短视频"
        ));

        // 小红书 - 有ViewId，不需要特殊符号
        defaultRules.add(new AppGuardRule(
                "com.xingin.xhs", "S",
                "com.xingin.matrix.detail.activity.DetailFeedActivity",
                "com.xingin.xhs:id/matrixNickNameView",
                false, "", "小红书详情页"
        ));

        defaultRules.add(new AppGuardRule(
                "com.xingin.xhs", "S",
                "com.xingin.xhs.index.v2.IndexActivityV2",
                "com.xingin.xhs:id/matrixNickNameView",
                false, "", "小红书热门页面"
        ));

        // B站 - 有ViewId，不需要特殊符号
        defaultRules.add(new AppGuardRule(
                "tv.danmaku.bili", "S",
                "com.bilibili.video.story.StoryVideoActivity",
                "tv.danmaku.bili:id/name",
                false, "", "B站短视频页面"
        ));

        // 快手 - 有ViewId，不需要特殊符号  
        defaultRules.add(new AppGuardRule(
                "com.smile.gifmaker", "S",
                "com.yxcorp.gifshow.HomeActivity",
                "",
                true, "@", "快手精选页面"
        ));

        defaultRules.add(new AppGuardRule(
                "com.smile.gifmaker", "S",
                "com.yxcorp.gifshow.detail.PhotoDetailActivity",
                "com.smile.gifmaker:id/username_group",
                false, "", "快手首页"
        ));

        // 淘宝 - 有ViewId，不需要特殊符号
        defaultRules.add(new AppGuardRule(
                "com.taobao.taobao", "S",
                "",
                "com.taobao.taobao:id/video_host",
                false, "", "淘宝短视频"
        ));

        // 支付宝 - 有ViewId，不需要特殊符号
        defaultRules.add(new AppGuardRule(
                "com.eg.android.AlipayGphone", "S",
                "",
                "com.alipay.android.living.dynamic:id/author_title",
                false, "", "支付宝生活动态"
        ));

        // 微博 - 有ViewId，不需要特殊符号
        defaultRules.add(new AppGuardRule(
                "com.sina.weibo", "S",
                "",
                "com.sina.weibo:id/nickname_new_ui_message",
                false, "", "微博信息流"
        ));

        // QQ空间
        defaultRules.add(new AppGuardRule(
                "com.tencent.mobileqq", "S",
                "com.tencent.mobileqq.activity.QPublicTransFragmentActivity",
                "", false, "", "QQ空间"
        ));

        // 爱奇艺 - 需要使用@符号判断
        defaultRules.add(new AppGuardRule(
                "com.qiyi.video", "S",
                "",
                "", true, "@", "爱奇艺短视频"
        ));

        // 拼多多 - 需要使用@符号判断
        defaultRules.add(new AppGuardRule(
                "com.xunmeng.pinduoduo", "S",
                "",
                "", true, "@", "拼多多信息流"
        ));

        // 红果短剧
        defaultRules.add(new AppGuardRule(
                "com.phoenix.read", "S",
                "com.dragon.read.component.shortvideo.impl.ShortSeriesActivity",
                "", false, "", "红果短剧"
        ));

        return defaultRules;
    }

    /**
     * 检查是否应该处理当前事件
     */
    public boolean shouldProcessEvent(int eventType, String packageName, String activityName) {
        if (packageName == null) {
            return false;
        }

        // 首先检查该应用是否在监控列表中且启用了守卫
        MonitoredApp monitoredApp = monitoredAppDao.getAppByPkgName(packageName);
        if (monitoredApp == null || !monitoredApp.isGuardEnabled()) {
            return false;
        }

        // 按需加载当前包名的规则
        loadRulesForPackage(packageName);

//        Log.d(TAG, "shouldProcessEvent: eventType: " + eventType + ", packageName: " + packageName + ", activityName: " + activityName);
        synchronized (currentAppRules) {
            for (AppGuardRule rule : currentAppRules) {
                if (rule.matchesEventType(eventType) && rule.matchesActivity(activityName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 处理事件并发送广播
     */
    public void processEvent(int eventType, String packageName, String activityName,
                             AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return;
        }

        // 获取监控应用的配置
        MonitoredApp monitoredApp = monitoredAppDao.getAppByPkgName(packageName);
        if (monitoredApp == null || !monitoredApp.isGuardEnabled()) {
            return;
        }

        // 确保当前包名的规则已加载
        loadRulesForPackage(packageName);

        synchronized (currentAppRules) {
            for (AppGuardRule rule : currentAppRules) {
                if (!rule.matchesEventType(eventType) || !rule.matchesActivity(activityName)) {
                    continue;
                }

                // 事件处理间隔
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastHandleTime < monitoredApp.getBroadcastInterval()) {
                    continue;
                }
                lastHandleTime = currentTime;

                String authorName = extractAuthorName(rule, rootNode);

                if (authorName != null && !authorName.isEmpty()) {
                    // 添加到作者集合中
                    synchronized (authorSet) {
                        authorSet.add(authorName);
                        Log.d(TAG, "添加作者: " + authorName + ", 当前数量: " + authorSet.size() + "/" + monitoredApp.getScrollCount());

                        // 检查是否达到浏览个数阈值
                        if (authorSet.size() >= monitoredApp.getScrollCount() && !settingsManager.getPauseEnabled()) {
                            sendBroadcast();
                            authorSet.clear(); // 清空集合，重新开始计数
                            Log.d(TAG, "发送守卫广播: " + packageName + " - " + authorName + ", 已达到阈值: " + monitoredApp.getScrollCount());
                        }
                    }
                    break; // 找到匹配的规则后就退出
                }
            }
        }
    }

    /**
     * 根据规则提取作者名称
     */
    private String extractAuthorName(AppGuardRule rule, AccessibilityNodeInfo rootNode) {
        try {
            if (rule.hasViewId()) {
                // 通过ViewId获取文本
                List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(rule.getViewId());
                if (nodes != null && !nodes.isEmpty()) {
                    String text = nodes.get(0).getText() != null ? nodes.get(0).getText().toString() : "";
                    // 回收节点
                    for (AccessibilityNodeInfo node : nodes) {
                        node.recycle();
                    }
                    return text;
                }
            } else if (rule.isUseSpecialSymbol()) {
                // 通过特殊符号查找
                return findTextWithSpecialSymbol(rootNode, rule.getSpecialSymbol());
            } else {
                // 使用时间戳作为标识
                return LocalTime.now().toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "提取作者名称失败", e);
        }

        return null;
    }

    /**
     * 查找包含特殊符号的文本（广度优先搜索）
     */
    private String findTextWithSpecialSymbol(AccessibilityNodeInfo rootNode, String symbol) {
        if (rootNode == null || symbol == null || symbol.isEmpty()) {
            return null;
        }

        List<AccessibilityNodeInfo> nodesToRecycle = new ArrayList<>();

        try {
            // 先检查根节点
            String result = checkNodeForSymbol(rootNode, symbol);
            if (result != null) {
                return result;
            }

            // 使用广度优先搜索，限制深度避免性能问题
            return performBreadthFirstSearch(rootNode, symbol, nodesToRecycle, 20); // 限制20层深度

        } catch (Exception e) {
            Log.e(TAG, "查找特殊符号文本失败", e);
        } finally {
            // 清理资源：回收创建的子节点
            for (AccessibilityNodeInfo node : nodesToRecycle) {
                try {
                    if (node != null && node != rootNode) {
                        node.recycle();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "回收节点失败: " + e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * 执行广度优先搜索
     */
    private String performBreadthFirstSearch(AccessibilityNodeInfo rootNode, String symbol,
                                             List<AccessibilityNodeInfo> nodesToRecycle, int maxDepth) {
        List<AccessibilityNodeInfo> currentLevel = new ArrayList<>();
        currentLevel.add(rootNode);

        for (int depth = 0; depth < maxDepth && !currentLevel.isEmpty(); depth++) {
            List<AccessibilityNodeInfo> nextLevel = new ArrayList<>();

            for (AccessibilityNodeInfo node : currentLevel) {
                if (node == null) continue;

                try {
                    // 检查当前层的所有子节点
                    int childCount = node.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        AccessibilityNodeInfo childNode = node.getChild(i);
                        if (childNode != null) {
                            nodesToRecycle.add(childNode); // 记录需要回收的节点

                            // 立即检查子节点
                            String result = checkNodeForSymbol(childNode, symbol);
                            if (result != null) {
                                return result; // 找到结果立即返回
                            }

                            nextLevel.add(childNode);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "处理节点子元素时出错: " + e.getMessage());
                    // 继续处理其他节点
                }
            }

            currentLevel = nextLevel;
        }

        return null;
    }

    /**
     * 检查单个节点是否包含特殊符号
     */
    private String checkNodeForSymbol(AccessibilityNodeInfo node, String symbol) {
        if (node == null || symbol == null || symbol.isEmpty()) {
            return null;
        }

        try {
            // 检查节点文本
            CharSequence text = node.getText();
//            Log.d(TAG,"Text:"+text+",viewID:"+viewId);
            if (text != null && text.toString().contains(symbol)) {
                return text.toString();
            }

            // 检查ViewId（新浪微博等能够通过遍历查找含有viewId的节点, 但是直接使用findAccessibilityNodeInfosByViewId获取的节点, 无法获取到文本）
            String viewId = node.getViewIdResourceName();
            if (viewId != null && viewId.contains(symbol)) {
                // 如果ViewId包含符号，但文本为空，尝试获取文本
                if (text != null && !text.toString().isEmpty()) {
                    return text.toString();
                }
                // 使用时间戳作为标识
                return LocalTime.now().toString();
            }
            // 检查内容描述
            // CharSequence contentDesc = node.getContentDescription();
            // if (contentDesc != null && contentDesc.toString().contains(symbol)) {
            //     return contentDesc.toString();
            // }
        } catch (Exception e) {
            Log.w(TAG, "检查节点内容失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 发送短视频变化广播
     */
    private void sendBroadcast() {
        Intent intent = new Intent("com.seeother.action.SHOW_FLOATING_WINDOW");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * 获取所有守卫规则（从数据库）
     */
    public List<AppGuardRule> getAllGuardRules() {
        return repository.getAllRulesSync();
    }

    /**
     * 获取当前应用的守卫规则
     */
    public List<AppGuardRule> getCurrentAppRules() {
        synchronized (currentAppRules) {
            return new ArrayList<>(currentAppRules);
        }
    }

    /**
     * 如果需要则刷新缓存
     */
    private void refreshCacheIfNeeded(String packageName) {
        if (packageName.equals(currentPackageName)) {
            synchronized (currentAppRules) {
                currentAppRules.clear();
                currentPackageName = "";
            }
            // 清空作者集合
            synchronized (authorSet) {
                authorSet.clear();
            }
        }
    }

    /**
     * 获取指定包名的所有规则（从数据库）
     */
    public List<AppGuardRule> getRulesForPackage(String packageName) {
        return repository.getRulesForPackageSync(packageName);
    }

    /**
     * 刷新当前应用规则（从数据库重新加载）
     */
    public void refreshCurrentAppRules() {
        synchronized (currentAppRules) {
            String packageToRefresh = currentPackageName;
            currentPackageName = ""; // 强制重新加载
            if (!packageToRefresh.isEmpty()) {
                loadRulesForPackage(packageToRefresh);
            }
        }
    }

    /**
     * 获取Repository实例（用于外部直接数据库操作）
     */
    public AppGuardRuleRepository getRepository() {
        return repository;
    }

    /**
     * 释放资源
     */
    public void destroy() {
        repository.close();
        synchronized (currentAppRules) {
            currentAppRules.clear();
        }
        synchronized (authorSet) {
            authorSet.clear();
        }
        currentPackageName = "";
        lastHandleTime = 0;
    }
} 