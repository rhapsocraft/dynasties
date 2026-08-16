package com.maestreaux.dynasties.core.simulation.ai.behavior;

import com.maestreaux.dynasties.core.simulation.ai.IAgent;

// Initial implementation: Add Harvesting behavior

    /*
        Harvest behavior
        get entity zone

        is zone loaded?
        yes:
            get all crops ready for harvest in zone
        no:
            get all cached crops ready for harvest in zone

        set move target to nearest available crop

        harvest crop once nearby

        transfer crop drops into entity/agent inventory

        replant seeds if applicable

        get nearest valid storage

        set move target to nearest valid storage

        transfer crops from entity inventory into storage

    */

public class BaseAgentBehavior implements IAgentBehavior {
    protected boolean started = false;
    protected IAgent agent;

    public BaseAgentBehavior(IAgent agent) {
        this.agent = agent;
    }

    @Override
    public boolean canTick() {
        return this.started;
    }

    @Override
    public void tick() {}

    @Override
    public boolean canStart() { return false; }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public boolean hasStarted() {
        return this.started;
    }
}
