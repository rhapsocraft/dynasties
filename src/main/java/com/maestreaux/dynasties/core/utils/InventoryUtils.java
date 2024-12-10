package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.core.ItemLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class InventoryUtils {
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
}
