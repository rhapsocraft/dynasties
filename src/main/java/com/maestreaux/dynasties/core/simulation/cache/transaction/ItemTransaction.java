package com.maestreaux.dynasties.core.simulation.cache.transaction;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;

import java.util.Objects;

public class ItemTransaction implements ICacheTransaction {

    private final int slot;
    private int quantity;
    private Tag data;

    public ItemTransaction(int slot, int quantity, Tag data) {
        this.slot = slot;
        this.quantity = quantity;
        this.data = data;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public int getSlot() {
        return this.slot;
    }

    public Tag getData() { return this.data; }

    @Override
    public int hashCode() {
        return Objects.hash(this.slot);
    }

    @Override
    public void commit() {

    }

    @Override
    public void transact() {

    }

    public enum ItemTransactionAction {
        TAKE,
        INSERT,
    }
}
