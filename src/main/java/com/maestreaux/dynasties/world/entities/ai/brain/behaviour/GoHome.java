package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class GoHome<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private Plot homePlot;

    protected void start(E entity) {
        var homePartition = this.homePlot.getHomePartition();

        if (homePartition != null) {
            BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(homePartition.getAbsoluteCenter(),0.6F,1));
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.homePlot = BrainUtils.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        return homePlot != null;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_PLOT.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
