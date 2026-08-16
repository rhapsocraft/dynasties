package com.maestreaux.dynasties.core.simulation.cache;

import java.util.List;

public interface ICacheHolder<T extends ICacheItem> {
    void insertCacheItem(ICacheItem item);
    void flushCacheItem(ICacheItem item);
    List<T> getCacheItems();
}
