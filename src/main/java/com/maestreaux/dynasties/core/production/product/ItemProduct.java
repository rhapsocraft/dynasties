package com.maestreaux.dynasties.core.production.product;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemProduct implements Production.IProduct<Item> {
    private Item item;
    private int quantity;

    public ItemProduct(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getProduct() {
        return this.item;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void produce(AbstractDynastyVillager villager) {
        villager.getInventory().addItem(new ItemStack(this.item, this.quantity));
    }

    public float getGains(AbstractDynastyVillager villager) {
        var marketAgent = villager.getSimEntity().asMarketAgent();

        var valuation = marketAgent.getValuations().get(this.item);

        return valuation * this.quantity;
    }
}