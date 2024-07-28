package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class DoConstruction<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    protected void start(E entity) {
        var homePlot = BrainUtils.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var partitionToBuildOn = homePlot.getPartitionToBuildOn();

            if (partitionToBuildOn != null && !partitionToBuildOn.isConstructionFinished()) {
                var blockToPlaceInfo = partitionToBuildOn.getBlocks().get(partitionToBuildOn.getConstructionCursor());
                var blockState = blockToPlaceInfo.state();
                var blockPos = blockToPlaceInfo.pos().offset(partitionToBuildOn.getAbsoluteOrigin());

                if(entity.level().setBlock(blockPos, blockState, 3)) {
                    this.cooldownFor((e) -> 10);
                } else {
                    this.cooldownFor((e) -> 0);
                };

                partitionToBuildOn.incrementConstructionCursor();
            }
        }
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
