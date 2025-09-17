package com.seeother.ui.recommend;

import androidx.lifecycle.MutableLiveData;

/**
 * 推荐模块的数据管理器，用于处理Fragment间的数据同步
 */
public class RecommendDataManager {
    private static RecommendDataManager instance;
    private final MutableLiveData<Boolean> dataChanged = new MutableLiveData<>();
    
    private RecommendDataManager() {
        dataChanged.setValue(false);
    }
    
    public static RecommendDataManager getInstance() {
        if (instance == null) {
            synchronized (RecommendDataManager.class) {
                if (instance == null) {
                    instance = new RecommendDataManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 通知数据已更改
     */
    public void notifyDataChanged() {
        dataChanged.postValue(true);
    }
    
    /**
     * 获取数据变化的LiveData
     */
    public MutableLiveData<Boolean> getDataChanged() {
        return dataChanged;
    }
    
    /**
     * 重置数据变化标志
     */
    public void resetDataChanged() {
        dataChanged.setValue(false);
    }
}
