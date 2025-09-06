package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.MealType;
import com.maestreaux.dynasties.init.ModMealTypes;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Item;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MealUtils {
    private static boolean hasIngredient(MealType mealType, Item ingredient) {
        return mealType.getRecipe().getIngredients().stream().anyMatch(pair -> pair.getFirst().equals(ingredient));
    }

    private static Map<Item, Integer> getIngredientsCountInPantryMap(List<ItemLocation> pantryItems) {
        return pantryItems.stream()
                .collect(Collectors.toMap(itemLocation -> itemLocation.stack.getItem(),
                        itemLocation -> itemLocation.stack.getCount(),
                        Integer::sum)
                );
    }

    private static boolean hasEnoughIngredients(MealType mealType, Item ingredient, int ingredientCount) {
        return mealType.getRecipe().getIngredients().stream().anyMatch(pair -> pair.getFirst() == ingredient && pair.getSecond() <= ingredientCount);
    }

    private static float getCravingScore(AbstractDynastyVillager villager, MealType mealType) {
        var stomach = villager.getStomach();
        var stomachNutrients = stomach.calculateNutrition(villager.level().getGameTime());
        var mealNutrients = mealType.getNutrients();

        var sumCarbs = stomachNutrients.carbohydrates + mealNutrients.carbohydrates();
        var sumMeat = stomachNutrients.meat + mealNutrients.meat();
        var sumSugar = stomachNutrients.sugar + mealNutrients.sugar();
        var sumVegetables = stomachNutrients.vegetable + mealNutrients.vegetable();
        var sumNutrition = (sumCarbs + sumMeat + sumSugar + sumVegetables);

        var avgNutrition = sumNutrition / 4;
        float balanceScore = Math.abs(sumCarbs - avgNutrition) + Math.abs(sumMeat - avgNutrition) + Math.abs(sumSugar - avgNutrition) + Math.abs(sumVegetables - avgNutrition);

        if (sumNutrition > 0) {
            return 1F - (balanceScore / (1.5F * sumNutrition));
        } else {
            return 1F;
        }
        // 1.5 is the theoretical maximum imbalance if # of nutrients is 4
    }

    public static float getOfferedIngredientDesirability(AbstractDynastyVillager buyer, AbstractDynastyVillager seller, Item ingredient) {
        var homeContainers = BrainUtil.getMemory(buyer, ModMemoryTypes.HOME_CONTAINERS.get());

        float desirabilityRating = 0F;

        if (homeContainers != null) {

            var mealTypesWithIngredient = ModMealTypes.getAllMealTypes().stream().filter(mealType -> hasIngredient(mealType, ingredient)).toList();

            if (!mealTypesWithIngredient.isEmpty()) {
                var sellerAgent = seller.asMarketAgent();

                for (var mealType : mealTypesWithIngredient) {
                    var ingredientsSet = mealType.getRecipe().getIngredients().stream().map(Pair::getFirst).collect(Collectors.toSet());
                    var pantryItems = InventoryUtils.getItemLocations(homeContainers, ingredientsSet).values().stream().flatMap(Collection::stream).toList();
                    var mealSurfeit = buyer.getStomach().getSurfeit(mealType);

                    var availableIngredientsCount = getIngredientsCountInPantryMap(pantryItems);
                    var ingredientsInInventory = buyer.getInventory().getItems().stream().filter(item -> ingredientsSet.contains(item.getItem())).toList();
                    for (var ingredientInInventory : ingredientsInInventory) {
                        var ingredientItem = ingredientInInventory.getItem();
                        availableIngredientsCount.put(ingredientItem, availableIngredientsCount.get(ingredientItem) + ingredientInInventory.getCount());
                    }

                    var price = sellerAgent.getActiveOffers().get(ingredient).getPrice();
                    var recipeIngredient = mealType.getRecipe().getIngredients().stream().filter(pair -> pair.getFirst() == ingredient).findFirst().orElse(null);

                    // TODO: get entire family's craving score?
                    var cravingScore = getCravingScore(buyer, mealType);

                    assert recipeIngredient != null;
                    // Meal's desirability is improved if fewer ingredients are needed to be bought
                    var neededIngredientsForMeal = Math.max(recipeIngredient.getSecond() - availableIngredientsCount.get(ingredient), 0);

                    float cost = neededIngredientsForMeal * price;
                    if (cost < 0.1F) cost = 0.1F;

                    desirabilityRating += (mealType.getBaseDesirability() * mealType.getCalories() * cravingScore * mealSurfeit) / cost;
                }

                // return desirabilityRating / mealTypesWithIngredient.size();
                return desirabilityRating / ModMealTypes.getAllMealTypes().size();
            }
        }


        return desirabilityRating;
    }
}
