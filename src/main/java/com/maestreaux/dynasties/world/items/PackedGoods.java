package com.maestreaux.dynasties.world.items;

import com.maestreaux.dynasties.core.ItemLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PackedGoods extends Item {
    public PackedGoods(Properties p_41383_) {
        super(p_41383_);
    }

    public static ItemStack packItems(ItemStack pack, ItemStack itemStackToInsert) {
        BundleContents bundlecontents = pack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            BundleContents.Mutable bundlecontents$mutable = new BundleContents.Mutable(bundlecontents);
            bundlecontents$mutable.tryInsert(itemStackToInsert);
        }

        return itemStackToInsert;
    }

    public static boolean unpackItemsInPackLocation(ItemLocation packLocation) {
        var pack = packLocation.stack;
        BundleContents bundlecontents = pack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            BundleContents.Mutable bundlecontents$mutable = new BundleContents.Mutable(bundlecontents);

            int currentSlot = 0;

            ItemStack removedItem;
            while ((removedItem = bundlecontents$mutable.removeOne()) != null && currentSlot < packLocation.itemHandler.getSlots()) {
                while (packLocation.itemHandler.insertItem(currentSlot, removedItem, false) != ItemStack.EMPTY) {
                    ++currentSlot;
                }
            }

            return bundlecontents.isEmpty();
        }

        return false;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack p_150775_) {
        return !p_150775_.has(DataComponents.HIDE_TOOLTIP) && !p_150775_.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) ? Optional.ofNullable(p_150775_.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new) : Optional.empty();
    }
}
