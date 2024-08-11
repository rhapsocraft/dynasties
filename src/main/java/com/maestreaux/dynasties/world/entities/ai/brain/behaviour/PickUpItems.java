package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class PickUpItems<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    protected void start(E entity) {
        var nearbyItems = BrainUtils.getMemory(entity, SBLMemoryTypes.NEARBY_ITEMS.get());

        if (nearbyItems != null && !nearbyItems.isEmpty()) {
            var nearestVisibleItem = nearbyItems.stream().filter(itemEntity -> entity.wantsToPickUp(itemEntity.getItem())).filter(entity::hasLineOfSight).findFirst().orElse(null);

            if (nearestVisibleItem != null) {
                BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(nearestVisibleItem, true));
                BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(nearestVisibleItem, false), 0.6F, 0));
            } else {
                BrainUtils.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
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
                Pair.of(SBLMemoryTypes.NEARBY_ITEMS.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
