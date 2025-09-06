package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class StockWares<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    private static final Set<Item> WARES = Set.of(Items.WHEAT, Items.POTATO, Items.CARROT, Items.BEETROOT, Items.PORKCHOP);

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    protected void start(E entity) {
        var containers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());

        var marketAgent = entity.asMarketAgent();
        var activeOffers = marketAgent.getActiveOffers();

        var tradeSlot = entity.getTradeSlot();
        var tradeInventory = entity.getTradeInventory();

        if (containers != null) {
            var itemLocationsMap = InventoryUtils.getItemLocations(containers, WARES);

            // Find all locations of sellable items
            for (var itemToSell : itemLocationsMap.keySet()) {
                var itemLocations = itemLocationsMap.get(itemToSell);

                // See if we should sell items
                for (var itemLocation : itemLocations) {
                    var itemInLocation = itemLocation.stack.getItem();
                    var canAddItem = tradeInventory.canAddItem(itemLocation.stack);

                    if (canAddItem && itemLocation.stack != ItemStack.EMPTY) {
                        int itemSlot = tradeSlot.computeIfAbsent(itemInLocation, (itemToTrade) -> IntStream.range(0, tradeInventory.getContainerSize()).filter(slot -> tradeInventory.getItem(slot) == ItemStack.EMPTY).findFirst().orElse(-1));

                        if (itemSlot != -1) {
                            var itemStackInSlot = tradeInventory.getItem(itemSlot);
                            var stackSizeRemaining = itemStackInSlot == ItemStack.EMPTY ? itemToSell.getDefaultMaxStackSize() : itemStackInSlot.getMaxStackSize() - itemStackInSlot.getCount();

                            // We have no storage for this item. Evaluate next item
                            if (stackSizeRemaining == 0) {
                                break;
                            }

                            var surplus = Math.min(Math.max((entity.asMarketAgent().calculateSurplus(containers, itemInLocation)) - itemStackInSlot.getCount(), 0), itemLocation.stack.getCount());
                            var amountToAdd = Math.min(stackSizeRemaining, surplus);

                            var itemStackToAdd = itemLocation.itemHandler.extractItem(itemLocation.slot, amountToAdd, false);

                            if (itemStackToAdd != ItemStack.EMPTY) {
                                if (itemStackInSlot.getItem() != itemToSell) {
                                    tradeInventory.setItem(itemSlot, itemStackToAdd);
                                } else {
                                    itemStackInSlot.grow(amountToAdd);
                                }

                                marketAgent.updateStock(tradeInventory.getItem(itemSlot), amountToAdd);
                            }

                            // activeItemOffer.setQuantity(activeItemOffer.getQuantityOffered() + amountToAdd);
                        }
                    } else {
                        // We have no storage for this item. Evaluate next item
                        break;
                    }
                }
            }
        }
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_CONTAINERS.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
