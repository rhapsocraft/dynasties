package com.maestreaux.dynasties.core;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class MarketAgent {
    public static Map<Item, Float> MARKETABLE_ITEMS = Map.of(
            Items.POTATO, 1F,
            Items.CARROT, 1F,
            Items.WHEAT, 1F,
            Items.BREAD, 1.5F,
            Items.BEETROOT, 0.5F,
            Items.BAKED_POTATO, 1.5F
    );

    public static int BASE_PRICE = 10;

    private final Map<Item, Float> valuations = new HashMap<>();
    private final Map<Item, TradeOffer> activeOffers = new HashMap<>();
    private final AbstractDynastyVillager entity;
    private int money = 200;
    private float foodBudget = 0.5F;

    public MarketAgent(AbstractDynastyVillager villager) {
        this.entity = villager;
    }

    public void setMoney(int newMoney) {
        this.money = newMoney;
    }

    public int getMoney() {
        return this.money;
    }

    // LET ME COOK
    public float getAverageValuations() {
        var values = valuations.entrySet();

        return !values.isEmpty() ? values.stream().map(
                entry -> {
                    var weight = MARKETABLE_ITEMS.get(entry.getKey());
                    var valuation = entry.getValue();

                    return valuation * weight;
                }
        ).reduce(Float::sum).orElse(0F) / values.size() : 1F;
    }

    public void purchaseFrom(TradeOffer offer, int quantity) {
        var quantityToBuy = Math.min(offer.getStock(), quantity);
        var cost = offer.getPrice() * quantityToBuy;

        if (this.money > cost) {
            offer.sell(quantity);

            var newBalance = this.money - cost;
            this.setMoney(newBalance);
        }
    }

    public void updateOffer(Item item, int quantity) {
        this.adjustValuation(item);

        var itemValuation = valuations.get(item);
        var price = (int) Math.ceil(itemValuation * BASE_PRICE);

        var activeOffer = this.activeOffers.get(item);

        if (activeOffer != null) {
            activeOffer.setPrice(price);
            activeOffer.setQuantity(quantity);
        } else {
            this.activeOffers.put(item, new TradeOffer(this, item, quantity, price));
        }
    }

    public Map<Item, TradeOffer> getActiveOffers() {
        return this.activeOffers;
    }

    public void adjustValuation(Item item) {
        var activeItemOffer = this.activeOffers.get(item);

        if (activeItemOffer != null) {
            var percentSold = activeItemOffer.getQuantitySold() / activeItemOffer.getQuantityOffered();
            float currentValuation = this.valuations.computeIfAbsent(item, (itemToValue) -> MARKETABLE_ITEMS.get(itemToValue) * this.getAverageValuations());

            if (percentSold < 0.33) {
                this.valuations.put(item, currentValuation * 0.9F);
            } else if (percentSold > 0.66) {
                this.valuations.put(item, currentValuation * 1.1F);
            }
        }
    }

    // Speculate potential profits from producing and selling an item
    // public float speculate() {}

    public static class TradeOffer {
        private final MarketAgent agent;
        private final Item itemOffered;
        private int quantityOffered;
        private int quantitySold = 0;
        private int priceEach;

        public TradeOffer(MarketAgent agent, Item itemOffered, int quantity, int price) {
            this.agent = agent;
            this.itemOffered = itemOffered;
            this.quantityOffered = quantity;
            this.priceEach = price;
        }

        public TradeOffer(MarketAgent agent, Item itemOffered, int quantity) {
            this(agent, itemOffered, quantity, BASE_PRICE);
        }
        public Item getItemOffered() {
            return this.itemOffered;
        }

        public int getQuantityOffered() {
            return this.quantityOffered;
        }

        public int getQuantitySold() {
            return this.quantitySold;
        }

        public int getStock() {
            return this.quantityOffered - this.quantitySold;
        }

        public int getPrice() {
            return this.priceEach;
        }

        public void setPrice(int newPrice) {
            this.priceEach = newPrice;
        }

        public void setQuantity(int quantity) {
            this.quantityOffered = quantity;
        }

        public int sell(int quantity) {
            int actualQuantity = Math.min(this.getStock(), quantity);
            this.quantitySold -= actualQuantity;

            var sale = this.getPrice() * actualQuantity;
            var newBalance = this.agent.getMoney() + sale;
            this.agent.setMoney(newBalance);

            return actualQuantity;
        }
    }
}
