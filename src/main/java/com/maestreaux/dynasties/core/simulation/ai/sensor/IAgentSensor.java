package com.maestreaux.dynasties.core.simulation.ai.sensor;

import net.minecraft.server.level.ServerLevel;

public interface IAgentSensor<T> {
    String getSensorType();
    void update(ServerLevel level);
    T getDetections();
}
