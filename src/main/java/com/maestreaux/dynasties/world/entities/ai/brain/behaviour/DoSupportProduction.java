package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class DoSupportProduction<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private Production<?, ?, ?> production;
    private final Production.ActionDataset dataset = new Production.ActionDataset() {};

    private void tryFulfillRequirements(
            Production.IRequirements requirement,
            Production.ActionDataset dataset, AbstractDynastyVillager entity) {
        requirement.tryFulfillRequirements(dataset, entity);
    }

    protected void start(E entity) {
        var requirement = this.production.getRequirement();
        tryFulfillRequirements(requirement, this.dataset, entity);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {

        // Get Available Assets
        this.production = BrainUtil.getMemory(entity, ModMemoryTypes.BEST_PRODUCTION_TASK.get());

        if (this.production != null) {
            return !this.production.canProduce(entity) && this.production.canPerformSupportTask(entity);
        }

        return false;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {

        // Best Production
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.BEST_PRODUCTION_TASK.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
