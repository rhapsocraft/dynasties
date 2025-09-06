package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class FetchIngredients<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private ItemLocation targetIngredient = null;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());
        var isCampfireAvailable = false;
        var jobAvailable = false;

        if (homePlot != null) {
            var relevantJob = homePlot.jobMap.get(Plot.JobType.COOK_FOOD);

            if (!relevantJob.isFulfilledForToday(entity.level().getGameTime())) {
                if (!relevantJob.isClaimed()) {
                    relevantJob.claim(entity);
                    jobAvailable = true;
                } else if (relevantJob.getClaimant() == entity) {
                    jobAvailable = true;
                }


            } else {
                return false;
            }
        }

        var campfires = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CAMPFIRES.get());
        if (campfires != null) {
            var campfire = campfires.getFirst();
            isCampfireAvailable = campfire != null && campfire.getItems().stream().anyMatch(ItemStack::isEmpty);
        }

        return !entity.getInventory().hasAnyOf(Dictionaries.INGREDIENTS) && isCampfireAvailable && jobAvailable && AIUtils.shouldCook(entity);
    }

    @Override
    protected void start(E entity) {
        var ingredientsLocations = BrainUtil.getMemory(entity, ModMemoryTypes.INGREDIENTS.get());

        if (ingredientsLocations != null) {
            if (this.targetIngredient != null) {
                var targetPos = this.targetIngredient.blockEntity.getBlockPos();

                if (AIUtils.isCloseEnoughToTarget(entity, targetPos)) {
                    var extractFood = this.targetIngredient.itemHandler.extractItem(this.targetIngredient.slot, 1, false);

                    entity.getInventory().addItem(extractFood);
                    this.targetIngredient = null;
                } else {
                    BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
                    BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
                }
            } else {
                this.targetIngredient = ingredientsLocations.getFirst();
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
                Pair.of(ModMemoryTypes.HOME_PLOT.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.INGREDIENTS.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
