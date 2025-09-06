package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.MealType;
import com.maestreaux.dynasties.core.simulation.SimulatedVillagerEntity;
import com.maestreaux.dynasties.init.ModMealTypes;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.world.item.Item;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.*;
import java.util.stream.Collectors;

public class MealUtils {
    private static boolean hasIngredient(MealType mealType, Item ingredient) {
        return mealType.getRecipe().getIngredients().keySet().stream().anyMatch(item -> item.equals(ingredient));
    }

    private static Map<Item, Integer> getIngredientsCountInPantryMap(List<ItemLocation> pantryItems) {
        return pantryItems.stream()
                .collect(Collectors.toMap(itemLocation -> itemLocation.stack.getItem(),
                        itemLocation -> itemLocation.stack.getCount(),
                        Integer::sum)
                );
    }

    private static boolean hasEnoughIngredients(MealType mealType, Item ingredient, int ingredientCount) {
        return mealType.getRecipe().getIngredients().entrySet().stream().anyMatch(entry -> entry.getKey() == ingredient && entry.getValue() <= ingredientCount);
    }

    private static float getCravingScore(SimulatedVillagerEntity villager, MealType mealType) {
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
                var sellerAgent = seller.getSimEntity().asMarketAgent();

                for (var mealType : mealTypesWithIngredient) {
                    var ingredientsSet = new HashSet<>(mealType.getRecipe().getIngredients().keySet());
                    var pantryItems = InventoryUtils.getItemLocations(homeContainers, ingredientsSet).values().stream().flatMap(Collection::stream).toList();

                    var availableIngredientsCount = getIngredientsCountInPantryMap(pantryItems);
                    var ingredientsInInventory = buyer.getInventory().getItems().stream().filter(item -> ingredientsSet.contains(item.getItem())).toList();
                    for (var ingredientInInventory : ingredientsInInventory) {
                        var ingredientItem = ingredientInInventory.getItem();
                        availableIngredientsCount.put(ingredientItem, availableIngredientsCount.computeIfAbsent(ingredientItem, ing -> 0) + ingredientInInventory.getCount());
                    }

                    var price = sellerAgent.getActiveOffers().get(ingredient).getPrice();
                    var recipeIngredient = mealType.getRecipe().getIngredients().entrySet().stream().filter(entry -> entry.getKey().equals(ingredient)).findFirst().orElse(null);

                    assert recipeIngredient != null;
                    // Meal's desirability is improved if fewer ingredients are needed to be bought
                    var availableIngredients = availableIngredientsCount.computeIfAbsent(ingredient, ing -> 0);
                    var neededIngredientsForMeal = Math.max(recipeIngredient.getValue() - availableIngredients, 0);

                    // TODO: this should be handled in a different method
                    var buyerAgent = buyer.getSimEntity().asMarketAgent();
                    var itemDesiredSupply = Math.max(buyerAgent.getDesiredSupply(ingredient) - availableIngredients, 0);

                    if (neededIngredientsForMeal > 0) {
                        float cost = neededIngredientsForMeal * price;
                        if (cost < 0.1F) cost = 0.1F;

                        desirabilityRating += getMealDesirability(buyer, mealType) / cost;
                    } else if (itemDesiredSupply > 0) {
                        desirabilityRating += (float) 1 / price;
                    }
                }

                // return desirabilityRating / mealTypesWithIngredient.size();
                return desirabilityRating / ModMealTypes.getAllMealTypes().size();
            }
        }


        return desirabilityRating;
    }

    public static float getMealDesirability(AbstractDynastyVillager cook, MealType mealType) {
        // TODO: get family's/household's craving?
        var mealSurfeit = cook.getSimEntity().getStomach().getSurfeitFactor(mealType, cook.level().getGameTime());
        var cravingScore = getCravingScore(cook.getSimEntity(), mealType);
        var normalizedCalories = mealType.getCalories() / 10.0F;

        return mealType.getBaseDesirability() * normalizedCalories * (1 + cravingScore) * mealSurfeit;
    }

    public static boolean canCookMeal(AbstractDynastyVillager cook, MealType mealType, List<ItemLocation> pantryItems) {
        var availableIngredients = getIngredientsCountInPantryMap(pantryItems);
        var canCook = true;

        for (var ingredient: mealType.getRecipe().getIngredients().entrySet()) {
            var availableIngredientCount = availableIngredients.computeIfAbsent(ingredient.getKey(), (item) -> 0);

            if (availableIngredientCount < ingredient.getValue()) {
                canCook = false;
                break;
            }
        }

        return canCook;
    }

    public static List<ItemLocation> getAvailableIngredientsForMeal(AbstractDynastyVillager cook, MealType mealType) {
        var homeContainers = BrainUtil.getMemory(cook, ModMemoryTypes.HOME_CONTAINERS.get());

        if (homeContainers != null) {
            var ingredients = mealType.getRecipe().getIngredients().keySet();
            var pantryItems = InventoryUtils.getItemLocations(homeContainers, ingredients).values().stream().flatMap(Collection::stream).toList();

            if (canCookMeal(cook, mealType, pantryItems)) {
                return pantryItems;
            }
        }

        return null;
    }

    public static MealType getBestAvailableMeal(AbstractDynastyVillager cook) {
        var mealTypes = ModMealTypes.getAllMealTypes().stream().filter(Objects::nonNull).sorted((m1, m2) -> Float.compare(getMealDesirability(cook, m2), getMealDesirability(cook, m1))).toList();
        var homeContainers = BrainUtil.getMemory(cook, ModMemoryTypes.HOME_CONTAINERS.get());

        if (homeContainers != null) {
            var pantryItems = InventoryUtils.getItemLocations(homeContainers, ModMealTypes.ALL_RECIPE_INGREDIENTS).values().stream().flatMap(Collection::stream).toList();

            for (var mealType : mealTypes) {
                if (canCookMeal(cook, mealType, pantryItems)) {
                    return mealType;
                }
            }
        }

        return null;
    }
}
