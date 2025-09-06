package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
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
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class CookFood<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private CampfireBlockEntity campfire;
    private ItemStack ingredient;

    protected void start(E entity) {
        if (AIUtils.isCloseEnoughToTarget(entity, this.campfire.getBlockPos())) {
            this.campfire.placeFood((ServerLevel) entity.level(), entity, this.ingredient);
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        } else {
            BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.campfire.getBlockPos()));
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.campfire.getBlockPos(), 0.6F, 1));
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());
        var jobAvailable = false;
        this.ingredient = entity.getInventory().getItems().stream().filter(item -> Dictionaries.INGREDIENTS.contains(item.getItem())).findFirst().orElse(null);

        if (homePlot != null && this.ingredient != null) {
            var relevantJob = homePlot.jobMap.get(Plot.JobType.COOK_FOOD);

            if (!relevantJob.isFulfilledForToday(entity.level().getGameTime())) {
                if (!relevantJob.isClaimed()) {
                    relevantJob.claim(entity);
                    jobAvailable = true;
                } else if (relevantJob.getClaimant() == entity) {
                    jobAvailable = true;
                }

                if (!AIUtils.shouldCook(entity)) {
                    relevantJob.fulfill(level.getGameTime());
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        var campfires = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CAMPFIRES.get());
        if (campfires != null) {
            this.campfire = campfires.getFirst();
        }

        return  !this.ingredient.isEmpty() && this.campfire != null && this.campfire.getItems().stream().anyMatch(ItemStack::isEmpty) && jobAvailable && AIUtils.shouldCook(entity);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.INGREDIENTS.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.HOME_CAMPFIRES.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
