package com.maestreaux.dynasties.core.simulation;

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

    public static SimulationBlockGetter simulationGetter(ServerLevel level) {
        return new SimulationBlockGetter(level);
    }

    public static void doTick(ServerLevel level, int currentTick) {
        if (LAST_TICK == -1) {
            LAST_TICK = currentTick;
        } else {
            TICK_DIFFERENCE = getTickDifference(currentTick);
        }

        tickEntities(level);
    }

    public static void tickEntities(ServerLevel level) {
        for (var entity : SimulationState.getEntities(level)) {
            entity.tick();
        }
    }

    public static void doRandomTick(ServerLevel level) {
        var randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        var randomTicks = randomTickSpeed * TICK_INTERVAL;

        if (randomTickSpeed > 0) {
            for (var zone : Zone.getZones(level)) {
                var cache = zone.cache;

                var cacheMap = cache.getCacheMap();
                var sections = cache.getSections();

                for (var section : sections) {
                    for (int t = 0; t < randomTicks; t++) {
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
