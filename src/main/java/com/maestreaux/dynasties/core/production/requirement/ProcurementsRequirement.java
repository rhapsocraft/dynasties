package com.maestreaux.dynasties.core.production.requirement;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.*;

// Makes sure producer has items needed to produce, if not, attempt to procure them
public class ProcurementsRequirement implements Production.IRequirements {
    private final Map<Item, Integer> productionRecipeMap;

    public ProcurementsRequirement(Map<Item, Integer> productionRecipeMap) {
        this.productionRecipeMap = productionRecipeMap;
    }

    @Override
    public float getCost(AbstractDynastyVillager villager) {
        float cost = 0.0F;
        var valuations = villager.asMarketAgent().getValuations();

        for (var itemCount : this.productionRecipeMap.entrySet()) {
            cost += valuations.get(itemCount.getKey()) * itemCount.getValue();
        }

        return cost;
    }

    @Override
    public boolean fulfillsRequirements(AbstractDynastyVillager villager) {
        var inventory = villager.getInventory();
        var itemCountsInInventory = InventoryUtils.getItemCounts(inventory, this.productionRecipeMap.keySet());
        var fulfillsRequirements = !itemCountsInInventory.isEmpty();

        for (var itemCount : itemCountsInInventory.entrySet()) {
            var neededItemCount = this.productionRecipeMap.get(itemCount.getKey());

            if (itemCount.getValue() < neededItemCount) {
                fulfillsRequirements = false;
                break;
            }
        }

        return fulfillsRequirements;
    }

    @Override
    public boolean canFulfillRequirements(AbstractDynastyVillager villager) {
        var containers = BrainUtil.getMemory(villager, ModMemoryTypes.HOME_CONTAINERS.get());
        var canAttempt = true;

        if (containers != null) {
            var itemCountsInContainers = InventoryUtils.getItemCounts(containers, this.productionRecipeMap.keySet());

            for (var recipeItemCount : this.productionRecipeMap.entrySet()) {
                var neededItemCount = itemCountsInContainers.get(recipeItemCount.getKey());

                if (neededItemCount == null || neededItemCount < recipeItemCount.getValue()) {
                    canAttempt = false;
                    break;
                }
            }
        } else {
            return false;
        }

        return canAttempt;
    }

    @Override
    public void tryFulfillRequirements(Production.ActionDataset dataset, AbstractDynastyVillager villager) {
        var containers = BrainUtil.getMemory(villager, ModMemoryTypes.HOME_CONTAINERS.get());

        // TODO: Doesn't work yet. Use Key-Value pair
        //var procurementDataset = (ProcurementActionDataset) dataset;

        if (containers != null) {
            var itemLocations = InventoryUtils.getItemLocations(containers, this.productionRecipeMap.keySet());
            var inventory = villager.getInventory();

            for (var set : this.productionRecipeMap.entrySet()) {
                int remaining = set.getValue();

                while (remaining > 0) {
                    var itemsInContainers = itemLocations.get(set.getKey());

                    for (var itemLocation : itemsInContainers) {
                        var stack = itemLocation.getStack();
                        var toRemove = Math.min(stack.getCount(), remaining);

                        // TODO: What happens if villager somehow has full inventory?
                        var extractedItems = itemLocation.itemHandler.extractItem(itemLocation.slot, toRemove, false);
                        inventory.addItem(extractedItems);

                        remaining -= toRemove;

                        if (remaining == 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void collectCost(AbstractDynastyVillager villager) {
        var inventory = villager.getInventory();
        var itemSlotsMap = new HashMap<Item, List<ItemStack>>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var itemStack = inventory.getItem(i);

            itemSlotsMap.computeIfAbsent(itemStack.getItem(), (item) -> new ArrayList<>()).add(itemStack);
        }

        for (var set : this.productionRecipeMap.entrySet()) {
            int remaining = set.getValue();
            var relevantItems = itemSlotsMap.get(set.getKey());

            for (var itemStack : relevantItems) {
                var toRemove = Math.min(itemStack.getCount(), remaining);
                itemStack.shrink(toRemove);

                remaining -= toRemove;
            }
        }
    }

    public static class ProcurementActionDataset extends Production.ActionDataset {
        BlockPos containerTargetPos;
    }
}
