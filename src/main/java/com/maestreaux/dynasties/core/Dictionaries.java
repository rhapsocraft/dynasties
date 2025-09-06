package com.maestreaux.dynasties.core;

import com.maestreaux.dynasties.init.ModItems;
import com.maestreaux.dynasties.init.ModMealTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Temporary
public class Dictionaries {
    public static Set<Item> VALID_SEEDS = new HashSet<>(List.of(
            Items.POTATO,
            Items.CARROT,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD,
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS
    ));

    public static Set<Item> FOOD = new HashSet<>(Stream.concat(Stream.of(
            Items.POTATO,
            Items.CARROT,
            Items.BAKED_POTATO,
            Items.COOKED_BEEF,
            Items.COOKED_PORKCHOP,
            Items.COOKED_CHICKEN
    ), ModItems.MEAL_ITEMS.stream().map(RegistryObject::get)).collect(Collectors.toSet()));

    public static Set<Item> INGREDIENTS = new HashSet<>(List.of(
            Items.POTATO,
            Items.BEEF,
            Items.PORKCHOP,
            Items.CHICKEN
    ));

    public static int BASE_DESIRED_SUPPLY = 8;

    public static int BASE_PRICE = 1;
}

