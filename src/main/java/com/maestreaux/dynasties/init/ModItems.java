package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.items.Meal;
import com.maestreaux.dynasties.world.items.PackedGoods;
import com.maestreaux.dynasties.world.items.debug.DebugPlottingToolItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.maestreaux.dynasties.init.ModMealTypes.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DynastiesMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynastiesMod.MODID);
    // 0x786959, 0x5d5656,
    public static final RegistryObject<Item> DYNASTY_VILLAGER_SPAWN_EGG = register("dynasties_villager_spawn_egg", (name) -> new SpawnEggItem(ModEntityTypes.DYNASTY_VILLAGER.get(), new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));

    public static final RegistryObject<Item> DEBUG_TOOL = register("debug_tool", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))) {
        @Override
        public boolean isFoil(ItemStack itemstack) {
            return true;
        }
    });
    public static final RegistryObject<Item> DEBUG_TOOL_PLOT_CONVERTER = register("debug_tool_plot_converter", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))) {
        @Override
        public boolean isFoil(ItemStack itemstack) {
            return true;
        }
    });
    public static final RegistryObject<Item> DEBUG_TOOL_PLOT = register("debug_tool_plot", (name) -> new DebugPlottingToolItem(
            new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));
    // public static final RegistryObject<Item> DEBUG_TOOL_SELECTOR = ITEMS.register("dynasties_debug_tool_selector", () -> new DebugPlottingToolItem(new Item.Properties()));
    public static final RegistryObject<Item> TENT = register("tent_item", (name) -> new BlockItem(ModBlocks.TENT.get(),
            new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));

    public static final RegistryObject<Item> CAMPFIRE_POT = register("campfire_pot", (name) -> new BlockItem(ModBlocks.CAMPFIRE_POT.get(),
            new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));

    public static final RegistryObject<Item> COIN = register("coin", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));
    public static final RegistryObject<Item> PACKED_GOODS = register("packed_goods", (name) -> new PackedGoods(new Item.Properties()
            .setId((ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))))
            .stacksTo(1)
            .component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
    ));

    public static final RegistryObject<Item> SPOON = register("spoon", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));

    public static final RegistryObject<Item> WOOL_YARN = register("wool_yarn", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).stacksTo(16)));
    public static final RegistryObject<Item> WOOL_CLOTH = register("wool_cloth", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).stacksTo(16)));
    public static final RegistryObject<Item> WOOL_TUNIC = register("wool_tunic", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).stacksTo(1)));
    public static final RegistryObject<Item> WOOL_PANTS = register("wool_pants", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).stacksTo(1)));

    public static final RegistryObject<Item> LINEN_CLOTH = register("linen_cloth", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).stacksTo(16)));
    public static final RegistryObject<Item> SILK_CLOTH = register("silk_cloth", (name) -> new Item(new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name))).stacksTo(16)));

//    public static final RegistryObject<Item> BEEF_POTATO_STEW_MEAL = register("beef_potato_stew", (name) -> new Meal(
//            new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))
//    , ModMealTypes.getMealType(BEEF_POTATO_STEW)));

    public static final List<RegistryObject<Item>> MEAL_ITEMS = new ArrayList<>();

    public static RegistryObject<Item> register(String name, Function<String, Item> itemFn) {
        return ITEMS.register(name, () -> itemFn.apply(name));
    }

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("villager_dynasties_misc", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(ModItems.CAMPFIRE_POT.get()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(DYNASTY_VILLAGER_SPAWN_EGG.get());
                output.accept(DEBUG_TOOL.get());
                output.accept(DEBUG_TOOL_PLOT.get());
                output.accept(DEBUG_TOOL_PLOT_CONVERTER.get());
                output.accept(TENT.get());
                output.accept(COIN.get());
                output.accept(PACKED_GOODS.get());
                output.accept(CAMPFIRE_POT.get());
                output.accept(SPOON.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> MEALS_TAB = CREATIVE_MODE_TABS.register("villager_dynasties_meals", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(ModItems.MEAL_ITEMS.getFirst().get()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                for (var mealItem : MEAL_ITEMS) {
                    output.accept(mealItem.get());
                }
            }).build());

    public static final RegistryObject<CreativeModeTab> TEXTILES_TAB = CREATIVE_MODE_TABS.register("villager_dynasties_textiles", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(ModItems.WOOL_CLOTH.get()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(WOOL_YARN.get());
                output.accept(WOOL_CLOTH.get());
                output.accept(WOOL_TUNIC.get());
                output.accept(WOOL_PANTS.get());
                output.accept(LINEN_CLOTH.get());
                output.accept(SILK_CLOTH.get());
            }).build());

}