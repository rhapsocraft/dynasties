package com.maestreaux.dynasties.core;

import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.HashMap;
import java.util.List;
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
    public long valuationsLastUpdated = 0;

    public MarketAgent(AbstractDynastyVillager villager) {
        this.entity = villager;
    }

    public void setMoney(int newMoney) {
        this.money = newMoney;
    }

    public int getMoney() {
        return this.money;
    }

    public Map<Item, Float> getValuations() {
        return this.valuations;
    }

    // LET ME COOK
    public float getAverageValuations() {
        var values = valuations.entrySet();

        return !values.isEmpty() ? values.stream().map(
                entry -> {
                    var weight = MARKETABLE_ITEMS.get(entry.getKey());
                    var valuation = entry.getValue();

                    if (weight == null) {
                        weight = 1F;
                    }

                    return valuation * weight;
                }
        ).reduce(Float::sum).orElse(0F) / values.size() : 1F;
    }

    public int getDesiredSupply(Item item) {
        var itemValuation = this.valuations.get(item);

        if (itemValuation == null) {
            itemValuation = 1.0F;
        }

        return Math.round(32 * itemValuation);
    }

    public int calculateSurplus(List<? extends BaseContainerBlockEntity> containers, Item item) {
        var supply = InventoryUtils.getItemSupply(containers, item);
        var desiredSupply = getDesiredSupply(item);

        return supply - desiredSupply;
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

    public void updateStock(ItemStack itemToSell, int quantity) {
        var offer = this.activeOffers.computeIfAbsent(itemToSell.getItem(), (item) -> new MarketAgent.TradeOffer(this, itemToSell));
        offer.setItemOffered(itemToSell);
        offer.setQuantity(offer.quantityOffered + quantity);
        this.entity.updateTradeOffers();
    }

    public void removeOffer(Item item) {
        this.activeOffers.remove(item);
        this.entity.updateTradeOffers();
    }

    public void updateOffer(ItemStack stack, int quantity) {
        var item = stack.getItem();
        this.adjustValuation(item);

        var itemValuation = valuations.get(item);
        var price = (int) Math.ceil(itemValuation * BASE_PRICE);

        var activeOffer = this.activeOffers.get(item);

        if (activeOffer != null) {
            activeOffer.setPrice(price);
            activeOffer.setQuantity(quantity);
        } else {
            this.activeOffers.put(item, new TradeOffer(this, stack, quantity, price));
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
        public static StreamCodec<RegistryFriendlyByteBuf, TradeOffer> STREAM_CODEC;

        private MarketAgent agent;
        private ItemStack itemOffered;
        private int quantityOffered;
        private int quantitySold = 0;
        private int priceEach;

        public TradeOffer(ItemStack itemStackOffered, int quantity, int quantitySold, int price) {
            this.itemOffered = itemStackOffered;
            this.quantityOffered = quantity;
            this.priceEach = price;
            this.quantitySold = quantitySold;
        }

        public TradeOffer(MarketAgent agent, ItemStack itemOffered, int quantity, int price) {
            this(itemOffered, quantity, 0, price);
            this.agent = agent;

            this.agent.valuations.computeIfAbsent(itemOffered.getItem(), (itemToValue) -> MARKETABLE_ITEMS.get(itemToValue) * this.agent.getAverageValuations());

        }

        public TradeOffer(MarketAgent agent, ItemStack itemOffered) {
            this(agent, itemOffered, itemOffered.getCount(), BASE_PRICE);
        }

        public ItemStack getItemOffered() {
            return this.itemOffered;
        }

        public int getQuantityOffered() {
            return this.quantityOffered;
        }

        public int getQuantitySold() {
            return this.quantitySold;
        }

        public void setItemOffered(ItemStack itemStack) {
            this.itemOffered = itemStack;
        }

        public AbstractDynastyVillager getEntity() {
            return this.agent.entity;
        }

        public int getStock() {
            return itemOffered.getCount();
        }

        public int getPrice() {
            return this.priceEach;
        }

        public void setPrice(int newPrice) {
            this.priceEach = newPrice;
            this.agent.entity.updateTradeOffers();
        }

        public void setQuantity(int quantity) {
            this.quantityOffered = quantity;
            this.agent.entity.updateTradeOffers();
        }

        public ItemStack sell(int quantity) {
            int actualQuantity = Math.min(this.getStock(), quantity);

            if (actualQuantity > 0) {
                this.quantitySold += actualQuantity;

                var sale = this.getPrice() * actualQuantity;
                var newBalance = this.agent.getMoney() + sale;
                this.agent.setMoney(newBalance);

                var entity = this.getEntity();
                var item = this.itemOffered.getItem();

                // remove items from slot
                var itemStackToSell = this.itemOffered.copy();
                itemStackToSell.setCount(actualQuantity);

                this.itemOffered.shrink(actualQuantity);

                this.agent.valuations.compute(item, (itemToValue, currentValuation) -> {
                    var valuation = currentValuation == null ? MARKETABLE_ITEMS.get(itemToValue) * this.agent.getAverageValuations() : currentValuation;
                    return valuation * (1.0F + (0.005F * actualQuantity));
                });

                entity.updateTradeOffers();

                // TODO: TEMPORARY
                this.setPrice(Math.round(this.agent.valuations.get(item) * BASE_PRICE));

                return itemStackToSell;
            }

            return null;
        }



        static {
            STREAM_CODEC = StreamCodec.composite(
                    ItemStack.STREAM_CODEC, (offer) -> offer.itemOffered,
                    ByteBufCodecs.INT, (offer) -> offer.quantityOffered,
                    ByteBufCodecs.INT, (offer) -> offer.quantitySold,
                    ByteBufCodecs.INT, (offer) -> offer.priceEach,
                    TradeOffer::new);
        }
    }
}
