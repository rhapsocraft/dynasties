package com.maestreaux.dynasties.core.simulation;

import com.maestreaux.dynasties.core.simulation.cache.BlockCacheItem;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.List;

public class Simulator {
    // Simulate necessary behavior to enable data-driven simulation of economic actions in unloaded chunks
    private static int LAST_TICK = -1;
    public static int TICK_DIFFERENCE = 0;
    public static int TICK_INTERVAL = 10;

    public static List<LevelChunkSection> zoneChunksCache = new ArrayList<>();

    public static int getTickDifference(int currentTick) {
        return LAST_TICK - currentTick;
    }

    public static void doTick(ServerLevel level, int currentTick) {
        if (LAST_TICK == -1) {
            LAST_TICK = currentTick;
        } else {
            TICK_DIFFERENCE = getTickDifference(currentTick);
        }

        updateCaches(level);
        doRandomTick(level);
        tickEntities(level);
    }

    public static void updateCaches(ServerLevel level) {
        for (var zone : Zone.getZones(level)) {
            var cache = zone.cache;

            if (cache != null) {
                // Get loaded chunks
                var chunks = cache.getChunks();

                for (var chunk : chunks) {
                    cache.getPositionsInChunk(chunk.toLong()).stream()
                            .map(pos -> cache.getCacheMap().get(pos))
                            .forEach(cacheItem -> {
                                if (level.hasChunk(chunk.x, chunk.z)) {
                                    if (cacheItem.getStatus() == BlockCacheItem.CacheStatus.PENDING) {
                                        // Flush pending changes when block's chunk is loaded on the current tick
                                        cacheItem.applyBlockState();
                                        cacheItem.setStatus(BlockCacheItem.CacheStatus.FLUSHED);
                                    }
                                } else {
                                    // Update cache state if chunk is no longer being loaded/simulated
                                    if (cacheItem.getStatus() == BlockCacheItem.CacheStatus.FLUSHED) {
                                        cacheItem.cacheBlockState();
                                    }
                                }
                            });
                }
            }
        }
    }

    public static void tickEntities(ServerLevel level) {
        for (var entity : SimulationState.getEntities(level)) {
            entity.tick();
        }
    }

    public static void doRandomTick(ServerLevel level) {
        // Reflect random tick game rule
        var randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        var randomTicks = randomTickSpeed * TICK_INTERVAL;

        if (randomTickSpeed > 0) {
            // TODO: Possible overlap of sections causing sections to be ticked more times than necessary
            for (var zone : Zone.getZones(level)) {
                var cache = zone.cache;

                if (cache != null) {
                    var cacheMap = cache.getCacheMap();
                    var chunks = cache.getChunks();

                    for (var chunk : chunks) {
                        // TODO: index zone by chunks
                        // Only do random tick if chunk is not loaded
                        if (!level.hasChunk(chunk.x, chunk.z)) {
                            var sections = cache.getSections(chunk);

                            for (var section : sections) {
                                for (int t = 0; t < randomTicks; t++) {
                                    // Replicates `ServerLevel.tickChunk` random tick implementation
                                    var blockPos = level.getBlockRandomPos(section.minBlockX(), section.minBlockY(), section.minBlockZ(), 15);

                                    var blockToTick = cacheMap.get(blockPos);

                                    if (blockToTick != null) {
                                        blockToTick.randomTick();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
