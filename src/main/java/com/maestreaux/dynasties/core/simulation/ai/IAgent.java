package com.maestreaux.dynasties.core.simulation.ai;

import com.maestreaux.dynasties.core.simulation.ai.behavior.IAgentBehavior;
import com.maestreaux.dynasties.core.simulation.ai.sensor.BaseAgentSensor;
import com.maestreaux.dynasties.core.simulation.ai.sensor.IAgentSensor;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.List;

public interface IAgent {
    ServerLevel getLevel();
    List<IAgentBehavior> getBehaviors();

    Collection<IAgentSensor<?>> getSensors();
    <T extends BaseAgentSensor<?,?>> T getSensor(Class<T> c);
    <T extends IAgentMemory<?>> T getMemory(Class<T> c);

    void tickAI();
}
