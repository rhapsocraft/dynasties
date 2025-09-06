package com.maestreaux.dynasties.world.entities.ai.brain.schedule;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import org.jetbrains.annotations.Nullable;

public class BasicSchedule extends SmartBrainSchedule {

    public BasicSchedule() {
        super(Type.DAYTIME);
    }

    @Override
    public @Nullable Activity tick(LivingEntity brainOwner) {
        var activity = super.tick(brainOwner);
        var brain = brainOwner.getBrain();

        if (activity != null && brain.activityRequirementsAreMet(activity)) {
            return activity;
        } else {
            return Activity.IDLE;
        }
    }
}
