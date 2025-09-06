package com.maestreaux.dynasties.core.simulation.cache;

public interface IRandomTickable {

    // Refer to ServerLevel.tickChunk() to accurately simulate how minecraft handles randomTicks
    void randomTick();
}
