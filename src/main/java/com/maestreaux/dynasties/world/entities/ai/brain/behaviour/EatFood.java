package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;

import java.util.List;

public class EatFood<E extends AbstractDynastyVillager> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private ItemStack foodItem;
    private int consumedItemNutrition = 0;

    public EatFood() {
        super(32);
    }

    @Override
    protected void doDelayedAction(E entity) {
        if (this.foodItem != null) {
            // foodItem.shrink(1);
            entity.setHunger(entity.getHunger() + (1000 * this.consumedItemNutrition));
            entity.eatFood(this.foodItem.getItem());
        }

        entity.setIsEating(false);
    }

    @Override
    protected void start(E entity) {
        // TODO: sorted by preference
        this.foodItem = entity.getInventory().getItems().stream().filter(item -> Dictionaries.FOOD.contains(item.getItem())).findFirst().orElse(null);
        this.consumedItemNutrition = 0;

        if (this.foodItem != null) {
            var consumable = this.foodItem.get(DataComponents.CONSUMABLE);
            var foodComponent = this.foodItem.get(DataComponents.FOOD);

            if (consumable != null) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, this.foodItem);

                if (foodComponent != null) {
                    this.consumedItemNutrition = foodComponent.nutrition();
                }

                consumable.startConsuming(entity, this.foodItem, InteractionHand.MAIN_HAND);
                entity.setIsEating(true);
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return entity.getInventory().hasAnyOf(Dictionaries.FOOD) && entity.getHunger() < entity.getMaxHunger();
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{});
    }
}
