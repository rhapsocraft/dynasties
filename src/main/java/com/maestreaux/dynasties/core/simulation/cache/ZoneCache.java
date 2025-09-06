package com.maestreaux.dynasties.core.simulation.cache;

import com.maestreaux.dynasties.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ZoneCache implements BlockGetter {
    private final Zone zone;
    private final ServerLevel level;

    private final Map<CacheCategory, Map<BlockPos, BlockCacheItem>> indexedCache = new HashMap<>();
    private final Map<BlockPos, BlockCacheItem> flatMap = new HashMap<>();
    private final Map<Long, Set<BlockPos>> positionsByChunk = new HashMap<>();

    // Chunks
    private final Set<ChunkPos> chunks =  new HashSet<>();
    private final Map<ChunkPos, Set<SectionPos>> chunkSectionsMap = new HashMap<>();

    public ZoneCache(ServerLevel level, Zone zone) {
        this.level = level;
        this.zone = zone;
    }

    public ServerLevel level() {
        return this.level;
    }

    public void clearSections() {
        this.chunks.clear();
        this.chunkSectionsMap.clear();
    }

    public void cacheSections(AABB boundingBox) {
        // Calculate the section bounds
        int minSectionX = Mth.floor(boundingBox.minX) >> 4;
        int minSectionY = Mth.floor(boundingBox.minY) >> 4;
        int minSectionZ = Mth.floor(boundingBox.minZ) >> 4;

        int maxSectionX = Mth.floor(boundingBox.maxX) >> 4;
        int maxSectionY = Mth.floor(boundingBox.maxY) >> 4;
        int maxSectionZ = Mth.floor(boundingBox.maxZ) >> 4;

        // Iterate through all sections in the bounding box
        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int z = minSectionZ; z <= maxSectionZ; z++) {
                var chunkPos = new ChunkPos(x, z);

                this.chunks.add(chunkPos);

                for (int y = minSectionY; y <= maxSectionY; y++) {
                    var section = SectionPos.of(x, y, z);

                    this.chunkSectionsMap.computeIfAbsent(chunkPos, (pos) -> new HashSet<>()).add(section);
                }
            }
        }
    }

    public Set<SectionPos> getSections(ChunkPos pos) { return this.chunkSectionsMap.get(pos); }

    public Set<ChunkPos> getChunks() { return this.chunks; }

    public void indexBlocks(AABB boundingBox) {
        var positions = BlockPos.betweenClosedStream(boundingBox);

        positions.forEach(this::insertCacheItem);
    }

    public boolean hasChunk(long pos) {
        return this.positionsByChunk.get(pos) != null;
    }

    public Set<BlockPos> getPositionsInChunk(long chunkPos) {
        return this.positionsByChunk.getOrDefault(chunkPos, new HashSet<>());
    }

    public void insertCacheItem(BlockPos pos) {
        var newPos = new BlockPos(pos);
        var state = this.level.getBlockState(newPos);

        BlockCacheItem cacheItem = null;

        if (state.is(Blocks.FARMLAND)) {
            cacheItem = new BlockCacheItem(this, newPos);
            indexedCache.computeIfAbsent(CacheCategory.FARMLAND, key -> new HashMap<>()).put(newPos, cacheItem);
        } else if (state.getBlock() instanceof CropBlock) {
            cacheItem = new CropCacheItem(this, newPos);
            indexedCache.computeIfAbsent(CacheCategory.CROP, key -> new HashMap<>()).put(newPos, cacheItem);
        }

        if (cacheItem != null) {
            this.flatMap.put(newPos, cacheItem);
            this.positionsByChunk.computeIfAbsent(ChunkPos.asLong(newPos), (posLong) -> new HashSet<>()).add(newPos);
        }
    }

    public void removeBlock(BlockPos pos) {
        var cacheItem = this.flatMap.get(pos);

        if (cacheItem != null) {
            cacheItem.state = Blocks.AIR.defaultBlockState();
        }
    }

    public void deleteCacheItem(BlockPos pos) {
        this.flatMap.remove(pos);
        this.positionsByChunk.getOrDefault(ChunkPos.asLong(pos), new HashSet<>()).remove(pos);

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

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos blockPos) {
        var cacheItem = this.flatMap.get(blockPos);

        return cacheItem == null ? Blocks.AIR.defaultBlockState() : cacheItem.state;
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return null;
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    public enum CacheCategory {
        CROP,
        FARMLAND
    }
}
