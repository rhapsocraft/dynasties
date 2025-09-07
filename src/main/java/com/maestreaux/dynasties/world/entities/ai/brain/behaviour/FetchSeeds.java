package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class FetchSeeds<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    private ItemLocation targetSeed = null;

    protected void start(E entity) {
        var seedLocations = BrainUtils.getMemory(entity, ModMemoryTypes.AVAILABLE_SEEDS.get());

        if (seedLocations != null) {
            if (this.targetSeed != null) {
                var targetPos = this.targetSeed.blockEntity.getBlockPos();

                if (AIUtils.isCloseEnoughToTarget(entity, targetPos)) {
                    var extractedSeeds = this.targetSeed.itemHandler.extractItem(this.targetSeed.slot, 64, false);
                    entity.getInventory().addItem(extractedSeeds);
                    this.targetSeed = null;
                } else {
                    BrainUtils.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
                    BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
                }
            } else {
                this.targetSeed = seedLocations.get(0);
                BrainUtils.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
                BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !entity.getInventory().hasAnyOf(Dictionaries.VALID_SEEDS);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.AVAILABLE_SEEDS.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.HOME_FARMLANDS.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
