package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.MealType;
import com.maestreaux.dynasties.world.items.Meal;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModMealTypes {
    public static final DeferredRegister<MealType> MEAL_TYPES = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "meal"), DynastiesMod.MODID);
    public static Supplier<IForgeRegistry<MealType>> MEAL_TYPE_REGISTRY;
    public static Set<Item> ALL_RECIPE_INGREDIENTS;

    public static RegistryObject<MealType> BOILED_POTATOES = registerMealType("boiled_potatoes", (name) -> new MealType(new MealType.MealNutrients(0, 3,  0, 0 ),
            new MealType.MealRecipe(Map.of(Items.POTATO, 3)), 15, 2, 5));
    public static RegistryObject<MealType> BOILED_CARROTS = registerMealType("boiled_carrots", (name) -> new MealType(new MealType.MealNutrients(0, 3,  0, 0 ),
            new MealType.MealRecipe(Map.of(Items.CARROT, 3)), 15, 2,5 ));
    public static RegistryObject<MealType> ROOT_VEGETABLE_STEW = registerMealType("root_vegetable_stew", (name) -> new MealType(new MealType.MealNutrients(0, 3,  0, 0 ),
            new MealType.MealRecipe(Map.of(Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1)), 15, 4, 5));
    public static RegistryObject<MealType> BEEF_POTATO_STEW = registerMealType("beef_potato_stew", (name) -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(Map.of(Items.BEEF, 1, Items.POTATO, 3)), 30, 6, 5));
    public static RegistryObject<MealType> BEEF_CARROT_STEW = registerMealType("beef_carrot_stew", (name) -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(Map.of(Items.BEEF, 1, Items.CARROT, 3)), 30, 6, 5));
    public static RegistryObject<MealType> PORK_POTATO_STEW =registerMealType("pork_potato_stew", (name) -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(Map.of(Items.PORKCHOP, 1, Items.POTATO, 3)), 30, 6, 5));
    public static RegistryObject<MealType> PORK_CARROT_STEW = registerMealType("pork_carrot_stew", (name) -> new MealType(new MealType.MealNutrients(2,3, 0, 0),
            new MealType.MealRecipe(Map.of(Items.PORKCHOP, 1, Items.CARROT, 3)), 30, 6, 5));
    public static RegistryObject<MealType> BEETROOT_SOUP = registerMealType("beetroot_soup", (name) -> new MealType(new MealType.MealNutrients(0,3, 0, 0),
            new MealType.MealRecipe(Map.of(Items.BEETROOT, 3)), 15, 2, 5));
    public static RegistryObject<MealType> PORK_POT_ROAST = registerMealType("pork_pot_roast", (name) -> new MealType(new MealType.MealNutrients(2,0, 0, 0),
            new MealType.MealRecipe(Map.of(Items.PORKCHOP, 2)), 16, 4, 4));
    public static RegistryObject<MealType> BEEF_POT_ROAST = registerMealType("beef_pot_roast", (name) -> new MealType(new MealType.MealNutrients(2,0, 0, 0),
            new MealType.MealRecipe(Map.of(Items.BEEF, 2)), 16, 4, 4));

    public static void register(IEventBus bus) {
        MEAL_TYPES.register(bus);
        MEAL_TYPE_REGISTRY = MEAL_TYPES.makeRegistry(RegistryBuilder::new);
    }

    public static RegistryObject<MealType> registerMealType(String name, Function<String, MealType> mealFn) {
        var mealType = mealFn.apply(name);
        var mealTypeRegistryObject = MEAL_TYPES.register(name, () -> mealType);

        var itemRegistryObject = ModItems.register(name, (itemName) -> {
            var mealItem = new Meal(
                    new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).food((
                            new FoodProperties.Builder()).nutrition(Mth.ceil(mealType.getCalories() / mealType.getServings())).saturationModifier(0.3F).build()));

            mealType.setItem(mealItem);

            return mealItem;
        });

        ModItems.MEAL_ITEMS.add(itemRegistryObject);

        return mealTypeRegistryObject;
    }

    public static void initializeMealTypes() {
        ALL_RECIPE_INGREDIENTS = getAllRecipeIngredients();
    }

    public static MealType getMealType(RegistryObject<MealType> registryObject) {
        return MEAL_TYPE_REGISTRY.get().getValue(registryObject.getId());
    }

    public static MealType getMealType(Meal meal) {
        return MEAL_TYPES.getEntries().stream()
                .filter(mealTypeRegistryObject -> mealTypeRegistryObject.get().getItem().equals(meal))
                .map(RegistryObject::get).findFirst().orElse(null);
    }

    public static Collection<MealType> getAllMealTypes() {
        return MEAL_TYPE_REGISTRY.get().getValues();
    }

    public static Set<Item> getAllRecipeIngredients() {
        return getAllMealTypes().stream().flatMap(mealType -> mealType.getRecipe().getIngredients().keySet().stream()).collect(Collectors.toSet());
    }

    public static String getMealTypeResourceName(RegistryObject<MealType> mealTypeRegistryObject) {
        return String.valueOf(mealTypeRegistryObject.getId()).replace(":", "_");
    }

    public static String getMealTypeResourceName(MealType mealType) {
        return String.valueOf(MEAL_TYPE_REGISTRY.get().getKey(mealType)).replace(":", "_");
    }
}
