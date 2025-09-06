package com.maestreaux.dynasties.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static Set<Item> FOOD = new HashSet<>(List.of(
            Items.POTATO,
            Items.CARROT,
            Items.BAKED_POTATO,
            Items.COOKED_BEEF,
            Items.COOKED_PORKCHOP,
            Items.COOKED_CHICKEN
    ));

    public static Set<Item> INGREDIENTS = new HashSet<>(List.of(
            Items.POTATO,
            Items.BEEF,
            Items.PORKCHOP,
            Items.CHICKEN
    ));
}

