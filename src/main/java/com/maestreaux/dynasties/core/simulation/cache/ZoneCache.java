package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;

import java.util.Map;

public class ZoneCache {
    public Map<String, Map<BlockCacheType, BlockCacheItem>> blockTypesCache;

    public enum BlockCacheType {
        CROP,
        WORKSTATION,
    }

    public static class BlockCacheItem {
        BlockPos pos;
        BlockCacheType type;
    }
}
