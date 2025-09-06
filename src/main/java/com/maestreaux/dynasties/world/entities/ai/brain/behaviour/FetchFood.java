package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.maestreaux.dynasties.world.entities.blockentity.CampfirePotBlockEntity;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;
import java.util.List;

public class FetchFood<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private ItemLocation targetFood = null;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !entity.getInventory().hasAnyOf(Dictionaries.FOOD) && entity.getHunger() < (entity.getMaxHunger() - 3000);
    }

    @Override
    protected void start(E entity) {
        var foodLocations = BrainUtil.getMemory(entity, ModMemoryTypes.AVAILABLE_FOOD.get());

        if (foodLocations != null) {
            if (this.targetFood != null) {
                var targetPos = this.targetFood.blockEntity.getBlockPos();

                if (AIUtils.isCloseEnoughToTarget(entity, targetPos)) {
                    var extractFood = this.targetFood.itemHandler.extractItem(this.targetFood.slot, 1, false);

                    entity.getInventory().addItem(extractFood);
                    this.targetFood = null;
                } else {
                    BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
                    BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
                }
            } else {
                this.targetFood = foodLocations.getFirst();
                BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
                BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            }
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.AVAILABLE_FOOD.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.AVAILABLE_MEAL.get(), MemoryStatus.VALUE_ABSENT),
        });
    }
}
