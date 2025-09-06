package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.core.utils.MealUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.maestreaux.dynasties.world.entities.blockentity.CampfirePotBlockEntity;
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

import java.util.Collection;
import java.util.List;

public class CookFood<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private CampfirePotBlockEntity cookingPot;
    private ItemStack ingredient;

    protected void start(E entity) {
        if (AIUtils.isCloseEnoughToTarget(entity, this.cookingPot.getBlockPos())) {
            var mealToCook = BrainUtil.getMemory(entity, ModMemoryTypes.BEST_MEAL.get());
            var homeContainers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());

            if (mealToCook != null && homeContainers != null) {

                // Use ingredients
                var pantryItems = InventoryUtils.getItemLocations(homeContainers, mealToCook.getRecipe().getIngredients().keySet());

                for (var set : mealToCook.getRecipe().getIngredients().entrySet()) {
                    int remaining = set.getValue();

                    while (remaining > 0) {
                        var ingredientsInPantry = pantryItems.get(set.getKey());

                        for (var ingredient: ingredientsInPantry) {
                            var stack = ingredient.getStack();
                            var toRemove = Math.min(stack.getCount(), remaining);
                            stack.shrink(toRemove);

                            remaining -= toRemove;
                        }
                    }
                }

                this.cookingPot.setMeal(mealToCook, mealToCook.getServings());
            }

            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        } else {
            BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.cookingPot.getBlockPos()));
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.cookingPot.getBlockPos(), 0.6F, 1));
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());
        var jobAvailable = false;
        // this.ingredient = entity.getInventory().getItems().stream().filter(item -> Dictionaries.INGREDIENTS.contains(item.getItem())).findFirst().orElse(null);

//        if (homePlot != null && this.ingredient != null) {
//            var relevantJob = homePlot.jobMap.get(Plot.JobType.COOK_FOOD);
//
//            if (!relevantJob.isFulfilledForToday(entity.level().getGameTime())) {
//                if (!relevantJob.isClaimed()) {
//                    relevantJob.claim(entity);
//                    jobAvailable = true;
//                } else if (relevantJob.getClaimant() == entity) {
//                    jobAvailable = true;
//                }
//
//                if (!AIUtils.shouldCook(entity)) {
//                    relevantJob.fulfill(level.getGameTime());
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }

        var cookingPots = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CAMPFIRE_POTS.get());
        var bestMeal = BrainUtil.getMemory(entity, ModMemoryTypes.BEST_MEAL.get());
        var homeContainers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());
        var canCook = false;

        if (bestMeal != null && homeContainers != null) {
            var pantryItems = InventoryUtils.getItemLocations(homeContainers, bestMeal.getRecipe().getIngredients().keySet()).values()
                    .stream().flatMap(Collection::stream).toList();

            canCook = MealUtils.canCookMeal(entity, bestMeal, pantryItems);
        }

        if (cookingPots != null) {
            this.cookingPot = cookingPots.getFirst();
        }

        // return !this.ingredient.isEmpty() && this.cookingPot != null && !this.cookingPot.hasContents() && jobAvailable && AIUtils.shouldCook(entity);
        return this.cookingPot != null && !this.cookingPot.hasContents() && canCook;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
//                Pair.of(ModMemoryTypes.INGREDIENTS.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.HOME_CAMPFIRE_POTS.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.BEST_MEAL.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
