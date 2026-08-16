package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.core.ItemLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class InventoryUtils {
    public static Comparator<ItemStack> nutritionSorter = (o1, o2) -> getNutrition(o2) - getNutrition(o1);

    public static Comparator<ItemStack> getPotentialNutritionSorter(ServerLevel level) {
        return (o1, o2) -> getPotentialNutrition(level, o2) - getPotentialNutrition(level, o1);
    }

    public static Comparator<ItemLocation> itemLocationNutritionSorter = (o1, o2) -> nutritionSorter.compare(o1.stack, o2.stack);
    public static Comparator<ItemLocation> itemLocationPotentialNutritionSorter(ServerLevel level) {
        return (o1, o2) -> getPotentialNutritionSorter(level).compare(o1.stack, o2.stack);
    }

    public static int getNutrition(ItemStack item) {
        var foodProperties = item.get(DataComponents.FOOD);

        if (foodProperties != null) {
            return foodProperties.nutrition();
        }

        return 0;
    }

    public static Map<Item, Integer> getItemCounts(SimpleContainer inventory, Set<Item> itemSet) {
        var itemCountsMap = new HashMap<Item, Integer>();

        inventory.getItems().stream()
                .filter(itemStack -> itemSet.contains(itemStack.getItem()))
                .forEach(itemStack -> {
                    var item = itemStack.getItem();

                    if (itemSet.contains(item)) {
                        itemCountsMap.computeIfAbsent(item, (itemToCompute) -> itemStack.getCount());
                    }
                });

        return itemCountsMap;
    }

    public static Map<Item, Integer> getItemCounts(List<? extends BaseContainerBlockEntity> containers, Set<Item> itemSet) {
        var itemCountsMap = new HashMap<Item, Integer>();

        for (var container: containers) {
            for(int i = 0; i < container.getContainerSize(); ++i) {
                ItemStack itemStack = container.getItem(i);
                var item = itemStack.getItem();

                if (itemSet.contains(item)) {
                    itemCountsMap.computeIfAbsent(item, (itemToCompute) -> itemStack.getCount());
                }

            }
        }

        return itemCountsMap;
    }

    public static int getPotentialNutrition(ServerLevel level, ItemStack item) {
        var recipeInput = new SingleRecipeInput(item);
        var potentialItemRecipe = level.recipeAccess().getRecipeFor(RecipeType.CAMPFIRE_COOKING, recipeInput, level);

        if (potentialItemRecipe.isPresent()) {
            var recipeItem = potentialItemRecipe.get().value().assemble(recipeInput, level.registryAccess());
            return getNutrition(recipeItem);
        } else {
            return 0;
        }
    }

    public static IItemHandler getItemHandler(BaseContainerBlockEntity container) {
        return container.getCapability(ITEM_HANDLER).resolve().orElse(null);
    }

    public static Map<Item, List<ItemLocation>> getItemLocations(List<? extends BaseContainerBlockEntity> containers, Set<Item> itemsToFind) {
        Map<Item, List<ItemLocation>> itemLocationMap = new HashMap<>();

        for(var container: containers) {
            if (!container.hasAnyOf(itemsToFind)) continue;

            var itemHandler = getItemHandler(container);
            if (itemHandler != null) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    var itemStackSlot = itemHandler.getStackInSlot(i);
                    var itemInSlot = itemStackSlot.getItem();

                    if (itemsToFind.contains(itemInSlot)) {
                        itemLocationMap.computeIfAbsent(itemInSlot, (item) -> new ArrayList<>()).add(new ItemLocation(itemStackSlot, container, itemHandler, i));
                    }
                }
            }
        }

        return itemLocationMap;
    }

    public static int getItemSupply(List<? extends BaseContainerBlockEntity> containers, Item item) {
        var itemLocationsMap = getItemLocations(containers, Set.of(item));
        var itemLocations = itemLocationsMap.get(item);

        if (itemLocations != null && !itemLocations.isEmpty()) {
            return itemLocations.stream().mapToInt(itemLocation -> itemLocation.stack.getCount()).sum();
        }

        return 0;
    }
}
