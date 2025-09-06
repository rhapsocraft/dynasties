package com.maestreaux.dynasties.core.simulation.cache;

import com.maestreaux.dynasties.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.stream.Collectors;

public class ZoneCache {
    private final Zone zone;
    private final ServerLevel level;

    private final Map<CacheCategory, Map<BlockPos, BlockCacheItem>> indexedCache = new HashMap<>();
    private Map<BlockPos, BlockCacheItem> flatMap = new HashMap<>();
    private Set<SectionPos> sections =  new HashSet<>();

    public ZoneCache(ServerLevel level, Zone zone) {
        this.level = level;
        this.zone = zone;
    }

    public void cacheSections(AABB boundingBox) {
        // Calculate the section bounds
        int minSectionX = ((int) Math.floor(boundingBox.minX)) >> 4;
        int minSectionY = ((int) Math.floor(boundingBox.minY)) >> 4;
        int minSectionZ = ((int) Math.floor(boundingBox.minZ)) >> 4;

        int maxSectionX = ((int) Math.floor(boundingBox.maxX)) >> 4;
        int maxSectionY = ((int) Math.floor(boundingBox.maxY)) >> 4;
        int maxSectionZ = ((int) Math.floor(boundingBox.maxZ)) >> 4;

        // Iterate through all sections in the bounding box
        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    this.sections.add(SectionPos.of(x, y, z));
                }
            }
        }
    }

    public List<SectionPos> getSections() {
        return this.sections.stream().toList();
    }

    public void indexBlocks(AABB boundingBox) {
        var positions = BlockPos.betweenClosedStream(boundingBox);

        positions.forEach(this::insertCacheItem);
    }

    public void insertCacheItem(BlockPos pos) {
        var newPos = new BlockPos(pos);
        var state = this.level.getBlockState(newPos);

        this.flatMap.put(newPos, new BlockCacheItem(this.level, newPos));

        if (state.is(Blocks.FARMLAND)) {
            indexedCache.computeIfAbsent(CacheCategory.FARMLAND, key -> new HashMap<>()).put(newPos, new BlockCacheItem(this.level, newPos));
        } else if (state.getBlock() instanceof CropBlock) {
            indexedCache.computeIfAbsent(CacheCategory.CROP, key -> new HashMap<>()).put(newPos, new CropCacheItem(this.level, newPos));
        }
    }

    public void deleteCacheItem(BlockPos pos) {
        this.flatMap.remove(pos);

        for (var subMap : this.indexedCache.values()) {
            if (subMap.remove(pos) != null) {
                break;
            }
        }
    }

    public Map<BlockPos, BlockCacheItem> getIndexedCacheMap(CacheCategory category) {
        return this.indexedCache.get(category);
    }

    public Map<BlockPos, BlockCacheItem> getCacheMap() {
        return this.flatMap;
    }

    public List<BlockPos> getAllPos() {
        return this.indexedCache.values().stream().flatMap(map -> map.keySet().stream()).toList();
    }

    public enum CacheCategory {
        CROP,
        FARMLAND
    }
}
