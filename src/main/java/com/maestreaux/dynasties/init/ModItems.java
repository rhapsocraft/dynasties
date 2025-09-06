package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.items.debug.DebugPlottingToolItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

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

    public static final RegistryObject<Item> DEBUG_TOOL_PLOT = register("debug_tool_plot", (name) -> new DebugPlottingToolItem(
            new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));
    // public static final RegistryObject<Item> DEBUG_TOOL_SELECTOR = ITEMS.register("dynasties_debug_tool_selector", () -> new DebugPlottingToolItem(new Item.Properties()));

    public static final RegistryObject<Item> TENT = register("tent_item", (name) -> new BlockItem(ModBlocks.TENT.get(),
            new Item.Properties().setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))));

    public static RegistryObject<Item> register(String name, Function<String, Item> itemFn) {
        return ITEMS.register(name, () -> itemFn.apply(name));
    }

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