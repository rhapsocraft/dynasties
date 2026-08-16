package com.maestreaux.dynasties.core.simulation.ai.behavior;

import com.maestreaux.dynasties.core.simulation.ai.IAgent;
import com.maestreaux.dynasties.core.simulation.ai.sensor.CropSensor;
import com.maestreaux.dynasties.core.simulation.cache.CropCacheItem;

public class HarvestCrops extends BaseAgentBehavior {
    public HarvestCrops(IAgent agent) {
        super(agent);
    }

    @Override
    public boolean canStart() {
        var cropSensor = this.agent.getSensor(CropSensor.class);
        var detections = cropSensor.getDetections();

        return detections.stream().anyMatch(CropCacheItem::isMature);
    }

    @Override
    public void start() {

    }
}
