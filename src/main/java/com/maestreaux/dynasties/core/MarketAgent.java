package com.maestreaux.dynasties.core;

import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModItems;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketAgent {
    public static Map<Item, Float> MARKETABLE_ITEMS = Map.of(
            ModItems.COIN.get(), 1F,
            Items.POTATO, 5F,
            Items.CARROT, 5F,
            Items.WHEAT, 5F,
            Items.BREAD, 7.5F,
            Items.BEETROOT, 5F,
            Items.BAKED_POTATO, 7.5F,
            Items.PORKCHOP, 7.5F
    );

    private Map<Item, Float> valuations = new HashMap<>() {{ put(ModItems.COIN.get(), MARKETABLE_ITEMS.get(ModItems.COIN.get())); }};
    private final Map<Item, TradeOffer> activeOffers = new HashMap<>();
    private final AbstractDynastyVillager entity;

    // DEBUG
    private int money = 300;

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

    public int getItemPrice(Item item) {
        var currency = this.getCurrency();
        return Math.round(this.valuations.computeIfAbsent(item, this::extrapolateValuation) * this.valuations.get(currency) * Dictionaries.BASE_PRICE);
    }

    public Item getCurrency() {
        // TODO: May Change
        return ModItems.COIN.get();
    }

    public Map<Item, Float> getValuations() {
        return this.valuations;
    }
    public void setValuations(Map<Item, Float> newValuations) {
        this.valuations = newValuations;
    }

    // LET ME COOK
    public float getAverageValuations() {
        var values = valuations.values();

        return !values.isEmpty() ? values.stream().reduce(Float::sum).orElse(0F) / values.size() : 1F;
    }

    public int getDesiredSupply(Item item) {
        var itemValuation = this.valuations.get(item);

        if (itemValuation == null) {
            itemValuation = 1.0F;
        }

        return Math.round((float) (Dictionaries.BASE_DESIRED_SUPPLY * Math.log(itemValuation)));
    }

    public void resetValuations() {
        this.valuations = new HashMap<>() {{ put(ModItems.COIN.get(), MARKETABLE_ITEMS.get(ModItems.COIN.get())); }};
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
        var price = (int) Math.ceil(itemValuation * Dictionaries.BASE_PRICE);

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

    public ItemStack buyOffer(TradeOffer offer, int quantityBought) {
        var itemBought = offer.sell(quantityBought);

        if (itemBought != null) {
            this.updateValuationsOfExchange(itemBought.getItem(), this.getCurrency(), quantityBought, offer.priceEach);

            return itemBought;
        }

        return null;
    }

    public void adjustValuation(Item item) {
        var activeItemOffer = this.activeOffers.get(item);

        if (activeItemOffer != null) {
            var percentSold = activeItemOffer.getQuantitySold() / activeItemOffer.getQuantityOffered();
            float currentValuation = this.valuations.computeIfAbsent(item, this::extrapolateValuation);

            if (percentSold < 0.33) {
                this.valuations.put(item, currentValuation * 0.9F);
            } else if (percentSold > 0.66) {
                this.valuations.put(item, currentValuation * 1.1F);
            }
        }
    }

    public float extrapolateValuation(Item item) {
        return (MARKETABLE_ITEMS.get(item) + this.getAverageValuations()) / 2;
    }

    public void updateValuationsOfExchange(Item itemReceived, Item itemGiven, int quantityReceived, int quantityGiven) {
        this.valuations.compute(itemGiven, (itemToValue, currentValuation) -> {
            var valuation = currentValuation == null ? extrapolateValuation(itemToValue) : currentValuation;
            return valuation + (0.01F * quantityGiven);
        });

        this.valuations.compute(itemReceived, (itemToValue, currentValuation) -> {
            var valuation = currentValuation == null ? extrapolateValuation(itemToValue) : currentValuation;
            return valuation - (0.01F * quantityReceived);
        });
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

            this.agent.valuations.computeIfAbsent(itemOffered.getItem(), (itemToValue) -> this.agent.extrapolateValuation(itemToValue));

        }

        public TradeOffer(MarketAgent agent, ItemStack itemOffered) {
            this(agent, itemOffered, itemOffered.getCount(), agent.getItemPrice(itemOffered.getItem()));
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
            int quantitySold = Math.min(this.getStock(), quantity);

            if (quantitySold > 0) {
                this.quantitySold += quantitySold;

                var sale = this.getPrice() * quantitySold;
                var newBalance = this.agent.getMoney() + sale;
                this.agent.setMoney(newBalance);

                var entity = this.getEntity();
                var itemSold = this.itemOffered.getItem();

                // remove items from slot
                var itemStackToSell = this.itemOffered.copy();
                itemStackToSell.setCount(quantitySold);

                this.itemOffered.shrink(quantitySold);

                this.agent.updateValuationsOfExchange(this.agent.getCurrency(), itemSold, this.priceEach, quantitySold);

                entity.updateTradeOffers();

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
