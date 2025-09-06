package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

public class AdjustValuationsAndPrices<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return super.checkExtraStartConditions(level, entity);

    }
}
