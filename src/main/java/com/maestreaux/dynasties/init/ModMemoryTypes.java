package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.world.Plot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;

public class ModMemoryTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, DynastiesMod.MODID);
    public static final RegistryObject<MemoryModuleType<Plot>> HOME_PLOT = MEMORY_TYPES.register("home_plot", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<Plot>>> AVAILABLE_PLOTS = MEMORY_TYPES.register("available_plots", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<BlockPos>> AVAILABLE_TENT = MEMORY_TYPES.register("available_tent", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<BlockPos>>> HOME_FARMLANDS = MEMORY_TYPES.register("home_farmlands", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<BaseContainerBlockEntity>>> HOME_CONTAINERS = MEMORY_TYPES.register("home_containers", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<ItemLocation>>> AVAILABLE_SEEDS = MEMORY_TYPES.register("available_seeds", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<BlockPos>>> FULLY_GROWN_CROPS = MEMORY_TYPES.register("fully_grown_crops", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<ItemLocation>>> AVAILABLE_FOOD = MEMORY_TYPES.register("available_food", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> HOME_LIVESTOCK = MEMORY_TYPES.register("home_livestock", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<ItemLocation>>> INGREDIENTS = MEMORY_TYPES.register("ingredients", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<List<CampfireBlockEntity>>> HOME_CAMPFIRES = MEMORY_TYPES.register("home_campfires", () -> new MemoryModuleType<>(Optional.empty()));
}
