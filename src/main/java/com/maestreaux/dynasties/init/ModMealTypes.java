package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.MealType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModMealTypes {
    public static final DeferredRegister<MealType> MEAL_TYPES = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "meal"), DynastiesMod.MODID);
    public static Supplier<IForgeRegistry<MealType>> MEAL_TYPE_REGISTRY;
    public static Set<Item> ALL_RECIPE_INGREDIENTS;

    public static RegistryObject<MealType> BOILED_POTATOES = MEAL_TYPES.register("boiled_potatoes", () -> new MealType(new MealType.MealNutrients(0, 3,  0, 0 ),
            new MealType.MealRecipe(List.of(Pair.of(Items.POTATO, 3))), 15, 2));
    public static RegistryObject<MealType> BOILED_CARROTS = MEAL_TYPES.register("boiled_carrots", () -> new MealType(new MealType.MealNutrients(0, 3,  0, 0 ),
            new MealType.MealRecipe(List.of(Pair.of(Items.CARROT, 3))), 15, 2));
    public static RegistryObject<MealType> ROOT_VEGETABLE_STEW = MEAL_TYPES.register("root_vegetable_stew", () -> new MealType(new MealType.MealNutrients(0, 3,  0, 0 ),
            new MealType.MealRecipe(List.of(Pair.of(Items.POTATO, 1), Pair.of(Items.CARROT, 1), Pair.of(Items.BEETROOT, 1) )), 15, 4));
    public static RegistryObject<MealType> BEEF_POTATO_STEW = MEAL_TYPES.register("beef_potato_stew", () -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(List.of(Pair.of(Items.BEEF, 1), Pair.of(Items.POTATO, 3))), 30, 6));
    public static RegistryObject<MealType> BEEF_CARROT_STEW = MEAL_TYPES.register("beef_carrot_stew", () -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(List.of(Pair.of(Items.BEEF, 1), Pair.of(Items.CARROT, 3))), 30, 6));
    public static RegistryObject<MealType> PORK_POTATO_STEW = MEAL_TYPES.register("pork_potato_stew", () -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(List.of(Pair.of(Items.PORKCHOP, 1), Pair.of(Items.POTATO, 3))), 30, 6));
    public static RegistryObject<MealType> PORK_CARROT_STEW = MEAL_TYPES.register("pork_carrot_stew", () -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(List.of(Pair.of(Items.PORKCHOP, 1), Pair.of(Items.CARROT, 3))), 30, 6));

    public static void register(IEventBus bus) {
        MEAL_TYPES.register(bus);
        MEAL_TYPE_REGISTRY = MEAL_TYPES.makeRegistry(RegistryBuilder::new);
    }

    public static void initializeMealTypes() {
        ALL_RECIPE_INGREDIENTS = getAllRecipeIngredients();
    }

    public static MealType getMealType(RegistryObject<MealType> registryObject) {
        return MEAL_TYPE_REGISTRY.get().getValue(registryObject.getId());
    }

    public static Collection<MealType> getAllMealTypes() {
        return MEAL_TYPE_REGISTRY.get().getValues();
    }

    public static Set<Item> getAllRecipeIngredients() {
        return getAllMealTypes().stream().flatMap(mealType -> mealType.getRecipe().getIngredients().stream().map(Pair::getFirst)).collect(Collectors.toSet());
    }
}
