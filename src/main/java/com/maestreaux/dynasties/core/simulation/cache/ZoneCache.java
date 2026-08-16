package com.maestreaux.dynasties.core.simulation.cache;

import com.maestreaux.dynasties.core.simulation.cache.inventory.InventoryCacheItem;
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

    private final Map<CacheCategory, Map<BlockPos, BlockCacheItem>> blockCategoryCache = new HashMap<>();
    private final Map<BlockPos, BlockCacheItem> flatBlockMap = new HashMap<>();
    private final Map<Long, Set<BlockPos>> positionsByChunk = new HashMap<>();

    private final Map<BlockPos, InventoryCacheItem> inventoryCache = new HashMap<>();

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

    public List<InventoryCacheItem> getInventories() { return this.inventoryCache.values().stream().toList(); }

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

        BlockEntity blockEntity = this.level.getBlockEntity(pos);

        if (blockEntity == null) {
            BlockCacheItem cacheItem = null;

            // TODO: Implement Cache Handler
            if (state.is(Blocks.FARMLAND)) {
                cacheItem = new BlockCacheItem(this, this.level, newPos);
                this.blockCategoryCache.computeIfAbsent(CacheCategory.FARMLAND, key -> new HashMap<>()).put(newPos, cacheItem);
            } else if (state.getBlock() instanceof CropBlock) {
                cacheItem = new CropCacheItem(this, this.level, newPos);
                this.blockCategoryCache.computeIfAbsent(CacheCategory.CROP, key -> new HashMap<>()).put(newPos, cacheItem);
            }

            if (cacheItem != null) {
                this.flatBlockMap.put(newPos, cacheItem);
                this.positionsByChunk.computeIfAbsent(ChunkPos.asLong(newPos), (posLong) -> new HashSet<>()).add(newPos);
            }
        } else {
            var inventoryCacheItem = new InventoryCacheItem(newPos, this.level);
            this.inventoryCache.put(newPos, inventoryCacheItem);

            this.positionsByChunk.computeIfAbsent(ChunkPos.asLong(newPos), (posLong) -> new HashSet<>()).add(newPos);
        }

    }

    public void deleteCacheItem(BlockPos pos) {
        this.flatBlockMap.remove(pos);
        this.positionsByChunk.getOrDefault(ChunkPos.asLong(pos), new HashSet<>()).remove(pos);

        for (var subMap : this.blockCategoryCache.values()) {
            if (subMap.remove(pos) != null) {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ICacheItem> Map<BlockPos, T> getIndexedCacheMap(CacheCategory category) {
        var cacheMap = this.blockCategoryCache.get(category);
        return (Map<BlockPos, T>) cacheMap;
    }

    public Map<BlockPos, BlockCacheItem> getCacheMap() {
        return this.flatBlockMap;
    }

    public List<BlockPos> getAllPos() {
        return this.blockCategoryCache.values().stream().flatMap(map -> map.keySet().stream()).toList();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos blockPos) {
        var cacheItem = this.flatBlockMap.get(blockPos);

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

    interface TypedKey<T> {
        Class<? extends T> getType();
    }

    public enum CacheCategory implements TypedKey<ICacheItem> {
        CROP(CropCacheItem.class),
        FARMLAND(BlockCacheItem.class);

        private final Class<? extends ICacheItem> type;
        CacheCategory(Class<? extends ICacheItem> type) { this.type = type; }

        @Override
        public Class<? extends ICacheItem> getType() {
            return type;
        }
    }
}
