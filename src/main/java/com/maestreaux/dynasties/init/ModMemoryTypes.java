package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Plot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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
}
