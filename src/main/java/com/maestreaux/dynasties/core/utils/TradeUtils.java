package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.maestreaux.dynasties.init.ModMealTypes.ALL_RECIPE_INGREDIENTS;

public class TradeUtils {
    public static Map<Item, List<Pair<MarketAgent.TradeOffer, AbstractDynastyVillager>>> getAllAvailableTradeOffersMap(List<AbstractDynastyVillager> otherTraders) {
        var tradeOffersMap = new HashMap<Item, List<Pair<MarketAgent.TradeOffer, AbstractDynastyVillager>>>();

        for (var trader : otherTraders) {
            var agent = trader.asMarketAgent();

            for (var offer : agent.getActiveOffers().values()) {
                tradeOffersMap.get(offer.getItemOffered().getItem()).add(Pair.of(offer, trader));
            }
        }

        return tradeOffersMap;
    }

    public static List<Pair<MarketAgent.TradeOffer, Float>> getDesiredOffers(AbstractDynastyVillager trader, List<AbstractDynastyVillager> otherTraders) {
        // TODO: Evaluate based on trade categories
        var evaluator = trader.asMarketAgent();
        var tradeOffers = new ArrayList<Pair<MarketAgent.TradeOffer, Float>>();

        for (var otherTrader : otherTraders) {
            if (otherTrader == trader) continue;

            var traderAgent = otherTrader.asMarketAgent();
            var activeOffers = traderAgent.getActiveOffers();

            // Evaluate Ingredients
            for (var tradeOffer : activeOffers.entrySet()) {
                var itemOffered = tradeOffer.getKey();

                if (ALL_RECIPE_INGREDIENTS.contains(itemOffered) && !itemOffered.equals(Items.AIR)) {
                    var desirability = MealUtils.getOfferedIngredientDesirability(trader, otherTrader, itemOffered);
                    tradeOffers.add(new Pair<>(tradeOffer.getValue(), desirability));
                }
            }
        }

        return tradeOffers.stream()
                .sorted((offer1, offer2) -> Float.compare(offer2.getSecond(), offer1.getSecond())).collect(Collectors.toList());
    }

}
