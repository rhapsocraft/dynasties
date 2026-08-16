package com.maestreaux.dynasties.core.simulation.cache.inventory;

import com.maestreaux.dynasties.core.simulation.cache.ICacheItem;
import com.maestreaux.dynasties.core.simulation.cache.transaction.ICacheTransactionHolder;
import com.maestreaux.dynasties.core.simulation.cache.transaction.ItemTransaction;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.util.*;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class InventoryCacheItem implements ICacheItem, ICacheTransactionHolder<ItemTransaction> {
    private final ServerLevel level;
    private final BlockPos pos;

    private final Map<Integer, Pair<Item, Tag>> slotItems = new HashMap<>();

    private final Queue<ItemTransaction> transactionsQueue = new LinkedList<>();

    public InventoryCacheItem(BlockPos pos, ServerLevel level) {
        this.pos = pos;
        this.level = level;
    }

    private BaseContainerBlockEntity getContainer() {
        var blockEntity = this.level.getBlockEntity(this.pos);

        if (blockEntity instanceof BaseContainerBlockEntity container) {
            return container;
        }

        return null;
    }

    private boolean cacheInventory() {
        var container = this.getContainer();

        if (container != null) {
            var inventory = container.getCapability(ITEM_HANDLER).resolve().orElse(null);

            if (inventory != null) {
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    var itemInSlot = inventory.getStackInSlot(slot);
                    this.slotItems.put(slot, Pair.of(itemInSlot.getItem(), itemInSlot.save(this.level.registryAccess())));
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean flush() {
        return this.flushTransactions();
    }

    @Override
    public boolean cache() {
        return this.cacheInventory();
    }

    @Override
    public boolean isLoaded() {
        return this.level.hasChunk(this.pos.getX(), this.pos.getY());
    }

    @Override
    public List<ItemTransaction> getTransactions() {
        return this.transactionsQueue.stream().toList();
    }

    @Override
    public void insertTransaction(ItemTransaction cacheTransaction) {
        this.transactionsQueue.offer(cacheTransaction);
    }

    @Override
    public boolean flushTransactions() {
        var container = this.getContainer();

        if (container != null) {
            var inventory = container.getCapability(ITEM_HANDLER).resolve().orElse(null);

            if (inventory != null) {
                while(!transactionsQueue.isEmpty()) {
                    var transaction = transactionsQueue.poll();

                    if (transaction != null) {
                        var itemstack = inventory.getStackInSlot(transaction.getSlot());

                        if (itemstack.isEmpty() && transaction.getQuantity() != 0) {
                            // Load item data from transaction
                            var newItemStack = ItemStack.parse(this.level.registryAccess(), transaction.getData()).orElse(ItemStack.EMPTY);;
                            newItemStack.setCount(transaction.getQuantity());

                            inventory.insertItem(transaction.getSlot(), newItemStack, false);
                        } else {
                            itemstack.setCount(transaction.getQuantity());
                        }

                        this.slotItems.remove(transaction.getSlot());
                    }
                }

                return true;
            }
        }

        return false;
    }
}
