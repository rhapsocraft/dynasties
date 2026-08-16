package com.maestreaux.dynasties.core.simulation.ai.sensor;

import com.maestreaux.dynasties.core.simulation.cache.CropCacheItem;
import com.maestreaux.dynasties.core.simulation.cache.ZoneCache;
import com.maestreaux.dynasties.core.simulation.entity.VillagerEntitySimulated;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CropSensor extends BaseAgentSensor<VillagerEntitySimulated, List<CropCacheItem>> {
    public CropSensor(VillagerEntitySimulated agent) {
        super(agent);
    }

    @Override
    public String getSensorType() {
        return "CropSensor";
    }

    @Override
    public List<CropCacheItem> getDetections() {
        var zone = this.agent.getHomeZone();
        Map<BlockPos, CropCacheItem> cachedCrops = zone.cache.getIndexedCacheMap(ZoneCache.CacheCategory.CROP);

        return cachedCrops.values().stream().map(CropCacheItem::collect).toList();
    }
}
