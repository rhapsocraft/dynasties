package com.maestreaux.dynasties.core;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ItemLocation {
    public ItemStack stack;
    public IItemHandler itemHandler;
    public BlockEntity blockEntity;
    public int slot;

    public ItemLocation(ItemStack stack, BlockEntity blockEntity, IItemHandler itemHandler, int slot) {
        this.stack = stack;
        this.itemHandler = itemHandler;
        this.slot = slot;
        this.blockEntity = blockEntity;
    }

    public ItemLocation(ItemStack stack, BlockEntity blockEntity, int slot) {
        this(stack, blockEntity, new ItemStackHandler(), slot);
    }
}
