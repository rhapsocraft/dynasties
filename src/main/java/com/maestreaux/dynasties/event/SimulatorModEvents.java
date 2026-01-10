package com.maestreaux.dynasties.event;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.simulation.Simulator;
import com.maestreaux.dynasties.core.simulation.cache.BlockCacheItem;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID)
public class SimulatorModEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Implement only on Overworld for now
            var level = event.getServer().getLevel(ServerLevel.OVERWORLD);

            var currentTick = event.getServer().getTickCount();
            if (currentTick % Simulator.TICK_INTERVAL == 0) {
                Simulator.doTick(level, currentTick);
            }
        }
    }

    private static void onAccessCacheItem(LevelAccessor level, long chunkPosLong, Consumer<BlockCacheItem> consumer) {
        if (!level.isClientSide()) {
            // TODO: Improve data structure for zones
            // curse you past me. you forgot to explain why we need to improve the data structure
            // note: game is checking every zone for every chunk loaded. we will need to index zones by chunk position
            for (var zone : Zone.getZones((ServerLevel) level)) {
                if (zone.cache != null && zone.cache.hasChunk(chunkPosLong)) {
                    var posMap = zone.cache.getPositionsInChunk(chunkPosLong);
                    var cacheMap = zone.cache.getCacheMap();

                    for (var pos : posMap) {
                        var cacheItem = cacheMap.get(pos);

                        if (cacheItem != null) {
                            consumer.accept(cacheItem);
                        }
                    }
                }
            }
        }
    }
//
//    @SubscribeEvent
//    public static void onChunkLoad(ChunkEvent.Load event) {
//        if (!event.getLevel().isClientSide()) {
//            var level = event.getLevel();
//            var posLong = event.getChunk().getPos().toLong();
//
//            onAccessCacheItem(level, posLong, BlockCacheItem::applyBlockState);
//        }
//    }
//
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            var level = event.getLevel();
            var posLong = event.getChunk().getPos().toLong();

            onAccessCacheItem(level, posLong, (cacheItem -> {
                if (cacheItem.getStatus() == BlockCacheItem.CacheStatus.FLUSHED) {
                    cacheItem.cacheBlockState();
                }
            }));
        }
    }

    @SubscribeEvent
    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent entityPlaceEvent) {
        var level = entityPlaceEvent.getLevel();

        if (!level.isClientSide()) {
            var placedBlockPos = entityPlaceEvent.getPos();
            var zone = Zone.getContainerZone((ServerLevel) entityPlaceEvent.getLevel(), placedBlockPos);

            if (zone != null && zone.cache != null) {
                zone.cache.insertCacheItem(placedBlockPos);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent entityBreakEvent) {
        var level = entityBreakEvent.getLevel();

        if (!level.isClientSide()) {
            var placedBlockPos = entityBreakEvent.getPos();
            var zone = Zone.getContainerZone((ServerLevel) entityBreakEvent.getLevel(), placedBlockPos);

            if (zone != null && zone.cache != null) {
                zone.cache.deleteCacheItem(placedBlockPos);
            }
        }
    }
}
