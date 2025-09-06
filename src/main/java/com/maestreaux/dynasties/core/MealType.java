package com.maestreaux.dynasties.core;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class MealType {

    private final float calories;
    private final MealNutrients nutrients;
    private final float baseDesirability;
    private final MealRecipe recipe;

    public MealType(MealNutrients nutrients, MealRecipe recipe, float calories, float baseDesirability) {
        this.nutrients = nutrients;
        this.calories = calories;
        this.baseDesirability = baseDesirability;
        this.recipe = recipe;
    }

    public MealRecipe getRecipe() { return this.recipe; }

    public float getCalories() {
        return calories;
    }

    public float getBaseDesirability() {
        return this.baseDesirability;
    }

    public MealNutrients getNutrients() {
        return this.nutrients;
    }

    public record MealNutrients(float meat, float vegetable, float sugar, float carbohydrates) { }

    public static class MealRecipe {
        private final List<Pair<Item, Integer>> ingredients;

        public MealRecipe(List<Pair<Item, Integer>> ingredients) {
            this.ingredients = ingredients;
        }

        public List<Pair<Item, Integer>> getIngredients() {
            return this.ingredients;
        }
    }
}
