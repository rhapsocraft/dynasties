package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class HarvestCrops<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private BlockPos targetCrop;

    protected void start(E entity) {
        var readyCrops = BrainUtils.getMemory(entity, ModMemoryTypes.FULLY_GROWN_CROPS.get());

        if (readyCrops != null && !readyCrops.isEmpty()) {
            if (this.targetCrop != null) {
                if (AIUtils.isCloseEnoughToTarget(entity, this.targetCrop)) {
                    entity.level().destroyBlock(this.targetCrop, true, entity);
                    this.targetCrop = null;
                } else {
                    BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetCrop, 0.6F, 1));
                }
            } else {
                this.targetCrop = readyCrops.get(0);
                BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            }
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.FULLY_GROWN_CROPS.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
