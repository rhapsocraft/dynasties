package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;

import java.util.List;

public class EatFood<E extends AbstractDynastyVillager> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private Item itemBeingEaten;
    private InteractionResult eatResult;

    private int consumedItemNutrition = 0;

    public EatFood() {
        super(32);
    }

    @Override
    protected void doDelayedAction(E entity) {
        if (this.eatResult != null && this.itemBeingEaten != null && this.eatResult == InteractionResult.CONSUME) {
            // foodItem.shrink(1);
            entity.setHunger(entity.getHunger() + (1000 * this.consumedItemNutrition));
            entity.eatFood(this.itemBeingEaten);
        }

        this.itemBeingEaten = null;
        this.eatResult = null;

        entity.setIsEating(false);
    }

    @Override
    protected void start(E entity) {
        // TODO: sorted by preference
        ItemStack foodItemStack = entity.getInventory().getItems().stream().filter(item -> Dictionaries.FOOD.contains(item.getItem())).findFirst().orElse(null);
        this.consumedItemNutrition = 0;

        if (foodItemStack != null) {
            var consumable = foodItemStack.get(DataComponents.CONSUMABLE);
            var foodComponent = foodItemStack.get(DataComponents.FOOD);

            if (consumable != null) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, foodItemStack);

                if (foodComponent != null) {
                    this.consumedItemNutrition = foodComponent.nutrition();
                }

                this.eatResult = consumable.startConsuming(entity, foodItemStack, InteractionHand.MAIN_HAND);
                this.itemBeingEaten = foodItemStack.getItem();

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
