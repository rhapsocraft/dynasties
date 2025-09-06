package com.maestreaux.dynasties.core;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class MealType {

    private final float calories;
    private final MealNutrients nutrients;
    private final float baseDesirability;
    private final MealRecipe recipe;
    private Item item;
    private final int servings;

    public MealType(MealNutrients nutrients, MealRecipe recipe, int calories, float baseDesirability, int servings) {
        this.nutrients = nutrients;
        this.calories = calories;
        this.baseDesirability = baseDesirability;
        this.recipe = recipe;
        this.servings = servings;
    }

    public MealRecipe getRecipe() { return this.recipe; }

    public int getServings() {
        return this.servings;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return this.item;
    }

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
        private final Map<Item, Integer> ingredients;

        public MealRecipe(Map<Item, Integer> ingredients) {
            this.ingredients = ingredients;
        }

        public Map<Item, Integer> getIngredients() {
            return this.ingredients;
        }
    }
}
