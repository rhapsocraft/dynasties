package com.maestreaux.dynasties.world.items;

import com.maestreaux.dynasties.core.MealType;
import net.minecraft.world.item.Item;

public class Meal extends Item {
    private MealType mealType;

    public Meal(Properties properties) {
        super(properties);
        this.mealType = null;
    }

    public Meal(Properties properties, MealType mealType) {
        this(properties);
        this.mealType = mealType;

        if (this.mealType != null) {
            this.mealType.setItem(this);
        }
    }

    public MealType getMealType() {
        return this.mealType;
    }
}
