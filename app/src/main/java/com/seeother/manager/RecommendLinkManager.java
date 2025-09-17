package com.seeother.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 推荐链接管理器
 * 用于管理推荐链接的存储和概率设置
 */
public class RecommendLinkManager {
    private static final String PREF_NAME = "recommend_links";
    private static final String KEY_LINKS = "links";
    private static final String KEY_PROBABILITY = "probability";
    private static final int DEFAULT_PROBABILITY = 30; // 默认30%概率

    private final SharedPreferences preferences;
    private static RecommendLinkManager instance;

    private RecommendLinkManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized RecommendLinkManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecommendLinkManager(context);
        }
        return instance;
    }

    /**
     * 添加推荐链接
     */
    public void addLink(String link) {
        if (TextUtils.isEmpty(link)) return;
        
        Set<String> links = getLinksSet();
        links.add(link);
        saveLinksSet(links);
    }

    /**
     * 删除推荐链接
     */
    public void removeLink(String link) {
        Set<String> links = getLinksSet();
        links.remove(link);
        saveLinksSet(links);
    }

    /**
     * 更新推荐链接
     */
    public void updateLink(String oldLink, String newLink) {
        if (TextUtils.isEmpty(newLink)) return;
        
        Set<String> links = getLinksSet();
        links.remove(oldLink);
        links.add(newLink);
        saveLinksSet(links);
    }

    /**
     * 获取所有推荐链接
     */
    public List<String> getAllLinks() {
        return new ArrayList<>(getLinksSet());
    }

    /**
     * 检查链接是否存在
     */
    public boolean linkExists(String link) {
        return getLinksSet().contains(link);
    }

    /**
     * 获取推荐链接数量
     */
    public int getLinkCount() {
        return getLinksSet().size();
    }

    /**
     * 设置链接打开概率 (0-100)
     */
    public void setProbability(int probability) {
        if (probability < 0) probability = 0;
        if (probability > 100) probability = 100;
        
        preferences.edit().putInt(KEY_PROBABILITY, probability).apply();
    }

    /**
     * 获取链接打开概率
     */
    public int getProbability() {
        return preferences.getInt(KEY_PROBABILITY, DEFAULT_PROBABILITY);
    }

    /**
     * 获取一个随机链接
     */
    public String getRandomLink() {
        List<String> links = getAllLinks();
        if (links.isEmpty()) return null;
        
        int randomIndex = (int) (Math.random() * links.size());
        return links.get(randomIndex);
    }

    /**
     * 检查是否应该打开链接（基于概率）
     */
    public boolean shouldOpenLink() {
        if (getLinkCount() == 0) return false;
        
        int probability = getProbability();
        return Math.random() * 100 < probability;
    }

    /**
     * 验证链接格式是否有效
     */
    public boolean isValidLink(String link) {
        if (TextUtils.isEmpty(link)) return false;
        
        // 检查是否是有效的URL或Schema
        return link.startsWith("http://") || 
               link.startsWith("https://") || 
               link.contains("://"); // 支持各种schema
    }

    private Set<String> getLinksSet() {
        return new HashSet<>(preferences.getStringSet(KEY_LINKS, new HashSet<>()));
    }

    private void saveLinksSet(Set<String> links) {
        preferences.edit().putStringSet(KEY_LINKS, links).apply();
    }
} 