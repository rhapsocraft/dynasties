package com.maestreaux.dynasties.core.simulation.ai.sensor;

import com.maestreaux.dynasties.core.simulation.ai.IAgent;
import net.minecraft.server.level.ServerLevel;

public abstract class BaseAgentSensor<A extends IAgent, T> implements IAgentSensor<T>{
    protected A agent;

    public BaseAgentSensor(A agent) {
        this.agent = agent;
    }

    @Override
    public abstract String getSensorType();

    @Override
    public void update(ServerLevel level) {}

    @Override
    public T getDetections() {
        return null;
    }
}
