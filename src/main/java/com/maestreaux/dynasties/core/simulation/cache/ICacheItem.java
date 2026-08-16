package com.maestreaux.dynasties.core.simulation.cache;

public interface ICacheItem {
    boolean flush();
    boolean cache();
    boolean isLoaded();
}
