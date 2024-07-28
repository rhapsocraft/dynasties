package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.items.debug.DebugPlottingToolItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DynastiesMod.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynastiesMod.MODID);

    public static final RegistryObject<Item> DYNASTY_VILLAGER_SPAWN_EGG = ITEMS.register("dynasty_villager_spawn_egg", () -> new ForgeSpawnEggItem(ModEntityTypes.DYNASTY_VILLAGER, 0x786959, 0x5d5656, new Item.Properties()));

    public static final RegistryObject<Item> DEBUG_TOOL = ITEMS.register("dynasties_debug_tool", () -> new SimpleFoiledItem(new Item.Properties()));

    public static final RegistryObject<Item> DEBUG_TOOL_PLOT = ITEMS.register("dynasties_debug_tool_plot", () -> new DebugPlottingToolItem(new Item.Properties()));
    // public static final RegistryObject<Item> DEBUG_TOOL_SELECTOR = ITEMS.register("dynasties_debug_tool_selector", () -> new DebugPlottingToolItem(new Item.Properties()));

    public static final RegistryObject<Item> TENT = ITEMS.register("tent", () -> new BlockItem(ModBlocks.TENT.get(), new Item.Properties()));


    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("villager_dynasties", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(Items.HONEY_BLOCK::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(DYNASTY_VILLAGER_SPAWN_EGG.get());
                output.accept(DEBUG_TOOL.get());
                output.accept(DEBUG_TOOL_PLOT.get());
                output.accept(TENT.get());
            }).build());

}