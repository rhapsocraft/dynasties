package com.maestreaux.dynasties.core.simulation;

import net.minecraft.server.level.ServerLevel;

public class Simulator {
    // Simulate necessary behavior to enable data-driven simulation of economic actions in unloaded chunks
    private static int LAST_TICK = -1;
    public static int TICK_DIFFERENCE = 0;

    public static void doTick(ServerLevel level, int currentTick) {
        if (LAST_TICK == -1) {
            LAST_TICK = currentTick;
        } else {
            TICK_DIFFERENCE = LAST_TICK - currentTick;
        }

        tickEntities(level);
    }

    public static void tickEntities(ServerLevel level) {
        for (var entity : SimulationState.getEntities(level)) {
            entity.tick();
        }
    }
}
