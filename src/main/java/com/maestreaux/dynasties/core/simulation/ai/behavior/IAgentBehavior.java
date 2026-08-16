package com.maestreaux.dynasties.core.simulation.ai.behavior;

public interface IAgentBehavior {
    boolean canTick();
    void tick();

    boolean canStart();
    boolean hasStarted();
    void start();
    void stop();
}
